package com.classparser.reflection.parser.structure.executeble;

import com.classparser.reflection.ContentJoiner;
import com.classparser.reflection.ParseContext;
import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.parser.base.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class provides functionality for parsing methods of class meta information
 * Parsing includes annotation, generics and default values for annotation methods
 *
 * @author Aleksey Makarov
 * @author Vadim Kiselev
 * @since 1.0.0
 */
public class MethodParser {

    private final GenericTypeParser genericTypeParser;

    private final ModifierParser modifierParser;

    private final AnnotationParser annotationParser;

    private final ArgumentParser argumentParser;

    private final IndentParser indentParser;

    private final ExceptionParser exceptionParser;

    private final ClassNameParser classNameParser;

    private final ValueParser valueParser;

    private final ConfigurationManager configurationManager;

    public MethodParser(ConfigurationManager configurationManager) {
        this.genericTypeParser = new GenericTypeParser(configurationManager);
        this.modifierParser = new ModifierParser(configurationManager);
        this.annotationParser = new AnnotationParser(configurationManager);
        this.argumentParser = new ArgumentParser(configurationManager);
        this.indentParser = new IndentParser(configurationManager);
        this.exceptionParser = new ExceptionParser(configurationManager);
        this.classNameParser = new ClassNameParser(configurationManager);
        this.valueParser = new ValueParser(configurationManager);
        this.configurationManager = configurationManager;
    }

    /**
     * Parses meta information of class about all methods and collects data to {@link String}
     *
     * @param clazz   any class
     * @param context context of parsing class process
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

        staticMethods.addAll(getStaticImplicitMethods(clazz, context));

        List<String> methods = new ArrayList<>();

        methods.addAll(staticMethods);
        methods.addAll(instanceMethods);

        return ContentJoiner.joinContent(methods, configurationManager.getLineSeparator());
    }

    /**
     * Parses method meta information and collects data about method,
     * includes the name, types, generics, annotation etc.
     *
     * @param method  any method
     * @param context context of parsing class process
     * @return parsed method
     */
    public String parseMethod(Method method, ParseContext context) {
        String indent = indentParser.getIndent(method, context);
        String annotations = annotationParser.parseAnnotationsAsBlock(method, context);
        String modifiers = modifierParser.parseModifiers(method);
        String generics = genericTypeParser.parseGenerics(method, true, context);
        String returnType = genericTypeParser.parseType(getReturnType(method),
                classNameParser.isInnerClassInStaticContext(method, method.getReturnType()),
                method.getAnnotatedReturnType(),
                context);
        String methodName = classNameParser.getMemberName(method);
        String arguments = argumentParser.parseArguments(method, context);
        String defaultAnnotationValue = valueParser.parseValue(method, context);
        String exceptions = exceptionParser.parseExceptions(method, context);
        String body = parseBody(method, context);
        String content = ContentJoiner.joinNotEmptyContentBySpace(modifiers, generics, returnType);

        return annotations + indent + content + " " + methodName + arguments +
                defaultAnnotationValue + exceptions + body;
    }

    /**
     * Checks if method displaying is necessary
     *
     * @param method any method
     * @return true if display is needed
     */
    private boolean isDisplayMethod(Method method) {
        if (method.getDeclaringClass().isEnum()) {
            return configurationManager.isDisplayEnumAsClass() || !isSpecialEnumMethod(method);
        } else {
            return configurationManager.isDisplaySyntheticEntities() ||
                    !method.isSynthetic() && !method.isBridge();
        }
    }

    /**
     * Collect static implicit methods, which cannot be obtained by reflection
     *
     * @param declaredClass declared class for methods
     * @param context       context of parsing class process
     * @return list of implicit methods
     */
    private List<String> getStaticImplicitMethods(Class<?> declaredClass, ParseContext context) {
        List<String> staticMethods = new ArrayList<>();

        if (declaredClass.equals(getUnsafeClass())) {
            staticMethods.add(parseUnsafeImplicitMethod(declaredClass, context));
        }

        return staticMethods;
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
     * @param method  any method
     * @param context context of parsing class process
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

    /**
     * Is default implicit enum methods
     *
     * @param method any enum method
     * @return true if method is implicit
     */
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

    private String parseImplicitMethod(Class<?> declaredClass,
                                       ParseContext context,
                                       int modifiers,
                                       Type returnType,
                                       String name,
                                       Type[] parameters) {
        String lineSeparator = configurationManager.getLineSeparator();
        String oneIndent = configurationManager.getIndentSpaces();
        String indent = indentParser.getIndent(declaredClass, context) + oneIndent;
        String parsedParameters = Arrays.stream(parameters)
                .map(type -> genericTypeParser.parseType(type, context))
                .collect(Collectors.joining(", "));

        return indent +
                modifierParser.parseMethodModifiers(modifiers, declaredClass) + " " +
                genericTypeParser.parseType(returnType, context) + " " +
                name + "(" + parsedParameters + ")" +
                " {" + lineSeparator + indent + oneIndent + "/* Compiled code */" + lineSeparator + indent + '}';
    }

    private String parseUnsafeImplicitMethod(Class<?> declaredClass, ParseContext context) {
        int modifiers = Modifier.PUBLIC | Modifier.STATIC;
        return parseImplicitMethod(declaredClass, context, modifiers, declaredClass, "getUnsafe", new Type[0]);
    }

    private Class<?> getUnsafeClass() {
        return classNameParser.forNameOrNull("sun.misc.Unsafe");
    }
}