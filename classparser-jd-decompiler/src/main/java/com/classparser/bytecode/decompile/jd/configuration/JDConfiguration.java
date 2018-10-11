package com.classparser.bytecode.decompile.jd.configuration;

import com.classparser.bytecode.decompile.jd.JDDecompiler;
import com.classparser.configuration.Configuration;

/**
 * Builder configuration for {@link JDDecompiler}
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public interface JDConfiguration extends Configuration {

    String SHOW_DEFAULT_CONSTRUCTOR_KEY = "shc";

    String REALIGNMENT_LINE_NUMBER_KEY = "rln";

    String SHOW_PREFIX_THIS_KEY = "spt";

    String MERGE_EMPTY_LINES_KEY = "mel";

    String UNICODE_ESCAPE_KEY = "uce";

    String SHOW_LINE_NUMBERS_KEY = "sln";

    String COUNT_INDENT_SPACES_KEY = "ind";

    /**
     * Displays default constructor
     * <p>
     * Default value: true
     *
     * @param flag true/false flag
     * @return builder instance
     */
    JDConfiguration displayDefaultConstructor(boolean flag);

    /**
     * Realignments line number
     * <p>
     * Default value: true
     *
     * @param flag true/false flag
     * @return builder instance
     */
    JDConfiguration realignmentLineNumber(boolean flag);

    /**
     * Displays prefix "this"
     * <p>
     * Default value: true
     *
     * @param flag true/false flag
     * @return builder instance
     */
    JDConfiguration displayPrefixThis(boolean flag);

    /**
     * Merges empty lines
     * <p>
     * Default value: true
     *
     * @param flag true/false flag
     * @return builder instance
     */
    JDConfiguration mergeEmptyLines(boolean flag);

    /**
     * Unicode escapes
     * <p>
     * Default value: false
     *
     * @param flag true/false flag
     * @return builder instance
     */
    JDConfiguration unicodeEscape(boolean flag);

    /**
     * Displays line numbers
     * <p>
     * Default value: false
     *
     * @param flag true/false flag
     * @return builder instance
     */
    JDConfiguration displayLineNumbers(boolean flag);

    /**
     * Set count of indentations string
     * <p>
     * Default value: 4 spaces
     *
     * @param indent count of indent spaces
     * @return builder instance
     */
    JDConfiguration setCountIndentSpaces(int indent);
}