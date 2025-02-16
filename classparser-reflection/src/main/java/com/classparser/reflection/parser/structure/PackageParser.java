package com.classparser.reflection.parser.structure;

import com.classparser.reflection.ParseContext;
import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.parser.base.AnnotationParser;
import com.classparser.reflection.parser.base.ClassNameParser;

/**
 * Class provides functionality for parsing package meta information
 *
 * @author Aleksey Makarov
 * @author Vadim Kiselev
 * @since 1.0.0
 */
public class PackageParser {

    private final AnnotationParser annotationParser;

    private final ClassNameParser classNameParser;

    private final ConfigurationManager configurationManager;

    public PackageParser(ConfigurationManager configurationManager) {
        this.annotationParser = new AnnotationParser(configurationManager);
        this.classNameParser = new ClassNameParser(configurationManager);
        this.configurationManager = configurationManager;
    }

    /**
     * Parses package meta information of given class
     * Includes package annotation from special {package-info} classes
     *
     * @param context context of parsing class process
     * @return string line with package meta information
     */
    public String parsePackage(ParseContext context) {
        if (isShouldBeDisplayed(context)) {
            Class<?> clazz = context.getCurrentParsedClass();

            Package classPackage = clazz.getPackage();
            String packageAnnotations = annotationParser.parseAnnotationsAsBlock(classPackage, context);
            String lineSeparator = configurationManager.getLineSeparator();

            if (configurationManager.isDisplayPackageInfoAsClass() || !classNameParser.isPackageInfo(clazz)) {
                return "package " + classPackage.getName() + ';' + lineSeparator + lineSeparator;
            } else {
                return packageAnnotations + "package " + classPackage.getName() + ';' + lineSeparator;
            }
        }

        return "";
    }

    /**
     * Checks if displaying package section for class is necessary
     *
     * @param context context of parsing class process
     * @return true if package section should be displayed
     */
    private boolean isShouldBeDisplayed(ParseContext context) {
        Class<?> clazz = context.getCurrentParsedClass();
        return clazz.getPackage() != null && context.isBasedParsedClass(clazz);
    }
}