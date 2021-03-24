package com.classparser.reflection.parser.structure.executeble;

import com.classparser.reflection.ParseContext;
import com.classparser.reflection.ContentJoiner;
import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.parser.base.AnnotationParser;
import com.classparser.reflection.parser.base.GenericTypeParser;
import com.classparser.reflection.parser.base.ModifierParser;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Class provides functionality by parsing meta information
 * about arguments for {@link Executable} objects
 *
 * @author Aleksey Makarov
 * @author Vadim Kiselev
 * @since 1.0.0
 */
public class ArgumentParser {

    private final ConfigurationManager configurationManager;

    private final GenericTypeParser genericTypeParser;

    private final ModifierParser modifierParser;

    private final AnnotationParser annotationParser;

    public ArgumentParser(ConfigurationManager configurationManager,
                          GenericTypeParser genericTypeParser,
                          ModifierParser modifierParser,
                          AnnotationParser annotationParser) {
        this.configurationManager = configurationManager;
        this.genericTypeParser = genericTypeParser;
        this.modifierParser = modifierParser;
        this.annotationParser = annotationParser;
    }

    /**
     * Parse arguments meta information and collecting all metadata
     * about arguments for any {@link Executable} instance.
     * Parsing result includes name, types, generics, annotations and etc.
     * <p>
     * For example:
     * <code>
     * (String arg0, int arg1, {@literal @}Annotation MyClass)
     * </code>
     *
     * @param executable any executable
     * @return parsed string line with information about arguments
     */
    public String parseArguments(Executable executable, ParseContext context) {
        List<String> strings = new ArrayList<>();

        if (isReceiverExplicitArgumentExists(executable)) {
            strings.add(parseReceiverExplicitArgument(executable, context));
        }

        AnnotatedType[] annotatedParameterTypes = executable.getAnnotatedParameterTypes();
        Parameter[] parameters = executable.getParameters();

        for (int index = 0; index < parameters.length; index++) {
            Parameter parameter = parameters[index];
            if (isShouldBeDisplayed(parameter, index)) {
                strings.add(getArgument(parameter, annotatedParameterTypes[index], context));
            }
        }

        return '(' + String.join(", ", strings) + ')';
    }

    /**
     * Checks if display argument is necessary
     *
     * @param parameter any argument
     * @param index     index of argument
     * @return true if display argument is necessary
     */
    private boolean isShouldBeDisplayed(Parameter parameter, int index) {
        Class<?> declaringClass = parameter.getDeclaringExecutable().getDeclaringClass();
        if (declaringClass.isEnum()) {
            return configurationManager.isDisplaySyntheticEntities() && configurationManager.isParseEnumAsClass() ||
                    !isSyntheticParameter(parameter, index);
        } else {
            return configurationManager.isDisplaySyntheticEntities() || !isSyntheticParameter(parameter, index);
        }
    }

    /**
     * Checks if argument is synthetic or implicit
     *
     * @param parameter any argument
     * @param index     index of argument
     * @return true if argument is synthetic or implicit
     */
    private boolean isSyntheticParameter(Parameter parameter, int index) {
        return parameter.isSynthetic() ||
                parameter.isImplicit() ||
                isSyntheticConstructorFirstParameter(parameter, index) ||
                isSyntheticEnumConstructorParameters(parameter, index);
    }

    /**
     * Checks if argument is first argument in nested class
     *
     * @param parameter any argument
     * @param index     argument index
     * @return true if argument is first in constructor for nested class
     */
    private boolean isSyntheticConstructorFirstParameter(Parameter parameter, int index) {
        Executable executable = parameter.getDeclaringExecutable();
        Class<?> clazz = executable.getDeclaringClass();
        return executable instanceof Constructor &&
                clazz.isMemberClass() &&
                !Modifier.isStatic(clazz.getModifiers()) &&
                index == 0;
    }

    /**
     * Checks first or second parameter in enum class
     *
     * @param parameter any parameter
     * @param index     parameter index
     * @return true if parameter is first or second in constructor for enum
     */
    private boolean isSyntheticEnumConstructorParameters(Parameter parameter, int index) {
        Executable executable = parameter.getDeclaringExecutable();
        return (executable instanceof Constructor &&
                executable.getDeclaringClass().isEnum() &&
                (index == 0 || index == 1));
    }

    /**
     * Parsing and obtain meta information about argument of {@link Executable} instance
     *
     * @param parameter     any parameter
     * @param annotatedType annotation type on parameter
     * @return parsed string line with information about argument
     */
    public String getArgument(Parameter parameter, AnnotatedType annotatedType, ParseContext context) {
        String annotations = annotationParser.parseAnnotationsAsInline(parameter, context);
        String type = resolveVariableArguments(parameter, annotatedType, context);
        String modifiers = modifierParser.parseModifiers(parameter);
        String name = parameter.getName();

        return ContentJoiner.joinNotEmptyContentBySpace(annotations, modifiers, type, name);
    }

    /**
     * Obtains argument type in depend on {@link ConfigurationManager#isDisplayGenericSignatures()} value
     *
     * @param parameter any argument
     * @return type of argument
     */
    private Type getParameterType(Parameter parameter) {
        if (configurationManager.isDisplayGenericSignatures()) {
            return parameter.getParameterizedType();
        } else {
            return parameter.getType();
        }
    }

    /**
     * Checks if receiver argument is exists
     * This method is based on exists receiver annotations and not always correctly working
     *
     * @param executable any executable
     * @return true if executable has receiver argument
     */
    private boolean isReceiverExplicitArgumentExists(Executable executable) {
        AnnotatedType annotatedReceiverType = executable.getAnnotatedReceiverType();
        if (annotatedReceiverType != null) {
            Annotation[] annotations = annotatedReceiverType.getAnnotations();
            return annotations != null && annotations.length > 0;
        }

        return false;
    }

    /**
     * Restorations receiver explicit argument for any executable instance
     *
     * @param executable any executable
     * @return parsed explicit receiver argument
     */
    private String parseReceiverExplicitArgument(Executable executable, ParseContext context) {
        AnnotatedType annotatedReceiverType = executable.getAnnotatedReceiverType();
        Class<?> declaringClass = executable.getDeclaringClass();

        String annotations = annotationParser.parseAnnotationsAsInline(annotatedReceiverType, context);
        String type = genericTypeParser.parseType(declaringClass, context) +
                genericTypeParser.parseGenerics(declaringClass, context);
        String name = "this";

        return annotations + " " + type + name;
    }

    /**
     * Parses argument and performs convert argument type if it's vararg
     * if option {@link ConfigurationManager#isDisplayVarArgs()} is enabled
     *
     * @param parameter     any argument
     * @param annotatedType annotation on type for this argument
     * @return parsed argument
     */
    private String resolveVariableArguments(Parameter parameter, AnnotatedType annotatedType, ParseContext context) {
        String type = genericTypeParser.parseType(getParameterType(parameter), annotatedType, context);
        if (parameter.isVarArgs() && configurationManager.isDisplayVarArgs()) {
            return type.substring(0, type.length() - 2) + "...";
        } else {
            return type;
        }
    }
}