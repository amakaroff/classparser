package com.classparser.reflection.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class store current context for parsing class process
 *
 * @author Aleksey Makarov
 * @since 1.0.0
 */
public final class ReflectionParserManager {

    private final ThreadLocal<Class<?>> threadLocalParsedClass;

    private final ThreadLocal<Class<?>> threadLocalCurrentParsedClass;

    private final ConfigurationManager configurationManager;

    public ReflectionParserManager() {
        this.configurationManager = new ConfigurationManager();
        this.threadLocalParsedClass = new ThreadLocal<>();
        this.threadLocalCurrentParsedClass = new ThreadLocal<>();
    }

    /**
     * Obtains current based parsed class
     *
     * @return based parsed class or null if parsing is not started
     */
    public Class<?> getBaseParsedClass() {
        return threadLocalParsedClass.get();
    }

    /**
     * Sets based parsed class for current context
     *
     * @param parsedClass based parsed class
     */
    public void setBaseParsedClass(Class<?> parsedClass) {
        threadLocalParsedClass.set(parsedClass);
    }

    /**
     * Obtains current parsed class
     *
     * @return parsed class or null if parsing is not started
     */
    public Class<?> getCurrentParsedClass() {
        return threadLocalCurrentParsedClass.get();
    }

    /**
     * Sets parsed class for current context
     *
     * @param currentParsedClass parsed class
     */
    public void setCurrentParsedClass(Class<?> currentParsedClass) {
        this.threadLocalCurrentParsedClass.set(currentParsedClass);
    }

    /**
     * Obtains current configuration manager.
     * Configuration manager is not tied to the context
     *
     * @return {@link ConfigurationManager} object
     */
    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    /**
     * Pops current parsed class and set to current context declaring class
     */
    public void popCurrentClass() {
        if (getCurrentParsedClass() != null) {
            setCurrentParsedClass(getCurrentParsedClass().getDeclaringClass());
        }
    }

    /**
     * Clears state for current parsed context
     */
    public void clearState() {
        threadLocalCurrentParsedClass.remove();
        threadLocalParsedClass.remove();
    }

    /**
     * Join strings by one above and one under line separator
     *
     * @param content list of contents
     * @return joined strings
     */
    public String joinContentByLineSeparator(List<String> content) {
        if (!content.isEmpty()) {
            String lineSeparator = configurationManager.getLineSeparator();
            return String.join(lineSeparator + lineSeparator, content) + lineSeparator;
        }

        return "";
    }

    /**
     * Performs the join of non-empty class content by space
     *
     * @param content class content
     * @return joined class content
     */
    public String joinNotEmptyContentBySpace(String... content) {
        return Arrays.stream(content).filter(s -> !s.isEmpty()).collect(Collectors.joining(" "));
    }
}