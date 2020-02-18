package com.classparser.bytecode.decompile.jd.configuration;

import com.classparser.bytecode.decompile.jd.JDDecompiler;
import com.classparser.configuration.Configuration;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker;

/**
 * Builder configuration for {@link JDDecompiler}
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public interface JDConfiguration extends Configuration {
    
    String REALIGNMENT_LINE_NUMBER_KEY = "rln";
    
    String MERGE_EMPTY_LINES_KEY = "mel";

    String SHOW_LINE_NUMBERS_KEY = "sln";

    String COUNT_INDENT_SPACES_KEY = "ind";
    
    String TYPE_MAKER_KEY = "stm";

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
     * Merges empty lines
     * <p>
     * Default value: true
     *
     * @param flag true/false flag
     * @return builder instance
     */
    JDConfiguration mergeEmptyLines(boolean flag);

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

    /**
     * Set type maker to decompiler internal processing
     * <p>
     * Default value: null
     *
     * @param typeMaker instance of type maker
     * @return builder instance
     */
    JDConfiguration setTypeMaker(TypeMaker typeMaker);
}