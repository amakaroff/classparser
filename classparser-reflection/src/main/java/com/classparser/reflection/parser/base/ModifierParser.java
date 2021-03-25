package com.classparser.reflection.parser.base;

import com.classparser.reflection.configuration.ConfigurationManager;

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

    private static final int BRIDGE = 0x00000040;

    private final ConfigurationManager configurationManager;

    public ModifierParser(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
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
            if (configurationManager.isDisplayImplicitModifiers() || !isInnerClassInAnnotation(clazz)) {
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
            if (configurationManager.isDisplayImplicitModifiers() ||
                    !clazz.isInterface() && !clazz.isEnum() && !clazz.isArray() && !clazz.isPrimitive()) {
                modifiers.add("abstract");
            }
        }

        if (Modifier.isStatic(modifierMask)) {
            if (configurationManager.isDisplayImplicitModifiers() || !isOnlyStaticInnerClass(clazz)) {
                modifiers.add("static");
            }
        }

        if (Modifier.isFinal(modifierMask)) {
            if (configurationManager.isDisplayImplicitModifiers() ||
                    !clazz.isEnum() && !clazz.isArray() && !clazz.isPrimitive()) {
                modifiers.add("final");
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

        if (isSynthetic(parameter)) {
            modifiers.add("synthetic");
        }

        if (isImplicit(parameter)) {
            modifiers.add("implicit");
        }

        if (isFinal(parameter)) {
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
            if (configurationManager.isDisplayImplicitModifiers() || !constructor.getDeclaringClass().isEnum()) {
                modifiers.add("private");
            }
        }

        if (isSynthetic(modifierMask)) {
            modifiers.add("synthetic");
        }

        return String.join(" ", modifiers);
    }

    public String parseFieldModifiers(int modifierMask, Class<?> declaredClass) {
        List<String> modifiers = new ArrayList<>();

        if (Modifier.isPublic(modifierMask)) {
            if (configurationManager.isDisplayImplicitModifiers() || !declaredClass.isInterface()) {
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

        if (Modifier.isVolatile(modifierMask)) {
            modifiers.add("volatile");
        }

        if (Modifier.isTransient(modifierMask)) {
            modifiers.add("transient");
        }

        if (Modifier.isStatic(modifierMask)) {
            if (configurationManager.isDisplayImplicitModifiers() || !declaredClass.isInterface()) {
                modifiers.add("static");
            }
        }

        if (Modifier.isFinal(modifierMask)) {
            if (configurationManager.isDisplayImplicitModifiers() || !declaredClass.isEnum() && !declaredClass.isInterface()) {
                modifiers.add("final");
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
        return parseFieldModifiers(field.getModifiers(), field.getDeclaringClass());
    }

    public String parseMethodModifiers(int modifierMask, Class<?> declaringClass) {
        List<String> modifiers = new ArrayList<>();

        if (Modifier.isPublic(modifierMask)) {
            if (configurationManager.isDisplayImplicitModifiers() || !declaringClass.isInterface()) {
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
            if (configurationManager.isDisplayImplicitModifiers() || !declaringClass.isInterface()) {
                modifiers.add("abstract");
            }
        }

        if (isDefault(modifierMask, declaringClass)) {
            modifiers.add("default");
        }

        if (isBridge(modifierMask)) {
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
     * Parses modifiers for method
     *
     * @param method any method
     * @return parsed modifiers
     */
    public String parseModifiers(Method method) {
        return parseMethodModifiers(method.getModifiers(), method.getDeclaringClass());
    }

    /**
     * Checks if argument is synthetic
     *
     * @param parameter any argument
     * @return true if argument is synthetic or implicit
     */
    public boolean isSynthetic(Parameter parameter) {
        return parameter.isSynthetic() ||
                isImplicitInnerClassConstructorParameter(parameter) ||
                isImplicitEnumConstructorParameter(parameter);
    }

    /**
     * Checks if argument is implicit
     *
     * @param parameter any argument
     * @return true if argument is implicit
     */
    public boolean isImplicit(Parameter parameter) {
        return isImplicit(parameter.getModifiers()) || isImplicitInnerClassConstructorParameter(parameter);
    }

    /**
     * Checks if argument is final
     *
     * @param parameter any argument
     * @return true if argument is final
     */
    public boolean isFinal(Parameter parameter) {
        return Modifier.isFinal(parameter.getModifiers()) || isImplicitInnerClassConstructorParameter(parameter);
    }

    /**
     * Check is exists synthetic modifier
     *
     * @param modifierMask modifiers mask
     * @return true if in a synthetic modifier exists in mask
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
     * Check is method in declared class is default
     * Copied from java.lang.reflect.Method
     *
     * @param modifierMask modifiers mask
     * @param declaredClass class which exists method
     * @return true if modifier mask have default
     */
    public boolean isDefault(int modifierMask, Class<?> declaredClass) {
        return (modifierMask & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC)) == Modifier.PUBLIC &&
                declaredClass.isInterface();
    }

    /**
     * Check is method is bridge
     *
     * @param modifierMask modifiers mask
     * @return true if modifier have bridge mask
     */
    public boolean isBridge(int modifierMask) {
        return (modifierMask & BRIDGE) != 0;
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
     * @return true if class is the inner interface, enum or annotation
     */
    private boolean isOnlyStaticInnerClass(Class<?> clazz) {
        Class<?> enclosingClass = clazz.getEnclosingClass();
        return clazz.isMemberClass() && (clazz.isInterface() || clazz.isEnum() || enclosingClass.isAnnotation());
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
     * Checks if argument is first argument in nested class
     *
     * @param parameter any argument
     * @return true if argument is first in constructor for nested class
     */
    private boolean isImplicitInnerClassConstructorParameter(Parameter parameter) {
        Executable executable = parameter.getDeclaringExecutable();
        Class<?> clazz = executable.getDeclaringClass();
        return executable instanceof Constructor &&
                clazz.isMemberClass() &&
                !Modifier.isStatic(clazz.getModifiers()) &&
                getArgumentIndex(parameter) == 0;
    }

    /**
     * Checks first or second parameter in enum class
     *
     * @param parameter any parameter
     * @return true if parameter is first or second in constructor for enum
     */
    private boolean isImplicitEnumConstructorParameter(Parameter parameter) {
        Executable executable = parameter.getDeclaringExecutable();
        int index = getArgumentIndex(parameter);
        return (executable instanceof Constructor &&
                executable.getDeclaringClass().isEnum() &&
                (index == 0 || index == 1));
    }

    /**
     * Tries to get real parameter index
     * Watch at the future for existing getter for Parameter#index field
     *
     * @param parameter any parameter of executable
     * @return parameter index
     */
    private int getArgumentIndex(Parameter parameter) {
        Executable executable = parameter.getDeclaringExecutable();
        Parameter[] parameters = executable.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (parameter == parameters[i]) {
                return i;
            }
        }

        return -1;
    }
}