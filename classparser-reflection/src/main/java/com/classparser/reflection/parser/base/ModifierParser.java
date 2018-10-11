package com.classparser.reflection.parser.base;

import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.configuration.ReflectionParserManager;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class provides functionality by resolving masks with modifiers information
 *
 * @author Aleksey Makarov
 * @author Vadim Kiselev
 * @since 1.0.0
 */
public class ModifierParser {

    private static final int SYNTHETIC = 0x00001000;

    private static final int IMPLICIT = 0x00008000;

    private final ConfigurationManager configurationManager;

    public ModifierParser(ReflectionParserManager manager) {
        this.configurationManager = manager.getConfigurationManager();
    }

    /**
     * Parses modifiers for class
     *
     * @param clazz any class
     * @return parsed modifiers
     */
    public String parseModifiers(Class<?> clazz) {
        List<String> modifiers = new ArrayList<>();

        int modifierMask = clazz.getModifiers();

        if (Modifier.isPublic(modifierMask)) {
            if (isDisplayExhaustiveModifiers() || !isInnerClassInAnnotation(clazz)) {
                modifiers.add("public");
            }
        }

        if (Modifier.isProtected(modifierMask)) {
            modifiers.add("protected");
        }

        if (Modifier.isPrivate(modifierMask)) {
            modifiers.add("private");
        }

        if (isSynthetic(modifierMask)) {
            modifiers.add("synthetic");
        }

        if (Modifier.isAbstract(modifierMask)) {
            if (isDisplayExhaustiveModifiers() || !isEnumOrInterface(clazz)) {
                modifiers.add("abstract");
            }
        }

        if (Modifier.isStatic(modifierMask)) {
            if (isDisplayExhaustiveModifiers() || !isOnlyStaticInnerClass(clazz)) {
                modifiers.add("static");
            }
        }

        if (Modifier.isStrict(modifierMask)) {
            modifiers.add("strictfp ");
        }

        return String.join(" ", modifiers);
    }

    /**
     * Parses modifiers for parameter
     *
     * @param parameter any parameter
     * @return parsed modifiers
     */
    public String parseModifiers(Parameter parameter) {
        List<String> modifiers = new ArrayList<>();

        int modifierMask = parameter.getModifiers();

        if (isSynthetic(modifierMask)) {
            modifiers.add("synthetic");
        }

        if (isImplicit(modifierMask)) {
            modifiers.add("implicit");
        }

        if (Modifier.isFinal(modifierMask)) {
            modifiers.add("final");
        }

        return String.join(" ", modifiers);
    }

    /**
     * Parses modifiers for constructor
     *
     * @param constructor any constructor
     * @return parsed modifiers
     */
    public String parseModifiers(Constructor<?> constructor) {
        List<String> modifiers = new ArrayList<>();

        int modifierMask = constructor.getModifiers();

        if (Modifier.isPublic(modifierMask)) {
            modifiers.add("public");
        }

        if (Modifier.isProtected(modifierMask)) {
            modifiers.add("protected");
        }

        if (Modifier.isPrivate(modifierMask)) {
            if (isDisplayExhaustiveModifiers() || !constructor.getDeclaringClass().isEnum()) {
                modifiers.add("private");
            }
        }

        return String.join(" ", modifiers);
    }

    /**
     * Parses modifiers for field
     *
     * @param field any field
     * @return parsed modifiers
     */
    public String parseModifiers(Field field) {
        List<String> modifiers = new ArrayList<>();

        int modifierMask = field.getModifiers();

        if (Modifier.isPublic(modifierMask)) {
            modifiers.add("public");
        }

        if (Modifier.isProtected(modifierMask)) {
            modifiers.add("protected");
        }

        if (Modifier.isPrivate(modifierMask)) {
            modifiers.add("private");
        }

        if (isSynthetic(modifierMask)) {
            modifiers.add("synthetic");
        }

        if (Modifier.isVolatile(modifierMask)) {
            modifiers.add("volatile");
        }

        if (Modifier.isTransient(modifierMask)) {
            modifiers.add("transient");
        }

        if (Modifier.isStatic(modifierMask)) {
            modifiers.add("static");
        }

        if (Modifier.isFinal(modifierMask)) {
            modifiers.add("final");
        }

        return String.join(" ", modifiers);
    }

