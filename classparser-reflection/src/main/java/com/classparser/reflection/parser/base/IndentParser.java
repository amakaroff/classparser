package com.classparser.reflection.parser.base;

import com.classparser.reflection.ParseContext;
import com.classparser.reflection.configuration.ConfigurationManager;

import java.lang.reflect.Member;

/**
 * Class provides functionality by counting indent spaces for class structure elements
 *
 * @author Aleksey Makarov
 * @author Vadim Kiselev
 * @since 1.0.0
 */
public class IndentParser {

    private final ConfigurationManager configurationManager;

    public IndentParser(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    /**
     * Resolving count of indent for any class structure element
     * It's may be field, method, constructor or inner class
     *
     * @param object any element of structure class
     * @return indent spaces
     */
    public String getIndent(Object object, ParseContext context) {
        StringBuilder indent = new StringBuilder();

        Class<?> declaringClass;
        if (object instanceof Member) {
            Member member = (Member) object;
            declaringClass = member.getDeclaringClass();

            if (declaringClass != null) {
                indent.append(configurationManager.getIndentSpaces());
            }
        } else if (object instanceof Class) {
            declaringClass = (Class<?>) object;
        } else {
            return "";
        }

        Class<?> parsedClass = context.getBaseParsedClass();
        if (declaringClass != null && !parsedClass.equals(declaringClass)) {
            while ((declaringClass = declaringClass.getDeclaringClass()) != null) {
                indent.append(configurationManager.getIndentSpaces());
            }
        }

        return indent.toString();
    }
}