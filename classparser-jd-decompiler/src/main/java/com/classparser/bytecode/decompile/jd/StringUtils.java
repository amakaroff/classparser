package com.classparser.bytecode.decompile.jd;

/**
 * Class provides functionality for correcting decompile code to Java convention standard
 *
 * @author Aleksei Makarov
 * @since 1.0.1
 */
class StringUtils {

    /**
     * Process of normalize open block condition and condition it to
     * egyptian standard
     * For example
     * <code>
     * code {
     * }
     * </code>
     *
     * @param builder any builder instance
     * @return normalized string
     */
    static String normalizeOpenBlockCharacter(StringBuilder builder) {
        int index = 1;
        while (index != 0) {
            int openBlock = builder.indexOf("{", index);
            int nonSpace = getFirstLeftNonCharNumber(builder, ' ', openBlock);
            if (nonSpace != -1 && builder.charAt(nonSpace) == '\n') {
                builder.delete(nonSpace, openBlock);
                builder.insert(nonSpace, ' ');
                index = openBlock;
            } else {
                index = openBlock + 1;
            }
        }

        return builder.toString();
    }

    /**
     * Obtains index of first left non select character from right side
     *
     * @param builder   any string builder instance
     * @param character select character
     * @return index of first left condition
     */
    static int getFirstLeftNonCharNumber(StringBuilder builder, char character) {
        return getFirstLeftNonCharNumber(builder, character, builder.length());
    }

    /**
     * Obtains index of first left non select character from select index
     *
     * @param line      any string builder instance
     * @param character select character
     * @param number    index from start search
     * @return index of first left condition
     */
    static int getFirstLeftNonCharNumber(StringBuilder line, char character, int number) {
        for (int i = number - 1; i > 0; i--) {
            if (line.charAt(i) != character) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Checks contains char in any {@link CharSequence} object
     *
     * @param charSequence any char sequence
     * @param character    any character
     * @return true if character constrains in char sequence
     */
    static boolean contains(CharSequence charSequence, char character) {
        int index = charSequence.length() - 1;
        for (int i = 0; i < index; i++) {
            if (charSequence.charAt(index) == character) {
                return true;
            }
        }

        return false;
    }

    /**
     * Obtains first left index of line separator
     *
     * @param builder any string builder
     * @return first left index
     */
    static int getNumberLeftOfLineSeparator(StringBuilder builder) {
        int index = builder.length() - 1;
        while (builder.charAt(index) != '\n') {
            index--;
        }

        return index + 1;
    }
}