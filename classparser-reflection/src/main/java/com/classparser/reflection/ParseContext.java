package com.classparser.reflection;

import java.util.HashSet;
import java.util.Set;

public class ParseContext {

    private final Class<?> baseClass;

    private final Set<Class<?>> importClasses;

    private Class<?> currentParsedClass;

    public ParseContext(Class<?> baseClass) {
        this.baseClass = baseClass;
        this.currentParsedClass = baseClass;
        this.importClasses = getAllInnerAndNestedClasses(baseClass);
    }

    public void setCurrentParsedClass(Class<?> currentParsedClass) {
        this.currentParsedClass = currentParsedClass;
    }

    public Class<?> getCurrentParsedClass() {
        return currentParsedClass;
    }

    public void addImportClass(Class<?> importedClass) {
        this.importClasses.add(importedClass);
    }

    public Set<Class<?>> getImportClasses() {
        return importClasses;
    }

    public void popCurrentClass() {
        Class<?> declaringClass = getCurrentParsedClass().getDeclaringClass();
        if (declaringClass != null) {
            setCurrentParsedClass(declaringClass);
        }
    }

    public boolean isBasedParsedClass(Class<?> clazz) {
        return getBaseParsedClass().equals(clazz);
    }

    public boolean isCurrentParsedClass(Class<?> clazz) {
        return getCurrentParsedClass().equals(clazz);
    }

    public Class<?> getBaseParsedClass() {
        return baseClass;
    }

    /**
     * Loads all inner and nested classes to set for any class
     *
     * @param clazz any class
     * @return set of all inner and nested classes
     */
    private Set<Class<?>> getAllInnerAndNestedClasses(Class<?> clazz) {
        Set<Class<?>> classes = new HashSet<>();

        classes.add(clazz);
        for (Class<?> innerClass : clazz.getDeclaredClasses()) {
            classes.addAll(getAllInnerAndNestedClasses(innerClass));
        }

        return classes;
    }

}
