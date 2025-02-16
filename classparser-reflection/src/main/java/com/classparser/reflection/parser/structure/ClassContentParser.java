package com.classparser.reflection.parser.structure;

import com.classparser.reflection.ParseContext;
import com.classparser.reflection.Phase;
import com.classparser.reflection.ReflectionParser;
import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.parser.base.ClassNameParser;
import com.classparser.reflection.parser.base.IndentParser;
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

    private final ClassNameParser classNameParser;

    private final IndentParser indentParser;

    private final ConfigurationManager manager;

    public ClassContentParser(ReflectionParser parser, ConfigurationManager configurationManager) {
        this.fieldParser = new FieldParser(configurationManager);
        this.classesParser = new ClassesParser(parser, configurationManager);
        this.constructorParser = new ConstructorParser(configurationManager);
        this.methodParser = new MethodParser(configurationManager);
        this.blockParser = new BlockParser(configurationManager);
        this.classNameParser = new ClassNameParser(configurationManager);
        this.indentParser = new IndentParser(configurationManager);
        this.manager = configurationManager;
    }

    /**
     * Parses signature for class
     * Includes fields, static initializer block, constructors, methods and inner classes
     *
     * @param context context of parsing class process
     * @return parsed class context
     */
    public String getClassContent(ParseContext context) {
        context.setPhase(Phase.BODY);
        try {
            Class<?> currentParsedClass = context.getCurrentParsedClass();
            if (manager.isDisplayPackageInfoAsClass() || !classNameParser.isPackageInfo(currentParsedClass)) {
                String lineSeparator = manager.getLineSeparator();
                String indent = indentParser.getIndent(currentParsedClass, context);

                List<String> contents = new ArrayList<>();

                contents.add(fieldParser.parseFields(context));
                contents.add(blockParser.parseStaticBlock(context));
                contents.add(constructorParser.parseConstructors(context));
                contents.add(methodParser.parseMethods(context));
                contents.add(classesParser.parseInnerClasses(context));

                String classContent = contents.stream()
                        .filter(content -> !content.isEmpty())
                        .collect(Collectors.joining(lineSeparator));

                return '{' + lineSeparator + lineSeparator + classContent + indent + '}';
            } else {
                return "";
            }
        } finally {
            context.clearPhase();
        }
    }
}
