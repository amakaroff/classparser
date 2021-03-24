package com.classparser.reflection;

import com.classparser.api.ClassParser;
import com.classparser.configuration.Configuration;
import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.exception.ReflectionParserException;
import com.classparser.reflection.parser.ClassTypeParser;
import com.classparser.reflection.parser.InheritanceParser;
import com.classparser.reflection.parser.base.*;
import com.classparser.reflection.parser.base.ImportParser;
import com.classparser.reflection.parser.structure.BlockParser;
import com.classparser.reflection.parser.structure.ClassesParser;
import com.classparser.reflection.parser.structure.FieldParser;
import com.classparser.reflection.parser.structure.PackageParser;
import com.classparser.reflection.parser.structure.executeble.ArgumentParser;
import com.classparser.reflection.parser.structure.executeble.ConstructorParser;
import com.classparser.reflection.parser.structure.executeble.ExceptionParser;
import com.classparser.reflection.parser.structure.executeble.MethodParser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    private final AnnotationParser annotationParser;

    private final GenericTypeParser genericTypeParser;

    private final IndentParser indentParser;

    private final ModifierParser modifierParser;

    private final ClassTypeParser classTypeParser;

    private final ImportParser importParser;

    private final ClassNameParser classNameParser;

    private final InheritanceParser inheritanceParser;

    private final PackageParser packageParser;

    private final FieldParser fieldParser;

    private final ClassesParser classesParser;

    private final ConstructorParser constructorParser;

    private final MethodParser methodParser;

    private final BlockParser blockParser;

    private final ConfigurationManager manager;

    public ReflectionParser() {
        manager = new ConfigurationManager();
        classTypeParser = new ClassTypeParser();
        modifierParser = new ModifierParser(manager);
        indentParser = new IndentParser(manager);
        classesParser = new ClassesParser(this, manager);
        importParser = new ImportParser(manager);
        classNameParser = new ClassNameParser(importParser);
        genericTypeParser = new GenericTypeParser(classNameParser, manager);
        annotationParser = new AnnotationParser(indentParser, genericTypeParser, manager, modifierParser);
        ValueParser valueParser = new ValueParser(genericTypeParser, annotationParser, manager);
        annotationParser.setValueParser(valueParser);
        genericTypeParser.setAnnotationParser(annotationParser);
        blockParser = new BlockParser(indentParser, manager);
        inheritanceParser = new InheritanceParser(genericTypeParser, manager);
        packageParser = new PackageParser(annotationParser, manager);
        ArgumentParser argumentParser = new ArgumentParser(manager, genericTypeParser, modifierParser, annotationParser);
        ExceptionParser exceptionParser = new ExceptionParser(genericTypeParser, manager);
        fieldParser = new FieldParser(manager, annotationParser, indentParser, modifierParser, genericTypeParser,
                classNameParser, valueParser);
        constructorParser = new ConstructorParser(manager, genericTypeParser, modifierParser, annotationParser,
                argumentParser, indentParser, exceptionParser);
        methodParser = new MethodParser(manager, genericTypeParser, modifierParser, annotationParser, argumentParser,
                indentParser, exceptionParser, classNameParser, valueParser);
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
                String classSignature = getClassSignature(clazz, context);
                String classContent = getClassContent(clazz, context);
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

    /**
     * Parses signature for class
     * Include annotations, modifiers, type, name, generics and inheritances
     *
     * @param clazz any class
     * @return parsed signature of class
     */
    private String getClassSignature(Class<?> clazz, ParseContext context) {
        String annotations = annotationParser.parseAnnotationsAsBlock(clazz, context);
        String indent = indentParser.getIndent(clazz, context);
        String modifiers = modifierParser.parseModifiers(clazz);
        String name = classNameParser.parseTypeName(clazz, context);
        String classType = classTypeParser.parseClassType(clazz);
        String generics = genericTypeParser.parseGenerics(clazz, context);
        String inheritances = inheritanceParser.parseInheritances(clazz, context);
        String content = ContentJoiner.joinNotEmptyContentBySpace(modifiers, classType, name);
        String specialContent = ContentJoiner.joinNotEmptyContentBySpace(generics, inheritances);

        if (generics.isEmpty()) {
            return annotations + indent + ContentJoiner.joinNotEmptyContentBySpace(content, specialContent);
        } else {
            return annotations + indent + content + specialContent;
        }
    }

    /**
     * Parses signature for class
     * Includes fields, static initializer block, constructors, methods and inner classes
     *
     * @param clazz any class
     * @return parsed class context
     */
    private String getClassContent(Class<?> clazz, ParseContext context) {
        List<String> contents = new ArrayList<>();

        contents.add(fieldParser.parseFields(clazz, context));
        contents.add(blockParser.parseStaticBlock(clazz, context));
        contents.add(constructorParser.parseConstructors(clazz, context));
        contents.add(methodParser.parseMethods(clazz, context));
        contents.add(classesParser.parseInnerClasses(clazz, context));
        String lineSeparator = manager.getLineSeparator();

        return contents.stream().filter(content -> !content.isEmpty()).collect(Collectors.joining(lineSeparator));
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        manager.reloadConfiguration(configuration);
    }
}