package com.classparser.bytecode.decompile.procyon.configuration;

import com.classparser.bytecode.decompile.procyon.ProcyonDecompiler;
import com.classparser.configuration.Configuration;
import com.strobel.decompiler.languages.Language;
import com.strobel.decompiler.languages.java.JavaFormattingOptions;
import com.strobel.decompiler.languages.java.JavaLanguage;

/**
 * Builder configuration for {@link ProcyonDecompiler}
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public interface ProcyonConfiguration extends Configuration {

    String UPLOAD_CLASS_REFERENCE_KEY = "ucr";

    String EXCLUDE_NESTED_TYPES_KEY = "ent";

    String FLATTEN_SWITCH_BLOCKS_KEY = "fsb";

    String FORCE_EXPLICIT_IMPORTS_KEY = "fei";

    String FORCE_EXPLICIT_TYPE_ARGUMENTS_KEY = "eta";

    String LANGUAGE_KEY = "lan";

    String JAVA_FORMATTER_OPTIONS_KEY = "jfo";

    String DISPLAY_SYNTHETIC_MEMBERS_KEY = "ssm";

    String ALWAYS_GENERATE_EXCEPTION_VARIABLE_FOR_CATCH_BLOCKS_KEY = "gec";

    String INCLUDE_ERROR_DIAGNOSTICS_KEY = "ied";

    String INCLUDE_LINE_NUMBERS_IN_BYTECODE_KEY = "iln";

    String RETAIN_REDUNDANT_CASTS_KEY = "rrc";

    String RETAIN_POINTLESS_SWITCHES_KEY = "rps";

    String UNICODE_OUTPUT_ENABLED_KEY = "uoe";

    String SHOW_DEBUG_LINE_NUMBERS_KEY = "sdl";

    String MERGE_VARIABLES_KEY = "mva";

    String SIMPLIFY_MEMBER_REFERENCES_KEY = "smr";

    String DISABLE_FOR_EACH_TRANSFORMS_KEY = "det";

    /**
     * Upload bytecode of references classes
     * for create annotation @Override
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    ProcyonConfiguration uploadClassReference(boolean flag);

    /**
     * Exclude nested types
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    ProcyonConfiguration excludeNestedTypes(boolean flag);

    /**
     * Decompile switch blocks
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    ProcyonConfiguration flattenSwitchBlocks(boolean flag);

    /**
     * Force explicit imports
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    ProcyonConfiguration forceExplicitImports(boolean flag);

    /**
     * Force explicit type arguments
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    ProcyonConfiguration forceExplicitTypeArguments(boolean flag);

    /**
     * Set language to decompile
     * <p>
     * Default value: {@link JavaLanguage}
     *
     * @param language true/false value
     * @return builder instance
     */
    ProcyonConfiguration setLanguage(Language language);

    /**
     * Set java formatter options
     * <p>
     * Default value:
     * Default by procyon decompiler plus
     * options.ClassBraceStyle default BraceStyle.EndOfLine;
     * options.InterfaceBraceStyle default BraceStyle.EndOfLine;
     * options.EnumBraceStyle default BraceStyle.EndOfLine;
     *
     * @param language true/false value
     * @return builder instance
     */
    ProcyonConfiguration setJavaFormatterOptions(JavaFormattingOptions language);

    /**
     * Display synthetic members
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    ProcyonConfiguration showSyntheticMembers(boolean flag);

    /**
     * Always generate exception variable for catch blocks
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    ProcyonConfiguration alwaysGenerateExceptionVariableForCatchBlocks(boolean flag);

    /**
     * Include error diagnostics
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    ProcyonConfiguration includeErrorDiagnostics(boolean flag);

    /**
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    ProcyonConfiguration includeLineNumbersInBytecode(boolean flag);

    /**
     * Retain redundant casts
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    ProcyonConfiguration retainRedundantCasts(boolean flag);

    /**
     * Retain pointless switches
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    ProcyonConfiguration retainPointlessSwitches(boolean flag);

    /**
     * Unicode output enabled
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    ProcyonConfiguration unicodeOutputEnabled(boolean flag);

    /**
     * Display debug line numbers
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    ProcyonConfiguration showDebugLineNumbers(boolean flag);

    /**
     * Merge variables
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    ProcyonConfiguration mergeVariables(boolean flag);

    /**
     * Simplify member references
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    ProcyonConfiguration simplifyMemberReferences(boolean flag);

    /**
     * Disable foreach transforms
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    ProcyonConfiguration disableForEachTransforms(boolean flag);
}