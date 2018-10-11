package com.classparser.reflection.parser;

/**
 * Class provides functionality to obtain information about class type
 * <ul>
 *      <li>class</li>
 *      <li>enum</li>
 *      <li>@interface</li>
 *      <li>interface</li>
 *      <li>primitive</li>
 *      <li>array</li>
 * </ul>
 *
 * @author Aleksey Makarov
 * @author Valim Kiselev
 * @since 1.0.0
 */
public class ClassTypeParser {

    /**
     * Parses meta information about class type
     *
     * @param clazz any class
     * @return parsed type of class
     */
    public String parseClassType(Class<?> clazz) {
        String type = "class";

        if (clazz.isEnum()) {
            type = "enum";
        } else if (clazz.isAnnotation()) {
            type = "@interface";
        } else if (clazz.isInterface()) {
            type = "interface";
        } else if (clazz.isPrimitive()) {
            type = "primitive";
        } else if (clazz.isArray()) {
            type = "array";
        }

        return type;
    }
}