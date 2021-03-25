package com.classparser.reflection.parser.base;

import com.classparser.reflection.ParseContext;
import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.parser.structure.ImportParser;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

/**
 * Class provides functionality for parsing class names
 *
 * @author Aleksey Makarov
 * @author Vadim Kiselev
 * @since 1.0.0
 */
public class ClassNameParser {

    private final ImportParser importParser;

    public ClassNameParser(ConfigurationManager configurationManager) {
        this.importParser = new ImportParser(configurationManager);
    }

    /**
     * Parses name of class and stores it to current import context
     * Returns full or simple name in depend on {@link ImportParser#tryAddToImport(Class, ParseContext)} return value
     *
     * @param clazz   any class
     * @param context context of parsing class process
     * @return parsed name of class
     */
    public String parseTypeName(Class<?> clazz, ParseContext context) {
        if (importParser.tryAddToImport(clazz, context)) {
            return getSimpleName(clazz);
        } else {
            return getName(clazz, context);
        }
    }

    /**
     * Parses class name with correctly resolving inner and nested classes
     *
     * @param classLink     any class link in class
     * @param isInsideClass flag if class link contains inside class block
     * @param context       context of parsing class process
     * @return parsed class name
     */
    public String parseClassName(Class<?> classLink, boolean isInsideClass, ParseContext context) {
        String className = parseTypeName(classLink, context);

        if (isFullNameRequired(classLink, isInsideClass, context)) {
            String packageName = "";
            if (className.contains(".")) {
                int simpleNameSeparator = className.lastIndexOf('.');

                packageName = className.substring(0, simpleNameSeparator) + ".";
                className = className.substring(simpleNameSeparator + 1);
            }

            Class<?> declaringClass = classLink.getDeclaringClass();
            if (declaringClass != null) {
                StringBuilder annotationNameBuilder = new StringBuilder(className);
                do {
                    annotationNameBuilder.insert(0, getSimpleName(declaringClass) + ".");
                    declaringClass = declaringClass.getDeclaringClass();
                } while (declaringClass != null && isFullNameRequired(declaringClass, isInsideClass, context));

                return packageName + annotationNameBuilder.toString();
            }

            return packageName + className;
        }

        return className;
    }

    /**
     * Tries understand is class name requires class name
     *
     * @param classLink     any class link
     * @param isInsideClass flag if class link contains inside class block
     * @param context       context of parsing class process
     * @return true if class requires full name
     */
    boolean isFullNameRequired(Class<?> classLink, boolean isInsideClass, ParseContext context) {
        if (classLink.isMemberClass()) {
            return isNeedNameForInnerClass(classLink, isInsideClass, context);
        } else if (classLink == context.getCurrentParsedClass()) {
            return !isInsideClass;
        } else {
            return false;
        }
    }

    /**
     * Is type for any member is inner class and contained in static context
     *
     * @param member any member like field or method
     * @param type   type related with that member
     * @return true if inner class is in static context
     */
    public boolean isInnerClassInStaticContext(Member member, Class<?> type) {
        return !Modifier.isStatic(member.getModifiers()) ||
                !type.isMemberClass() ||
                Modifier.isStatic(type.getModifiers()) ||
                !isHaveGenericInterfaces(type);
    }

    /**
     * Is class or own super classes have generics
     *
     * @param clazz any class
     * @return true if generic founds
     */
    private boolean isHaveGenericInterfaces(Class<?> clazz) {
        if (clazz != null) {
            if (clazz.getTypeParameters().length != 0) {
                return true;
            } else {
                return isHaveGenericInterfaces(clazz.getDeclaringClass());
            }
        } else {
            return false;
        }
    }

    /**
     * Checks is class exists in visibility zone for current parsed class
     * For example:
     * Top class
     * 1 /         \ 2
     * /\         /\
     * 3 4        5 6
     * <p>Class 3 in visibility zone for class 1 and not requires full name</p>
     * <p>
     * Class 4 and 5 in not visibility zone and if we used class 4 in 5 then we should
     * add enclosing class name
     * </p>
     *
     * @param classLink     any class
     * @param isInsideClass is class link contained inside class block
     * @param context       context of parsing class process
     * @return true if class in visibility zone for current parsed class
     */
    private boolean isInVisibilityZone(Class<?> classLink, boolean isInsideClass, ParseContext context) {
        Class<?> currentClass = context.getCurrentParsedClass();

        while (currentClass != null) {
            List<Class<?>> innerClasses = Arrays.asList(currentClass.getDeclaredClasses());

            if (innerClasses.contains(classLink) && isInsideClass) {
                return true;
            }

            isInsideClass = true;
            currentClass = currentClass.getDeclaringClass();
        }

        return false;
    }

    /**
     * Checks is needed name for inner class
     *
     * @param innerClass any class
     * @param context    context of parsing class process
     * @return true if name needed for inner class
     */
    boolean isNeedNameForInnerClass(Class<?> innerClass, boolean isInsideClass, ParseContext context) {
        return innerClass.isMemberClass()
                && (!getTopClass(innerClass).equals(getTopClass(context.getBaseParsedClass()))
                || !isInVisibilityZone(innerClass, isInsideClass, context));
    }

    /**
     * Obtains class simple name
     *
     * @param clazz any class
     * @return parsed simple name of class
     */
    String getSimpleName(Class<?> clazz) {
        String typeName = clazz.getSimpleName();
        if (typeName.isEmpty()) {
            typeName = clazz.getName();
            if (typeName.contains(".")) {
                typeName = typeName.substring(typeName.lastIndexOf('.') + 1);
            }
        }
        return typeName;
    }

    /**
     * Obtains member name
     *
     * @param member any member
     * @return parsed member name
     */
    public String getMemberName(Member member) {
        return member.getName();
    }

    /**
     * Obtains name of class
     * Returns simple name for current parsing class or inner class
     *
     * @param clazz   any class
     * @param context context of parsing class process
     * @return parsed name of class
     */
    private String getName(Class<?> clazz, ParseContext context) {
        if (clazz.isMemberClass() || context.isCurrentParsedClass(clazz)) {
            return getSimpleName(clazz);
        } else {
            return clazz.getName();
        }
    }

    /**
     * Retrievals top declaring class
     *
     * @param innerClass any class
     * @return top declaring class
     */
    private Class<?> getTopClass(Class<?> innerClass) {
        return innerClass.getDeclaringClass() != null ? getTopClass(innerClass.getDeclaringClass()) : innerClass;
    }

    /**
     * Try load class by name of null otherwise
     *
     * @param className name of class
     * @return class instance or null
     */
    public Class<?> forNameOrNull(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            return null;
        }
    }
}