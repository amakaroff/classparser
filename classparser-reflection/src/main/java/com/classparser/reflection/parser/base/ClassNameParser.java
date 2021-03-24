package com.classparser.reflection.parser.base;

import com.classparser.reflection.ParseContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
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

    public ClassNameParser(ImportParser importParser) {
        this.importParser = importParser;
    }

    /**
     * Parses name of class and stores it to current import context
     * Returns full or simple name in depend on {@link ImportParser#tryAddToImport(Class, ParseContext)} return value
     *
     * @param clazz any class
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
     * Parse annotation name uses context of class for parsing with all inner class names
     *
     * @param annotation annotation class
     * @return parsed annotation name
     */
    String parseAnnotationName(Class<? extends Annotation> annotation, boolean isAboveClass, ParseContext context) {
        String annotationName = parseTypeName(annotation, context);
        if (isNeedFullAnnotationNameForInnerClass(annotation, isAboveClass, context)) {
            String packageName = "";
            if (annotationName.contains(".")) {
                int simpleNameSeparator = annotationName.lastIndexOf('.');

                packageName = annotationName.substring(0, simpleNameSeparator) + ".";
                annotationName = annotationName.substring(simpleNameSeparator + 1);
            }

            Class<?> declaringClass = annotation.getDeclaringClass();
            if (declaringClass != null) {
                StringBuilder annotationNameBuilder = new StringBuilder(annotationName);
                do {
                    annotationNameBuilder.insert(0, getSimpleName(declaringClass) + ".");
                    declaringClass = declaringClass.getDeclaringClass();
                } while (declaringClass != null && isNeedFullAnnotationNameForInnerClass(declaringClass, isAboveClass, context));

                return packageName + annotationNameBuilder.toString();
            }

            return packageName + annotationName;
        }

        return annotationName;
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
     * @param clazz any class
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
     * Checks is needed name for inner class
     *
     * @param innerClass any class
     * @return true if name needed for inner class
     */
    boolean isNeedNameForInnerClass(Class<?> innerClass, ParseContext context) {
        return innerClass.isMemberClass()
                && (!getTopClass(innerClass).equals(getTopClass(context.getBaseParsedClass()))
                || !isInVisibilityZone(innerClass, context));
    }

    /**
     * Checks if necessary annotation full name for inner class
     *
     * @param declaringClass any class in hierarchy to top class of annotation
     * @param isAboveClass   is annotation set on class or not
     * @return true if annotation should have full name
     */
    private boolean isNeedFullAnnotationNameForInnerClass(Class<?> declaringClass,
                                                          boolean isAboveClass,
                                                          ParseContext context) {
        return declaringClass.isMemberClass() &&
                !getTopClass(declaringClass).equals(getTopClass(context.getBaseParsedClass())) ||
                !isAnnotationVisibilityZone(declaringClass, isAboveClass, context);
    }

    /**
     * Checks is class exists in visibility zone for current parsed class
     * For example:
     *    Top class
     * 1 /         \ 2
     *  /\         /\
     * 3 4        5 6
     * <p>Class 3 in visibility zone for class 1 and not requires full name</p>
     * <p>
     * Class 4 and 5 in not visibility zone and if we used class 4 in 5 then we should
     * add enclosing class name
     * </p>
     *
     * @param innerClass any class
     * @return true if class in visibility zone for current parsed class
     */
    private boolean isInVisibilityZone(Class<?> innerClass, ParseContext context) {
        Class<?> currentClass = context.getCurrentParsedClass();
        while (currentClass != null) {
            List<Class<?>> innerClasses = Arrays.asList(currentClass.getDeclaredClasses());
            if (innerClasses.contains(innerClass)) {
                return true;
            }

            currentClass = currentClass.getDeclaringClass();
        }

        return false;
    }

    /**
     * Checks if annotation in visibility of current zone for displaying full name
     *
     * @param declaringClass any class in hierarchy to top class of annotation
     * @param isAboveClass   is annotation set on class or not
     * @return true if annotation in visibility zone and full name is not necessary
     */
    private boolean isAnnotationVisibilityZone(Class<?> declaringClass, boolean isAboveClass, ParseContext context) {
        Class<?> currentClass = context.getCurrentParsedClass();

        if (Arrays.asList(declaringClass.getDeclaredClasses()).contains(currentClass)) {
            return true;
        } else {
            if (declaringClass == currentClass) {
                if (currentClass.isAnnotation()) {
                    return true;
                } else {
                    return !isAboveClass;
                }
            } else {
                Class<?> outerClass = declaringClass.getDeclaringClass();
                if (outerClass != null) {
                    if (Arrays.asList(outerClass.getDeclaredClasses()).contains(currentClass)) {
                        if (isInInnerClassHierarchyDown(outerClass, currentClass)) {
                            return !isAboveClass;
                        } else {
                            return false;
                        }
                    }
                }
            }

            return isInInnerClassHierarchyDown(currentClass, declaringClass);
        }
    }

    private boolean isInInnerClassHierarchyDown(Class<?> sourceClass, Class<?> stopClass) {
        if (sourceClass != null) {
            if (sourceClass == stopClass) {
                return true;
            } else {
                return isInInnerClassHierarchyDown(sourceClass.getDeclaringClass(), stopClass);
            }
        } else {
            return false;
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
}