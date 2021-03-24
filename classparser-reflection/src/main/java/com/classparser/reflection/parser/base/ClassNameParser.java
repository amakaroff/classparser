package com.classparser.reflection.parser.base;

import com.classparser.reflection.ParseContext;

import java.lang.reflect.Member;

/**
 * Class provides functionality for parsing class names
 *
 * @author Aleksey Makarov
 * @author Valim Kiselev
 * @since 1.0.0
 */
public class ClassNameParser {

    private final ImportParser importParser;

    public ClassNameParser(ImportParser importParser) {
        this.importParser = importParser;
    }

    /**
     * Parses name of class and stores it to current import context
     * Returns full or simple name in depend on {@link ImportParser#addToImportSection(Class, ParseContext)} return value
     *
     * @param clazz any class
     * @return parsed name of class
     */
    public String parseTypeName(Class<?> clazz, ParseContext context) {
        return importParser.addToImportSection(clazz, context) ? getSimpleName(clazz) : getName(clazz, context);
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
    private String getName(Class<?> clazz, ParseContext context) {
        if (clazz.isMemberClass() || context.isCurrentParsedClass(clazz)) {
            return getSimpleName(clazz);
        } else {
            return clazz.getName();
        }
    }
}