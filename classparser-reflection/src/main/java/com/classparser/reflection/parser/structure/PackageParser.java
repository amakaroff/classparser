package com.classparser.reflection.parser.structure;

import com.classparser.reflection.ParseContext;
import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.parser.base.AnnotationParser;

/**
 * Class provides functionality for parsing package meta information
 *
 * @author Aleksey Makarov
 * @author Vadim Kiselev
 * @since 1.0.0
 */
public class PackageParser {

    private final AnnotationParser annotationParser;

    private final ConfigurationManager configurationManager;

    public PackageParser(AnnotationParser annotationParser, ConfigurationManager configurationManager) {
        this.annotationParser = annotationParser;
        this.configurationManager = configurationManager;
    }

    /**
     * Parses package meta information of given class
     * Includes package annotation from special {package-info} classes
     *
     * @param clazz any class
     * @return string line with package meta information
     */
    public String parsePackage(Class<?> clazz, ParseContext context) {
        if (isShouldBeDisplayed(clazz, context)) {
            Package classPackage = clazz.getPackage();
            String packageAnnotations = annotationParser.parseAnnotationsAsBlock(classPackage, context);
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
    private boolean isShouldBeDisplayed(Class<?> clazz, ParseContext context) {
        return clazz.getPackage() != null && context.isBasedParsedClass(clazz);
    }
}