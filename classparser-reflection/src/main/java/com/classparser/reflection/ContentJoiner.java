package com.classparser.reflection;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility class provides methods for join contents by any separator
 * <p>
 * Thread safe
 *
 * @author Aleksey Makarov
 * @since 1.0.0
 */
public class ContentJoiner {

    /**
     * Join strings by one above and one under line separator
     *
     * @param content       list of contents
     * @param lineSeparator separator for content
     * @return joined strings
     */
    public static String joinContent(List<String> content, String lineSeparator) {
        if (!content.isEmpty()) {
            return join(lineSeparator + lineSeparator, content) + lineSeparator;
        }

        return "";
    }

    public static String joinSpace(List<String> content) {
        return join(" ", content);
    }

    public static String joinSpace(String... content) {
        return join(" ", content);
    }

    public static String join(String separator, String... content) {
        return join(separator, Arrays.asList(content));
    }

    public static String join(String separator, List<String> content) {
        if (!content.isEmpty()) {
            return content.stream().filter(Predicate.not(String::isEmpty)).collect(Collectors.joining(separator));
        }

        return "";
    }

    public static String joinGenerics(List<String> generics) {
        if (!generics.isEmpty()) {
            return "<" + join(", ", generics) + ">";
        }

        return "";
    }

    public static String joinArguments(List<String> arguments) {
        if (!arguments.isEmpty()) {
            return "(" + join(", ", arguments) + ")";
        }

        return "";
    }
}
