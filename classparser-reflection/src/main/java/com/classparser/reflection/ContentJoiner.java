package com.classparser.reflection;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            return String.join(lineSeparator + lineSeparator, content) + lineSeparator;
        }

        return "";
    }

    /**
     * Performs the join of non-empty class content by space
     *
     * @param content class content
     * @return joined class content
     */
    public static String joinNotEmptyContentBySpace(String... content) {
        return joinNotEmpty(" ", content);
    }

    /**
     * Performs the join of non-empty class content by space
     *
     * @param separator separator for content
     * @param content   class content
     * @return joined class content
     */
    public static String joinNotEmpty(String separator, String... content) {
        return Stream.of(content).filter(s -> !s.isEmpty()).collect(Collectors.joining(separator));
    }
}
