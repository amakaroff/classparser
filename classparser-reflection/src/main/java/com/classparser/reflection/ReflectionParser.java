package com.classparser.reflection;

import com.classparser.api.ClassParser;
import com.classparser.configuration.Configuration;
import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.exception.ReflectionParserException;
import com.classparser.reflection.parser.ClassTypeParser;
import com.classparser.reflection.parser.InheritanceParser;
import com.classparser.reflection.parser.base.*;
import com.classparser.reflection.parser.structure.*;
import com.classparser.reflection.parser.structure.executeble.ArgumentParser;
import com.classparser.reflection.parser.structure.executeble.ConstructorParser;
import com.classparser.reflection.parser.structure.executeble.ExceptionParser;
import com.classparser.reflection.parser.structure.executeble.MethodParser;

/**
 * Implementation of {@link ClassParser} provides
 * functionality for parsing class by reflection mechanism
 * That parsing mechanism can parse all classes, including
 * array and primitive types.
 * <p>
 * Thread safe
 *
 * @author Aleksey Makarov
 * @author Valim Kiselev
 * @since 1.0.0
 */
public class ReflectionParser implements ClassParser {

    private final IndentParser indentParser;

    private final ImportParser importParser;

    private final PackageParser packageParser;

    private final ClassContentParser classContentParser;

    private final ClassSignatureParser classSignatureParser;

    private final ConfigurationManager manager;

    public ReflectionParser() {
        manager = new ConfigurationManager();
        indentParser = new IndentParser(manager);
        importParser = new ImportParser(manager);
        ClassNameParser classNameParser = new ClassNameParser(importParser);
        GenericTypeParser genericTypeParser = new GenericTypeParser(classNameParser, manager);
        ModifierParser modifierParser = new ModifierParser(manager);
        AnnotationParser annotationParser = new AnnotationParser(indentParser, genericTypeParser, manager, modifierParser);
        packageParser = new PackageParser(annotationParser, manager);
        ValueParser valueParser = new ValueParser(genericTypeParser, annotationParser, manager);
        annotationParser.setValueParser(valueParser);
        genericTypeParser.setAnnotationParser(annotationParser);

        this.classSignatureParser = new ClassSignatureParser(
                annotationParser,
                indentParser,
                modifierParser,
                classNameParser,
                new ClassTypeParser(),
                genericTypeParser,
                new InheritanceParser(genericTypeParser, manager)
        );

        ArgumentParser argumentParser = new ArgumentParser(manager, genericTypeParser, modifierParser, annotationParser);
        ExceptionParser exceptionParser = new ExceptionParser(genericTypeParser, manager);

        this.classContentParser = new ClassContentParser(
                new FieldParser(manager, annotationParser, indentParser, modifierParser, genericTypeParser, classNameParser, valueParser),
                new BlockParser(indentParser, manager),
                new ConstructorParser(manager, genericTypeParser, modifierParser, annotationParser, argumentParser, indentParser, exceptionParser),
                new MethodParser(manager, genericTypeParser, modifierParser, annotationParser, argumentParser, indentParser, exceptionParser, classNameParser, valueParser),
                new ClassesParser(this, manager),
                manager
        );
    }

    @Override
    public String parseClass(Class<?> clazz) throws ReflectionParserException {
        return parseClass(clazz, new ParseContext(clazz));
    }

    public String parseClass(Class<?> clazz, ParseContext context) throws ReflectionParserException {
        if (clazz != null) {
            setUp(clazz, context);
            try {
                String lineSeparator = manager.getLineSeparator();
                String packageName = packageParser.parsePackage(clazz, context);
                String indent = indentParser.getIndent(clazz, context);
                String classSignature = classSignatureParser.getClassSignature(clazz, context);
                String classContent = classContentParser.getClassContent(clazz, context);
                String imports = getImports(clazz, context);
                String classBody = '{' + lineSeparator + lineSeparator + classContent + indent + '}';

                return packageName + imports + classSignature + ' ' + classBody;
            } finally {
                tearDown(context);
            }
        }

        throw new ReflectionParserException("Parsed class can't be a null!");
    }

    /**
     * Initializes current parser context
     *
     * @param clazz class to be parsed
     * @param context context of parsing class process
     */
    private void setUp(Class<?> clazz, ParseContext context) {
        context.setCurrentParsedClass(clazz);
    }

    /**
     * Clears current parser context
     *
     * @param context context of parsing class process
     */
    private void tearDown(ParseContext context) {
        context.popCurrentClass();
    }

    /**
     * Parses import section for class
     *
     * @param clazz any class
     * @return parsed import section or empty string if {@link ConfigurationManager#isEnabledImports()} disable
     */
    private String getImports(Class<?> clazz, ParseContext context) {
        if (context.isBasedParsedClass(clazz) && manager.isEnabledImports()) {
            return importParser.getImports(context);
        }

        return "";
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        manager.reloadConfiguration(configuration);
    }
}