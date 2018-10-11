package com.classparser.reflection.parser.structure;

import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.configuration.ReflectionParserManager;
import com.classparser.reflection.parser.base.AnnotationParser;

/**
 * Class provides functionality for parsing package meta information
 * This class depends on parsing context {@link ReflectionParserManager}
 *
 * @author Aleksey Makarov
 * @author Valim Kiselev
 * @since 1.0.0
 */
public class PackageParser {

    private final AnnotationParser annotationParser;

    private final ReflectionParserManager manager;

    private final ConfigurationManager configurationManager;

    public PackageParser(AnnotationParser annotationParser, ReflectionParserManager manager) {
        this.annotationParser = annotationParser;
        this.manager = manager;
        this.configurationManager = manager.getConfigurationManager();
    }

    /**
     * Parses package meta information of given class
     * Includes package annotation from special {package-info} classes
     *
     * @param clazz any class
     * @return string line with package meta information
     */
    public String parsePackage(Class<?> clazz) {
        if (isShouldBeDisplayed(clazz)) {
            Package classPackage = clazz.getPackage();
            String packageAnnotations = annotationParser.parseAnnotationsAsBlock(classPackage);
            String lineSeparator = configurationManager.getLineSeparator();

            return packageAnnotations + "package " + classPackage.getName() + ';' + lineSeparator + lineSeparator;
        }

        return "";
    }

    /**
     * Checks if displaying package section for class is necessary
     *
     * @param clazz any class
     * @return true if package section should be displayed
     */
    private boolean isShouldBeDisplayed(Class<?> clazz) {
        Package packageForClass = clazz.getPackage();
        Class<?> parsedClass = manager.getBaseParsedClass();

        return packageForClass != null && clazz == parsedClass;
    }
}