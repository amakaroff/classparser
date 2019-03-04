package com.classparser.reflection;

import com.classparser.api.ClassParser;
import com.classparser.configuration.Configuration;
import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.configuration.ReflectionParserManager;
import com.classparser.reflection.configuration.api.Clearance;
import com.classparser.reflection.configuration.api.ReflectionParserConfiguration;
import com.classparser.reflection.exception.ReflectionParserException;
import com.classparser.reflection.parser.ClassTypeParser;
import com.classparser.reflection.parser.InheritanceParser;
import com.classparser.reflection.parser.base.*;
import com.classparser.reflection.parser.imports.ImportParser;
import com.classparser.reflection.parser.structure.ClassContentParser;
import com.classparser.reflection.parser.structure.ClassSignatureParser;
import com.classparser.reflection.parser.structure.PackageParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link ClassParser} provides
 * functionality for parsing class by reflection mechanism
 * That parsing mechanism can parse all classes, including
 * array and primitive types.
 * <p>
 * Thread safe
 *
 * @author Aleksey Makarov
 * @author Vadim Kiselev
 * @since 1.0.0
 */
public class ReflectionParser implements ClassParser {

    private final IndentParser indentParser;

    private final ImportParser importParser;

    private final PackageParser packageParser;

    private final ClassContentParser classContentParser;

    private final ClassSignatureParser classSignatureParser;

    private final ReflectionParserManager manager;

    private final ConfigurationManager configurationManager;

    private final List<Clearance> clearances;

    public ReflectionParser() {
        this.manager = new ReflectionParserManager();
        this.configurationManager = manager.getConfigurationManager();

        this.indentParser = new IndentParser(manager);
        this.importParser = new ImportParser(manager);

        ModifierParser modifierParser = new ModifierParser(manager);
        ClassNameParser classNameParser = new ClassNameParser(importParser, manager);
        AnnotationParser annotationParser = new AnnotationParser(indentParser, manager, modifierParser, classNameParser);
        this.packageParser = new PackageParser(annotationParser, manager);

        GenericTypeParser genericTypeParser = new GenericTypeParser(classNameParser, annotationParser, manager);
        ValueParser valueParser = new ValueParser(genericTypeParser, annotationParser, manager);
        annotationParser.setValueParser(valueParser);
        this.classContentParser = new ClassContentParser(this, manager, indentParser, annotationParser,
                genericTypeParser, modifierParser, classNameParser, valueParser);

        ClassTypeParser classTypeParser = new ClassTypeParser();
        InheritanceParser inheritanceParser = new InheritanceParser(genericTypeParser, manager);
        this.classSignatureParser = new ClassSignatureParser(annotationParser, genericTypeParser, indentParser,
                modifierParser, classTypeParser, classNameParser, inheritanceParser, manager);

        List<Clearance> clearances = new ArrayList<>();

        clearances.add(manager);
        clearances.add(importParser);
        clearances.add(classNameParser);

        this.clearances = clearances;
    }

    @Override
    public String parseClass(Class<?> clazz) throws ReflectionParserException {
        if (clazz != null) {
            setUp(clazz);
            try {
                String lineSeparator = configurationManager.getLineSeparator();
                String packageName = packageParser.parsePackage(clazz);
                String indent = indentParser.getIndent(clazz);
                String classSignature = classSignatureParser.parseClassSignature(clazz);
                String classContent = classContentParser.parseContent(clazz);
                String imports = getImports(clazz);
                String classBody = '{' + lineSeparator + lineSeparator + classContent + indent + '}';

                return packageName + imports + classSignature + ' ' + classBody;
            } finally {
                tearDown(clazz);
            }
        }

        throw new ReflectionParserException("Parsed class can't be a null!");
    }

    /**
     * Initializes current parser context
     *
     * @param clazz class to be parsed
     */
    private void setUp(Class<?> clazz) {
        manager.setCurrentParsedClass(clazz);

        if (manager.getBaseParsedClass() == null) {
            manager.setBaseParsedClass(clazz);
            importParser.initBeforeParsing();
        }
    }

    /**
     * Clears current parser context
     *
     * @param clazz parsed class
     */
    private void tearDown(Class<?> clazz) {
        manager.popCurrentClass();

        if (clazz == manager.getBaseParsedClass()) {
            clearances.forEach(Clearance::clear);
        }
    }

    /**
     * Parses import section for class
     *
     * @param clazz any class
     * @return parsed import section or empty string if {@link ConfigurationManager#isEnabledImports()} disable
     */
    private String getImports(Class<?> clazz) {
        if (clazz == manager.getBaseParsedClass() && configurationManager.isEnabledImports()) {
            return importParser.getImports();
        }

        return "";
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        if (configuration instanceof ReflectionParserConfiguration) {
            configurationManager.reloadConfiguration(configuration);
        } else {
            throw new ReflectionParserException("Configuration should be type ReflectionParserConfiguration");
        }
    }
}