    /**
     * Parses modifiers for method
     *
     * @param method any method
     * @return parsed modifiers
     */
    public String parseModifiers(Method method) {
        List<String> modifiers = new ArrayList<>();

        int modifierMask = method.getModifiers();

        if (Modifier.isPublic(modifierMask)) {
            if (isDisplayExhaustiveModifiers() || !method.getDeclaringClass().isInterface()) {
                modifiers.add("public");
            }
        }

        if (Modifier.isProtected(modifierMask)) {
            modifiers.add("protected");
        }

        if (Modifier.isPrivate(modifierMask)) {
            modifiers.add("private");
        }

        if (Modifier.isAbstract(modifierMask)) {
            if (isDisplayExhaustiveModifiers() || !method.getDeclaringClass().isInterface()) {
                modifiers.add("abstract");
            }
        }

        if (method.isDefault()) {
            modifiers.add("default");
        }

        if (method.isBridge()) {
            modifiers.add("bridge");
        }

        if (Modifier.isSynchronized(modifierMask)) {
            modifiers.add("synchronized");
        }

        if (Modifier.isStrict(modifierMask)) {
            modifiers.add("strictfp");
        }

        if (Modifier.isStatic(modifierMask)) {
            modifiers.add("static");
        }

        if (Modifier.isNative(modifierMask)) {
            modifiers.add("native");
        }

        if (Modifier.isFinal(modifierMask)) {
            modifiers.add("final");
        }

        return String.join(" ", modifiers);
    }

    /**
     * Check is exists synthetic modifier
     *
     * @param modifierMask modifiers mask
     * @return true if in synthetic modifier exists in mask
     */
    public boolean isSynthetic(int modifierMask) {
        return (modifierMask & SYNTHETIC) != 0;
    }

    /**
     * Check is exists implicit modifier
     *
     * @param modifierMask modifiers mask
     * @return true if in implicit modifier exists in mask
     */
    public boolean isImplicit(int modifierMask) {
        return (modifierMask & IMPLICIT) != 0;
    }

    /**
     * Check is exists package private modifier
     *
     * @param modifierMask modifiers mask
     * @return true if in package private modifier exists in mask
     */
    public boolean isPackagePrivate(int modifierMask) {
        return !Modifier.isPublic(modifierMask) &&
                !Modifier.isProtected(modifierMask) &&
                !Modifier.isPrivate(modifierMask);
    }

    /**
     * Checks if class has member and interface, enum or annotation
     *
     * @param clazz any class
     * @return true if class is inner interface, enum or annotation
     */
    private boolean isOnlyStaticInnerClass(Class<?> clazz) {
        Class<?> enclosingClass = clazz.getEnclosingClass();
        return clazz.isMemberClass() && (clazz.isInterface() || clazz.isEnum() || enclosingClass.isAnnotation());
    }

    /**
     * Checks if class interface or enum
     *
     * @param clazz any class
     * @return true if class interface or enum
     */
    private boolean isEnumOrInterface(Class<?> clazz) {
        return clazz.isInterface() || clazz.isEnum();
    }

    /**
     * Checks if class has inner class in annotation class
     *
     * @param clazz any class
     * @return true if class has inner class in annotation class
     */
    private boolean isInnerClassInAnnotation(Class<?> clazz) {
        Class<?> enclosingClass = clazz.getEnclosingClass();
        return clazz.isMemberClass() && enclosingClass.isAnnotation();
    }

    /**
     * Checks is should be displayed exhaustive modifiers
     *
     * @return true if exhaustive modifiers should be displayed
     */
    private boolean isDisplayExhaustiveModifiers() {
        return !configurationManager.hideExhaustiveModifiers();
    }
}