package com.classparser.reflection.parser.base;

import com.classparser.reflection.configuration.ReflectionParserManager;
import com.classparser.reflection.configuration.api.Clearance;
import com.classparser.reflection.parser.imports.ImportParser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class provides functionality for parsing class names
 * This class depends on context {@link ReflectionParserManager} of parsing
 *
 * @author Aleksey Makarov
 * @author Vadim Kiselev
 * @since 1.0.0
 */
public class ClassNameParser implements Clearance {

    private final ImportParser importParser;

    private final ReflectionParserManager manager;

    private final ThreadLocal<Map<Class<?>, Set<Class<?>>>> threadLocalInnerClassCache;

    public ClassNameParser(ImportParser importParser, ReflectionParserManager manager) {
        this.importParser = importParser;
        this.manager = manager;
        this.threadLocalInnerClassCache = ThreadLocal.withInitial(HashMap::new);
    }

    /**
     * Parses name of class and stores it to current import context
     * Returns full or simple name in depend on {@link ImportParser#isCanUseSimpleName(Class)} return value
     *
     * @param clazz any class
     * @return parsed name of class
     */
    public String parseTypeName(Class<?> clazz) {
        if (importParser.isCanUseSimpleName(clazz)) {
            return getSimpleName(clazz);
        } else {
            return getName(clazz);
        }
    }

    /**
     * Parse annotation name uses context of class for parsing with all inner class names
     *
     * @param annotation annotation class
     * @return parsed annotation name
     */
    String parseAnnotationName(Class<? extends Annotation> annotation, boolean isAboveClass) {
        String annotationName = parseTypeName(annotation);
        if (isNeedAnnotationNameForInnerClass(annotation, isAboveClass)) {
            String packageName = "";
            if (annotationName.contains(".")) {
                int simpleNameSeparator = annotationName.lastIndexOf('.');

                packageName = annotationName.substring(0, simpleNameSeparator) + ".";
                annotationName = annotationName.substring(simpleNameSeparator + 1);
            }

            Class<?> declaringClass = annotation.getDeclaringClass();
            StringBuilder annotationNameBuilder = new StringBuilder(annotationName);
            do {
                annotationNameBuilder.insert(0, getSimpleName(declaringClass) + ".");
                declaringClass = declaringClass.getDeclaringClass();
            } while (isNeedAnnotationNameForInnerClass(declaringClass, isAboveClass));

            annotationNameBuilder.insert(0, getSimpleName(declaringClass) + ".");

            return packageName + annotationNameBuilder.toString();
        }

        return annotationName;
    }

    @Override
    public void clear() {
        threadLocalInnerClassCache.remove();
    }

    /**
     * Obtains class simple name
     *
     * @param clazz any class
     * @return parsed simple name of class
     */
    public String getSimpleName(Class<?> clazz) {
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
    private String getName(Class<?> clazz) {
        if (clazz.isMemberClass() || clazz == manager.getCurrentParsedClass()) {
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
    boolean isNeedNameForInnerClass(Class<?> innerClass) {
        Class<?> parsedClass = manager.getBaseParsedClass();
        return innerClass.isMemberClass()
                && (!getTopClass(innerClass).equals(getTopClass(parsedClass))
                || !isInVisibilityZone(innerClass));
    }

    /**
     * Checks if necessary annotation full name for inner class
     *
     * @param annotationClass any annotation
     * @param isAboveClass    is annotation set on class or not
     * @return true if annotation should have full name
     */
    private boolean isNeedAnnotationNameForInnerClass(Class<?> annotationClass, boolean isAboveClass) {
        return annotationClass != null &&
                annotationClass.isMemberClass() &&
                isIAnnotationVisibilityZone(annotationClass, isAboveClass);
    }

    /**
     * Checks is class exists in visibility zone for current parsed class
     * For example:
     * Top class
     * 1 /        \ 2
     * /\         /\
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
    private boolean isInVisibilityZone(Class<?> innerClass) {
        Class<?> currentClass = manager.getCurrentParsedClass();
        while (currentClass != null) {
            Set<Class<?>> innerClasses = getInnerClasses(currentClass);
            if (innerClasses.contains(innerClass)) {
                return true;
            }

            currentClass = currentClass.getDeclaringClass();
        }

        return false;
    }

    /**
     * Get collection of inner classes for any class.
     * If inner classes already exists in cache, then get from cache
     *
     * @param clazz any class
     * @return collection of inner classes
     */
    private Set<Class<?>> getInnerClasses(Class<?> clazz) {
        Map<Class<?>, Set<Class<?>>> cache = threadLocalInnerClassCache.get();
        Set<Class<?>> innerClasses = cache.get(clazz);

        if (innerClasses == null) {
            innerClasses = new HashSet<>(Arrays.asList(clazz.getDeclaredClasses()));
            cache.put(clazz, innerClasses);
        }

        return innerClasses;
    }

    /**
     * Checks if annotation in visibility of current zone for displaying full name
     *
     * @param annotationClass any annotation class
     * @param isAboveClass    is annotation set on class or not
     * @return true if annotation in visibility zone and full name is not necessary
     */
    private boolean isIAnnotationVisibilityZone(Class<?> annotationClass, boolean isAboveClass) {
        if (getTopClass(annotationClass) == getTopClass(manager.getBaseParsedClass())) {
            //Holy shit happens
            //I don't know what i write here
        }

        return true;
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