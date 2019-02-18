package com.classparser.reflection.parser.structure;

import com.classparser.reflection.ReflectionParser;
import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.configuration.ReflectionParserManager;
import com.classparser.reflection.parser.base.*;
import com.classparser.reflection.parser.structure.executeble.ArgumentParser;
import com.classparser.reflection.parser.structure.executeble.ConstructorParser;
import com.classparser.reflection.parser.structure.executeble.ExceptionParser;
import com.classparser.reflection.parser.structure.executeble.MethodParser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class provides functionality for parsing content of class: fields, methods, constructors and static blocks
 *
 * @author Aleksey Makarov
 * @since 1.0.5
 */
public class ClassContentParser {

    private final FieldParser fieldParser;

    private final ClassesParser classesParser;

    private final ConstructorParser constructorParser;

    private final MethodParser methodParser;

    private final BlockParser blockParser;

    private final ConfigurationManager configurationManager;

    public ClassContentParser(ReflectionParser reflectionParser,
                              ReflectionParserManager manager,
                              IndentParser indentParser,
                              AnnotationParser annotationParser,
                              GenericTypeParser genericTypeParser,
                              ModifierParser modifierParser,
                              ClassNameParser classNameParser,
                              ValueParser valueParser) {
        this.configurationManager = manager.getConfigurationManager();
        this.classesParser = new ClassesParser(reflectionParser, manager);
        this.blockParser = new BlockParser(indentParser, manager);

        ArgumentParser argumentParser = new ArgumentParser(manager, genericTypeParser, modifierParser, annotationParser);
        ExceptionParser exceptionParser = new ExceptionParser(genericTypeParser, manager);

        this.constructorParser = new ConstructorParser(manager, genericTypeParser, modifierParser,
                annotationParser, argumentParser, indentParser, exceptionParser);
        this.methodParser = new MethodParser(manager, genericTypeParser, modifierParser, annotationParser,
                argumentParser, indentParser, exceptionParser, classNameParser, valueParser);
        this.fieldParser = new FieldParser(manager, annotationParser, indentParser, modifierParser,
                genericTypeParser, classNameParser, valueParser);
    }

    /**
     * Parses signature for class
     * Includes fields, static initializer block, constructors, methods and inner classes
     *
     * @param clazz any class
     * @return parsed class context
     */
    public String parseContent(Class<?> clazz) {
        List<String> content = new ArrayList<>();

        content.add(fieldParser.parseFields(clazz));
        content.add(blockParser.parseStaticBlock(clazz));
        content.add(constructorParser.parseConstructors(clazz));
        content.add(methodParser.parseMethods(clazz));
        content.add(classesParser.parseInnerClasses(clazz));

        return content
                .stream()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(configurationManager.getLineSeparator()));
    }
}
