package com.classparser.reflection.parser.structure;

import com.classparser.reflection.ParseContext;
import com.classparser.reflection.ContentJoiner;
import com.classparser.reflection.ReflectionParser;
import com.classparser.reflection.configuration.ConfigurationManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Class provides functionality for parsing meta information about inner and nested classes
 *
 * @author Aleksey Makarov
 * @author Valim Kiselev
 * @since 1.0.0
 */
public class ClassesParser {

    private final ReflectionParser classParser;

    private final ConfigurationManager configurationManager;

    public ClassesParser(ReflectionParser classParser, ConfigurationManager configurationManager) {
        this.classParser = classParser;
        this.configurationManager = configurationManager;
    }

    /**
     * Parses inner and nested class meta information and collects info to {@link String}
     *
     * @param clazz any class
     * @return string line of meta information for inner and nested classes
     */
    public String parseInnerClasses(Class<?> clazz, ParseContext context) {
        if (configurationManager.isDisplayInnerClasses()) {
            List<String> classes = new ArrayList<>();

            for (Class<?> declaredClass : clazz.getDeclaredClasses()) {
                if (isShouldBeDisplayed(clazz)) {
                    classes.add(classParser.parseClass(declaredClass, context));
                }
            }

            return ContentJoiner.joinContent(classes, configurationManager.getLineSeparator());
        }

        return "";
    }

    /**
     * Checks if class displaying is necessary
     *
     * @param clazz any class
     * @return true if class display is needed
     */
    private boolean isShouldBeDisplayed(Class<?> clazz) {
        return configurationManager.isDisplaySyntheticEntities() || !clazz.isSynthetic();
    }
}