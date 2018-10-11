package com.classparser.reflection.parser.base;

import com.classparser.reflection.configuration.ReflectionParserManager;
import com.classparser.reflection.parser.imports.ImportParser;

import java.lang.reflect.Member;

/**
 * Class provides functionality for parsing class names
 * This class depends on context {@link ReflectionParserManager} of parsing
 *
 * @author Aleksey Makarov
 * @author Vadim Kiselev
 * @since 1.0.0
 */
public class ClassNameParser {

    private final ImportParser importParser;

    private final ReflectionParserManager manager;

    public ClassNameParser(ImportParser importParser, ReflectionParserManager manager) {
        this.importParser = importParser;
        this.manager = manager;
    }

    /**
     * Parses name of class and stores it to current import context
     * Returns full or simple name in depend on {@link ImportParser#addToImportSection(Class)} return value
     *
     * @param clazz any class
     * @return parsed name of class
     */
    public String parseTypeName(Class<?> clazz) {
        return importParser.addToImportSection(clazz) ? getSimpleName(clazz) : getName(clazz);
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
}