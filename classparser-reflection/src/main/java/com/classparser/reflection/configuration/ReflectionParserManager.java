package com.classparser.reflection.configuration;

import com.classparser.reflection.configuration.api.Clearance;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class store current context for parsing class process
 *
 * @author Aleksey Makarov
 * @since 1.0.0
 */
public final class ReflectionParserManager implements Clearance {

    private final ThreadLocal<ClassContextContainer> contextContainerThreadLocal;

    private final ConfigurationManager configurationManager;

    public ReflectionParserManager() {
        this.configurationManager = new ConfigurationManager();
        this.contextContainerThreadLocal = ThreadLocal.withInitial(ClassContextContainer::new);
    }

    /**
     * Obtains current based parsed class
     *
     * @return based parsed class or null if parsing is not started
     */
    public Class<?> getBaseParsedClass() {
        return contextContainerThreadLocal.get().getBaseParsedClass();
    }

    /**
     * Sets based parsed class for current context
     *
     * @param parsedClass based parsed class
     */
    public void setBaseParsedClass(Class<?> parsedClass) {
        contextContainerThreadLocal.get().setBaseParsedClass(parsedClass);
    }

    /**
     * Obtains current parsed class
     *
     * @return parsed class or null if parsing is not started
     */
    public Class<?> getCurrentParsedClass() {
        return contextContainerThreadLocal.get().getCurrentParsedClass();
    }

    /**
     * Sets parsed class for current context
     *
     * @param currentParsedClass parsed class
     */
    public void setCurrentParsedClass(Class<?> currentParsedClass) {
        contextContainerThreadLocal.get().setCurrentParsedClass(currentParsedClass);
    }

    /**
     * Obtains current configuration manager
     * Configuration manager is not tied to context
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
    @Override
    public void clear() {
        contextContainerThreadLocal.remove();
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
     * Performs join non empty class content by space
     *
     * @param content class content
     * @return joined class content
     */
    public String joinNotEmptyContentBySpace(String... content) {
        return Arrays.stream(content).filter(s -> !s.isEmpty()).collect(Collectors.joining(" "));
    }

    private class ClassContextContainer {

        private Class<?> baseParsedClass;

        private Class<?> currentParsedClass;

        Class<?> getBaseParsedClass() {
            return baseParsedClass;
        }

        void setBaseParsedClass(Class<?> baseParsedClass) {
            this.baseParsedClass = baseParsedClass;
        }

        Class<?> getCurrentParsedClass() {
            return currentParsedClass;
        }

        void setCurrentParsedClass(Class<?> currentParsedClass) {
            this.currentParsedClass = currentParsedClass;
        }
    }
}