package com.classparser.reflection.parser.structure.executeble;

import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.configuration.ReflectionParserManager;
import com.classparser.reflection.parser.base.ClassNameParser;
import com.classparser.reflection.parser.base.AnnotationParser;
import com.classparser.reflection.parser.base.GenericTypeParser;
import com.classparser.reflection.parser.base.IndentParser;
import com.classparser.reflection.parser.base.ModifierParser;
import com.classparser.reflection.parser.base.ValueParser;

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
 * @author Vadim Kiselev
 * @since 1.0.0
 */
public class MethodParser {

    private final ReflectionParserManager manager;

    private final ConfigurationManager configurationManager;

    private final GenericTypeParser genericTypeParser;

    private final ModifierParser modifierParser;

    private final AnnotationParser annotationParser;

    private final ArgumentParser argumentParser;

    private final IndentParser indentParser;

    private final ExceptionParser exceptionParser;

    private final ClassNameParser classNameParser;

    private final ValueParser valueParser;

    public MethodParser(ReflectionParserManager manager, GenericTypeParser genericTypeParser,
                        ModifierParser modifierParser, AnnotationParser annotationParser,
                        ArgumentParser argumentParser, IndentParser indentParser,
                        ExceptionParser exceptionParser, ClassNameParser classNameParser,
                        ValueParser valueParser) {
        this.manager = manager;
        this.configurationManager = manager.getConfigurationManager();
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
    public String parseMethods(Class<?> clazz) {
        List<String> methods = new ArrayList<>();

        for (Method method : clazz.getDeclaredMethods()) {
            if (isDisplayMethod(method)) {
                methods.add(parseMethod(method));
            }
        }

        return manager.joinContentByLineSeparator(methods);
    }

    /**
     * Checks if method displaying is necessary
     *
     * @param method any method
     * @return true if display is needed
     */
    private boolean isDisplayMethod(Method method) {
        return configurationManager.isDisplaySyntheticEntities() || !method.isSynthetic() && !method.isBridge();
    }

    /**
     * Parses method meta information and collects data about method,
     * includes name, types, generics, annotation and etc.
     *
     * @param method any method
     * @return parsed method
     */
    public String parseMethod(Method method) {
        String indent = indentParser.getIndent(method);
        String annotations = annotationParser.parseAnnotationsAsBlock(method);
        String modifiers = modifierParser.parseModifiers(method);
        String generics = genericTypeParser.parseGenerics(method);
        String returnType = genericTypeParser.parseType(getReturnType(method), method.getAnnotatedReturnType());
        String methodName = classNameParser.getMemberName(method);
        String arguments = argumentParser.parseArguments(method);
        String defaultAnnotationValue = valueParser.getValue(method);
        String exceptions = exceptionParser.parseExceptions(method);
        String body = parseBody(method);
        String content = manager.joinNotEmptyContentBySpace(modifiers, generics, returnType);

        return annotations + indent + content + " " + methodName + arguments + defaultAnnotationValue + exceptions + body;
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
     * As reflection is can't be access to bytecode body not contained java code
     *
     * @param method any method
     * @return parsed method body
     */
    private String parseBody(Method method) {
        String lineSeparator = configurationManager.getLineSeparator();
        String oneIndent = configurationManager.getIndentSpaces();
        String indent = indentParser.getIndent(method);

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
}