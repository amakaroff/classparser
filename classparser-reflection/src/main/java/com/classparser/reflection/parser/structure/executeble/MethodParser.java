package com.classparser.reflection.parser.structure.executeble;

import com.classparser.reflection.ParseContext;
import com.classparser.reflection.ContentJoiner;
import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.parser.base.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Class provides functionality for parsing methods of class meta information
 * Parsing includes annotation, generics and default values for annotation methods
 *
 * @author Aleksey Makarov
 * @author Valim Kiselev
 * @since 1.0.0
 */
public class MethodParser {

    private final ConfigurationManager configurationManager;

    private final GenericTypeParser genericTypeParser;

    private final ModifierParser modifierParser;

    private final AnnotationParser annotationParser;

    private final ArgumentParser argumentParser;

    private final IndentParser indentParser;

    private final ExceptionParser exceptionParser;

    private final ClassNameParser classNameParser;

    private final ValueParser valueParser;

    public MethodParser(ConfigurationManager configurationManager,
                        GenericTypeParser genericTypeParser,
                        ModifierParser modifierParser,
                        AnnotationParser annotationParser,
                        ArgumentParser argumentParser,
                        IndentParser indentParser,
                        ExceptionParser exceptionParser,
                        ClassNameParser classNameParser,
                        ValueParser valueParser) {
        this.configurationManager = configurationManager;
        this.genericTypeParser = genericTypeParser;
        this.modifierParser = modifierParser;
        this.annotationParser = annotationParser;
        this.argumentParser = argumentParser;
        this.indentParser = indentParser;
        this.exceptionParser = exceptionParser;
        this.classNameParser = classNameParser;
        this.valueParser = valueParser;
    }

    /**
     * Parses meta information of class about all methods and collects data to {@link String}
     *
     * @param clazz any class
     * @return parsed methods
     */
    public String parseMethods(Class<?> clazz, ParseContext context) {
        List<String> staticMethods = new ArrayList<>();
        List<String> instanceMethods = new ArrayList<>();

        for (Method method : clazz.getDeclaredMethods()) {
            if (isDisplayMethod(method)) {
                String parsedMethod = parseMethod(method, context);
                if (Modifier.isStatic(method.getModifiers())) {
                    staticMethods.add(parsedMethod);
                } else {
                    instanceMethods.add(parsedMethod);
                }
            }
        }

        staticMethods.addAll(instanceMethods);

        return ContentJoiner.joinContent(staticMethods, configurationManager.getLineSeparator());
    }

    /**
     * Checks if method displaying is necessary
     *
     * @param method any method
     * @return true if display is needed
     */
    private boolean isDisplayMethod(Method method) {
        if (method.getDeclaringClass().isEnum()) {
            return configurationManager.isParseEnumAsClass() || !isSpecialEnumMethod(method);
        } else {
            return configurationManager.isDisplaySyntheticEntities() ||
                    !method.isSynthetic() && !method.isBridge();
        }
    }

    /**
     * Parses method meta information and collects data about method,
     * includes the name, types, generics, annotation etc.
     *
     * @param method any method
     * @return parsed method
     */
    public String parseMethod(Method method, ParseContext context) {
        String indent = indentParser.getIndent(method, context);
        String annotations = annotationParser.parseAnnotationsAsBlock(method, context);
        String modifiers = modifierParser.parseModifiers(method);
        String generics = genericTypeParser.parseGenerics(method, context);
        String returnType = genericTypeParser.parseType(getReturnType(method), method.getAnnotatedReturnType(), context);
        String methodName = classNameParser.getMemberName(method);
        String arguments = argumentParser.parseArguments(method, context);
        String defaultAnnotationValue = valueParser.getValue(method, context);
        String exceptions = exceptionParser.parseExceptions(method, context);
        String body = parseBody(method, context);
        String content = ContentJoiner.joinNotEmptyContentBySpace(modifiers, generics, returnType);

        return annotations + indent + content + " " + methodName + arguments +
                defaultAnnotationValue + exceptions + body;
    }

    /**
     * Obtains return type for method in depend on {@link ConfigurationManager#isDisplayGenericSignatures()}
     *
     * @param method any method
     * @return return type for method
     */
    private Type getReturnType(Method method) {
        if (configurationManager.isDisplayGenericSignatures()) {
            return method.getGenericReturnType();
        } else {
            return method.getReturnType();
        }
    }

    /**
     * Obtains body for method
     * As reflection is can't be access to byte code body not contained java code
     *
     * @param method any method
     * @return parsed method body
     */
    private String parseBody(Method method, ParseContext context) {
        String lineSeparator = configurationManager.getLineSeparator();
        String oneIndent = configurationManager.getIndentSpaces();
        String indent = indentParser.getIndent(method, context);

        if (isMethodExistsRealization(method)) {
            return " {" + lineSeparator + indent + oneIndent + "/* Compiled code */" + lineSeparator + indent + '}';
        } else {
            return ";";
        }
    }

    /**
     * Checks if method is exists realization
     *
     * @param method any method
     * @return true if for method exists realization
     */
    private boolean isMethodExistsRealization(Method method) {
        return !Modifier.isAbstract(method.getModifiers()) && !Modifier.isNative(method.getModifiers());
    }

    private boolean isSpecialEnumMethod(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        if (declaringClass.isEnum() && Modifier.isStatic(method.getModifiers())) {
            Class<?> returnType = method.getReturnType();
            Class<?>[] parameterTypes = method.getParameterTypes();
            String methodName = method.getName();

            if (methodName.equals("valueOf") &&
                    returnType.equals(declaringClass) &&
                    parameterTypes.length == 1 &&
                    parameterTypes[0].equals(String.class)) {
                return true;
            } else {
                return methodName.equals("values") &&
                        returnType.isArray() &&
                        returnType.getComponentType().equals(declaringClass) &&
                        parameterTypes.length == 0;
            }
        }

        return false;
    }
}