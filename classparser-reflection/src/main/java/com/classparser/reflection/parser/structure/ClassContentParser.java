package com.classparser.reflection.parser.structure;

import com.classparser.reflection.ParseContext;
import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.parser.structure.executeble.ConstructorParser;
import com.classparser.reflection.parser.structure.executeble.MethodParser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClassContentParser {

    private final FieldParser fieldParser;

    private final BlockParser blockParser;

    private final ConstructorParser constructorParser;

    private final MethodParser methodParser;

    private final ClassesParser classesParser;

    private final ConfigurationManager manager;

    public ClassContentParser(FieldParser fieldParser,
                              BlockParser blockParser,
                              ConstructorParser constructorParser,
                              MethodParser methodParser,
                              ClassesParser classesParser,
                              ConfigurationManager manager) {
        this.fieldParser = fieldParser;
        this.classesParser = classesParser;
        this.constructorParser = constructorParser;
        this.methodParser = methodParser;
        this.blockParser = blockParser;
        this.manager = manager;
    }

    /**
     * Parses signature for class
     * Includes fields, static initializer block, constructors, methods and inner classes
     *
     * @param clazz any class
     * @return parsed class context
     */
    public String getClassContent(Class<?> clazz, ParseContext context) {
        List<String> contents = new ArrayList<>();

        contents.add(fieldParser.parseFields(clazz, context));
        contents.add(blockParser.parseStaticBlock(clazz, context));
        contents.add(constructorParser.parseConstructors(clazz, context));
        contents.add(methodParser.parseMethods(clazz, context));
        contents.add(classesParser.parseInnerClasses(clazz, context));
        String lineSeparator = manager.getLineSeparator();

        return contents.stream().filter(content -> !content.isEmpty()).collect(Collectors.joining(lineSeparator));
    }
}
