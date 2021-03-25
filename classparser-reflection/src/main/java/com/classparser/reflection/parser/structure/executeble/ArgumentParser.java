package com.classparser.reflection.parser.structure.executeble;

import com.classparser.reflection.ContentJoiner;
import com.classparser.reflection.ParseContext;
import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.parser.base.AnnotationParser;
import com.classparser.reflection.parser.base.ClassNameParser;
import com.classparser.reflection.parser.base.GenericTypeParser;
import com.classparser.reflection.parser.base.ModifierParser;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
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

    private final GenericTypeParser genericTypeParser;

    private final ModifierParser modifierParser;

    private final AnnotationParser annotationParser;

    private final ClassNameParser classNameParser;

    private final ConfigurationManager configurationManager;

    public ArgumentParser(ConfigurationManager configurationManager) {
        this.genericTypeParser = new GenericTypeParser(configurationManager);
        this.modifierParser = new ModifierParser(configurationManager);
        this.annotationParser = new AnnotationParser(configurationManager);
        this.classNameParser = new ClassNameParser(configurationManager);
        this.configurationManager = configurationManager;
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
            if (isShouldBeDisplayed(parameter)) {
                strings.add(parseArgument(parameter, annotatedParameterTypes[index], context));
            }
        }

        return '(' + String.join(", ", strings) + ')';
    }

    /**
     * Checks if display argument is necessary
     *
     * @param parameter any argument
     * @return true if display argument is necessary
     */
    private boolean isShouldBeDisplayed(Parameter parameter) {
        return configurationManager.isDisplaySyntheticEntities() ||
                !modifierParser.isSynthetic(parameter) && !modifierParser.isImplicit(parameter);
    }

    /**
     * Parsing and obtain meta information about argument of {@link Executable} instance
     *
     * @param parameter     any parameter
     * @param annotatedType annotation type on parameter
     * @return parsed string line with information about argument
     */
    public String parseArgument(Parameter parameter, AnnotatedType annotatedType, ParseContext context) {
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
                genericTypeParser.parseGenerics(declaringClass, true, context);
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
        String type = genericTypeParser.parseType(getParameterType(parameter),
                classNameParser.isInnerClassInStaticContext(parameter.getDeclaringExecutable(), parameter.getType()),
                annotatedType,
                context);
        if (parameter.isVarArgs() && configurationManager.isDisplayVarArgs()) {
            return type.substring(0, type.length() - 2) + "...";
        } else {
            return type;
        }
    }
}