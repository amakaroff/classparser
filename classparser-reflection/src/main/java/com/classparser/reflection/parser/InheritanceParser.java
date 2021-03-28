package com.classparser.reflection.parser;

import com.classparser.reflection.ContentJoiner;
import com.classparser.reflection.ParseContext;
import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.parser.base.GenericTypeParser;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Class provides functionality for parsing meta information
 * about inheritance and implemented interfaces for any classes
 *
 * @author Aleksey Makarov
 * @author Vadim Kiselev
 * @since 1.0.0
 */
public class InheritanceParser {

    private final GenericTypeParser genericTypeParser;

    private final ConfigurationManager configurationManager;

    public InheritanceParser(ConfigurationManager configurationManager) {
        this.genericTypeParser = new GenericTypeParser(configurationManager);
        this.configurationManager = configurationManager;
    }

    /**
     * Parses meta information about an inheritor and interfaces for class
     * <p>
     * For example:
     * <code>
     * extends Parent implements Interface
     * </code>
     *
     * @param clazz   any class
     * @param context context of parsing class process
     * @return parsed inheritances or empty string if inheritances is absent
     */
    public String parseInheritances(Class<?> clazz, ParseContext context) {
        return ContentJoiner.joinNotEmptyContentBySpace(parseSuperClass(clazz, context), parseInterfaces(clazz, context));
    }

    /**
     * Parses meta information about super class
     *
     * @param clazz   any class
     * @param context context of parsing class process
     * @return parsed super class
     */
    private String parseSuperClass(Class<?> clazz, ParseContext context) {
        if (isNecessaryDisplayedSuperClass(clazz)) {
            Type superClass = getSuperClassForClass(clazz);
            if (superClass != null) {
                String parsedSuperClass = genericTypeParser.parseType(getSuperClassForClass(clazz),
                        false,
                        clazz.getAnnotatedSuperclass(),
                        context);

                return "extends " + parsedSuperClass;
            }

        }

        return "";
    }

    /**
     * Parses meta information about implemented interfaces
     *
     * @param clazz   any class
     * @param context context of parsing class process
     * @return parsed interfaces
     */
    private String parseInterfaces(Class<?> clazz, ParseContext context) {
        if (isNecessaryDisplayedInterfaces(clazz)) {
            List<String> types = parseMultipleParentTypes(getInterfacesForClass(clazz),
                    clazz.getAnnotatedInterfaces(),
                    context);
            String interfaces = String.join(", ", types);
            String relationship = clazz.isInterface() ? "extends " : "implements ";

            if (!interfaces.isEmpty()) {
                return relationship + interfaces;
            }
        }

        return "";
    }

    /**
     * Parses the array of interface types to list of {@link String}
     * Also processes information about annotations for this interface types
     *
     * @param parentTypes    array of parent types
     * @param annotatedTypes array of annotated types for parent type
     * @param context        context of parsing class process
     * @return list of processed parent types for class
     */
    private List<String> parseMultipleParentTypes(Type[] parentTypes,
                                                  AnnotatedType[] annotatedTypes,
                                                  ParseContext context) {
        List<String> multipleParentTypes = new ArrayList<>();

        for (int index = 0; index < parentTypes.length; index++) {
            multipleParentTypes.add(genericTypeParser.parseType(parentTypes[index],
                    false,
                    ifEmpty(annotatedTypes, index),
                    context));
        }

        return multipleParentTypes;
    }

    /**
     * Checks if displaying of super class is necessary
     *
     * @param clazz any class
     * @return true if super class needs to be displayed
     */
    private boolean isNecessaryDisplayedSuperClass(Class<?> clazz) {
        return configurationManager.isDisplayDefaultInheritance() ||
                clazz.getSuperclass() != Object.class && !clazz.isEnum();
    }

    /**
     * Checks if displaying of super class is necessary
     *
     * @param clazz any class
     * @return true if super class needs to be displayed
     */
    private boolean isNecessaryDisplayedInterfaces(Class<?> clazz) {
        return configurationManager.isDisplayDefaultInheritance() || !clazz.isAnnotation();
    }

    /**
     * Obtain interfaces for class
     *
     * @param clazz any class
     * @return array of interfaces for class
     */
    private Type[] getInterfacesForClass(Class<?> clazz) {
        if (configurationManager.isDisplayGenericSignatures()) {
            return clazz.getGenericInterfaces();
        } else {
            return clazz.getInterfaces();
        }
    }

    /**
     * Obtains superclass for class
     *
     * @param clazz any class
     * @return superclass for class
     */
    private Type getSuperClassForClass(Class<?> clazz) {
        if (configurationManager.isDisplayGenericSignatures()) {
            return clazz.getGenericSuperclass();
        } else {
            return clazz.getSuperclass();
        }
    }

    /**
     * Checks if array is not empty and gets annotation from annotation array by index
     *
     * @param types annotation types array
     * @param index index for array
     * @return annotation by index or null if array is null or empty
     */
    private AnnotatedType ifEmpty(AnnotatedType[] types, int index) {
        return types == null || types.length == 0 ? null : types[index];
    }
}