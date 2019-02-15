package com.classparser.bytecode.utils;

import com.classparser.bytecode.configuration.ConfigurationManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class provides functionality by searching all inner classes for any class
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public class InnerClassesCollector {

    private final ConfigurationManager configurationManager;

    public InnerClassesCollector(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    /**
     * Collects inner/anonymous/local classes
     *
     * @param clazz any class
     * @return collection of all inner classes
     */
    public Collection<Class<?>> getInnerClasses(Class<?> clazz) {
        Set<Class<?>> classes = new HashSet<>();

        if (clazz != null) {
            if (!clazz.isArray() && !clazz.isPrimitive()) {
                classes.addAll(getAnonymousOrSyntheticClasses(clazz));
                classes.addAll(getInnerAndNestedClasses(clazz));
                classes.addAll(getLocalClasses(clazz));
            } else {
                String simpleName = ClassNameConverter.toJavaClassSimpleName(clazz);
                throw new IllegalArgumentException("Class " + simpleName + " can't be primitive or array!");
            }
        } else {
            throw new NullPointerException("Class can't be a null!");
        }

        return classes;
    }

    /**
     * Collects all anonymous and synthetic classes
     *
     * @param clazz any class
     * @return set of all anonymous classes
     */
    private Set<Class<?>> getAnonymousOrSyntheticClasses(Class<?> clazz) {
        Set<Class<?>> anonymousOrSyntheticClasses = new HashSet<>();

        int classId = 0;
        while (classId++ < 2 << 15) {
            try {
                String className = ClassNameConverter.toJavaClassName(clazz) + '$' + classId;
                Class<?> foundedClass = Class.forName(className);
                anonymousOrSyntheticClasses.add(foundedClass);
                anonymousOrSyntheticClasses.addAll(getInnerClasses(foundedClass));
            } catch (ClassNotFoundException | NoClassDefFoundError exception) {
                break;
            }
        }

        return anonymousOrSyntheticClasses;
    }

    /**
     * Collects all inner and nested classes
     *
     * @param clazz any class
     * @return set of all inner and nested classes
     */
    private Set<Class<?>> getInnerAndNestedClasses(Class<?> clazz) {
        Set<Class<?>> innerAndNestedClasses = new HashSet<>();

        for (Class<?> innerOrNestedClass : clazz.getDeclaredClasses()) {
            innerAndNestedClasses.add(innerOrNestedClass);
            innerAndNestedClasses.addAll(getInnerClasses(innerOrNestedClass));
        }

        return innerAndNestedClasses;
    }

    /**
     * Collects all local classes
     *
     * @param clazz any class
     * @return set of all local classes
     */
    private Set<Class<?>> getLocalClasses(Class<?> clazz) {
        Set<Class<?>> localClasses = new HashSet<>();

        ConstantPoolClassIterable classIterable = new ConstantPoolClassIterable(clazz, configurationManager);
        for (Class<?> constantClass : classIterable) {
            String constantClassName = ClassNameConverter.toJavaClassName(constantClass);
            String className = ClassNameConverter.toJavaClassName(clazz);
            if (constantClass.isLocalClass() && constantClass != clazz && constantClassName.startsWith(className)) {
                localClasses.add(constantClass);
                localClasses.addAll(getInnerClasses(constantClass));
            }
        }

        return localClasses;
    }
}