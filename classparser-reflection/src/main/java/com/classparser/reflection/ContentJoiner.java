package com.classparser.reflection;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ContentJoiner {

    /**
     * Join strings by one above and one under line separator
     *
     * @param content list of contents
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
        return Arrays.stream(content).filter(s -> !s.isEmpty()).collect(Collectors.joining(" "));
    }
}
