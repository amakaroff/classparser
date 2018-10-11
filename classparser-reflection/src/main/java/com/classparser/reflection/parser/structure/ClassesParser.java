package com.classparser.reflection.parser.structure;

import com.classparser.api.ClassParser;
import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.configuration.ReflectionParserManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Class provides functionality for parsing meta information about inner and nested classes
 * This class depends on parsing context {@link ReflectionParserManager}
 *
 * @author Aleksey Makarov
 * @author Vadim Kiselev
 * @since 1.0.0
 */
public class ClassesParser {

    private final ClassParser classParser;

    private final ReflectionParserManager manager;

    private final ConfigurationManager configurationManager;

    public ClassesParser(ClassParser classParser, ReflectionParserManager manager) {
        this.classParser = classParser;
        this.manager = manager;
        this.configurationManager = manager.getConfigurationManager();
    }

    /**
     * Parses inner and nested class meta information and collects info to {@link String}
     *
     * @param clazz any class
     * @return string line of meta information for inner and nested classes
     */
    public String parseInnerClasses(Class<?> clazz) {
        if (configurationManager.isDisplayInnerClasses()) {
            List<String> classes = new ArrayList<>();

            for (Class<?> declaredClass : clazz.getDeclaredClasses()) {
                if (isShouldBeDisplayed(clazz)) {
                    classes.add(classParser.parseClass(declaredClass));
                }
            }

            return manager.joinContentByLineSeparator(classes);
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