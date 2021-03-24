package com.classparser.reflection.parser.structure.executeble;

import com.classparser.reflection.ParseContext;
import com.classparser.reflection.ContentJoiner;
import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.parser.base.AnnotationParser;
import com.classparser.reflection.parser.base.GenericTypeParser;
import com.classparser.reflection.parser.base.IndentParser;
import com.classparser.reflection.parser.base.ModifierParser;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Class provides functionality by parsing constructor of class meta information
 * Parsing includes annotation, generics etc.
 *
 * @author Aleksey Makarov
 * @author Vadim Kiselev
 * @since 1.0.0
 */
public class ConstructorParser {

    private final ConfigurationManager configurationManager;

    private final GenericTypeParser genericParser;

    private final ModifierParser modifierParser;

    private final AnnotationParser annotationParser;

    private final ArgumentParser argumentParser;

    private final IndentParser indentParser;

    private final ExceptionParser exceptionParser;

    public ConstructorParser(ConfigurationManager configurationManager,
                             GenericTypeParser genericParser,
                             ModifierParser modifierParser,
                             AnnotationParser annotationParser,
                             ArgumentParser argumentParser,
                             IndentParser indentParser,
                             ExceptionParser exceptionParser) {
        this.configurationManager = configurationManager;
        this.genericParser = genericParser;
        this.modifierParser = modifierParser;
        this.annotationParser = annotationParser;
        this.argumentParser = argumentParser;
        this.indentParser = indentParser;
        this.exceptionParser = exceptionParser;
    }

    /**
     * Parse constructors meta information and collecting all metadata
     * about constructors for any {@link Class} instance.
     * Parsing result includes name, types, generics, annotations, modifiers, exceptions etc.
     *
     * @param clazz any class
     * @return parsed constructors
     */
    public String parseConstructors(Class<?> clazz, ParseContext context) {
        List<String> constructors = new ArrayList<>();

        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (isShouldBeDisplayed(constructor)) {
                constructors.add(parseConstructor(constructor, context));
            }
        }

        return ContentJoiner.joinContent(constructors, configurationManager.getLineSeparator());
    }

    /**
     * Checks if display constructor is necessary
     *
     * @param constructor any constructor
     * @return true if display constructor is necessary
     */
    private boolean isShouldBeDisplayed(Constructor<?> constructor) {
        return configurationManager.isDisplaySyntheticEntities() || !constructor.isSynthetic();
    }

    /**
     * Parses the constructor meta information and collects data about method,
     * includes the name, generics, annotation etc.
     *
     * @param constructor any constructor
     * @return string line with constructor meta information
     */
    private String parseConstructor(Constructor<?> constructor, ParseContext context) {
        String annotations = annotationParser.parseAnnotationsAsBlock(constructor, context);
        String indent = indentParser.getIndent(constructor, context);
        String modifiers = modifierParser.parseModifiers(constructor);
        String generics = genericParser.parseGenerics(constructor, context);
        String constructorName = genericParser.parseType(constructor.getDeclaringClass(), context);
        String arguments = argumentParser.parseArguments(constructor, context);
        String exceptions = exceptionParser.parseExceptions(constructor, context);
        String oneIndent = configurationManager.getIndentSpaces();
        String lineSeparator = configurationManager.getLineSeparator();
        String body = " {" + lineSeparator + indent + oneIndent + "/* Compiled code */" + lineSeparator + indent + '}';
        String content = ContentJoiner.joinNotEmptyContentBySpace(modifiers, generics, constructorName);

        return annotations + indent + content + arguments + exceptions + body;
    }
}