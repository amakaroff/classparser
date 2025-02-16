package com.classparser.reflection.parser;

import com.classparser.reflection.ParseContext;

/**
 * Class provides functionality to obtain information about class type
 * <ul>
 *      <li>class</li>
 *      <li>enum</li>
 *      <li>@interface</li>
 *      <li>interface</li>
 *      <li>primitive</li>
 *      <li>record</li>
 *      <li>array</li>
 * </ul>
 *
 * @author Aleksey Makarov
 * @author Vadim Kiselev
 * @since 1.0.0
 */
public class ClassTypeParser {

    /**
     * Parses meta information about class type
     *
     * @param context - parsing context
     * @return parsed type of class
     */
    public String parseClassType(ParseContext context) {
        Class<?> clazz = context.getCurrentParsedClass();
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
        } if (clazz.isRecord()) {
            type = "record";
        }

        return type;
    }
}