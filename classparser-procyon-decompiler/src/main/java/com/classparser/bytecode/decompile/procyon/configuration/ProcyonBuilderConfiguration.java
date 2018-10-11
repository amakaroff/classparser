package com.classparser.bytecode.decompile.procyon.configuration;

import com.classparser.bytecode.decompile.procyon.ProcyonDecompiler;
import com.strobel.decompiler.languages.Language;
import com.strobel.decompiler.languages.java.JavaFormattingOptions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class provides java API special builder for creating
 * configuration map for {@link ProcyonDecompiler}
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public class ProcyonBuilderConfiguration {

    /**
     * Creates instance of builder
     *
     * @return {@link ProcyonConfiguration} instance
     */
    public static ProcyonConfiguration getBuilderConfiguration() {
        return new Builder();
    }

    /**
     * Inner builder class implements {@link ProcyonConfiguration}
     */
    private static class Builder implements ProcyonConfiguration {

        private final Map<String, Object> configuration;

        /**
         * Default constructor for initialize {@link Builder}
         */
        public Builder() {
            this.configuration = new ConcurrentHashMap<>();
        }

        @Override
        public Map<String, Object> getConfiguration() {
            return configuration;
        }

        @Override
        public ProcyonConfiguration uploadClassReference(boolean flag) {
            configuration.put(UPLOAD_CLASS_REFERENCE_KEY, flag);
            return this;
        }

        @Override
        public ProcyonConfiguration excludeNestedTypes(boolean flag) {
            configuration.put(EXCLUDE_NESTED_TYPES_KEY, flag);
            return this;
        }

        @Override
        public ProcyonConfiguration flattenSwitchBlocks(boolean flag) {
            configuration.put(FLATTEN_SWITCH_BLOCKS_KEY, flag);
            return this;
        }

        @Override
        public ProcyonConfiguration forceExplicitImports(boolean flag) {
            configuration.put(FORCE_EXPLICIT_IMPORTS_KEY, flag);
            return this;
        }

        @Override
        public ProcyonConfiguration forceExplicitTypeArguments(boolean flag) {
            configuration.put(FORCE_EXPLICIT_TYPE_ARGUMENTS_KEY, flag);
            return this;
        }

        @Override
        public ProcyonConfiguration setLanguage(Language language) {
            configuration.put(LANGUAGE_KEY, language);
            return this;
        }

        @Override
        public ProcyonConfiguration setJavaFormatterOptions(JavaFormattingOptions language) {
            configuration.put(JAVA_FORMATTER_OPTIONS_KEY, language);
            return this;
        }

        @Override
        public ProcyonConfiguration showSyntheticMembers(boolean flag) {
            configuration.put(DISPLAY_SYNTHETIC_MEMBERS_KEY, flag);
            return this;
        }

        @Override
        public ProcyonConfiguration alwaysGenerateExceptionVariableForCatchBlocks(boolean flag) {
            configuration.put(ALWAYS_GENERATE_EXCEPTION_VARIABLE_FOR_CATCH_BLOCKS_KEY, flag);
            return this;
        }

        @Override
        public ProcyonConfiguration includeErrorDiagnostics(boolean flag) {
            configuration.put(INCLUDE_ERROR_DIAGNOSTICS_KEY, flag);
            return this;
        }

        @Override
        public ProcyonConfiguration includeLineNumbersInBytecode(boolean flag) {
            configuration.put(INCLUDE_LINE_NUMBERS_IN_BYTECODE_KEY, flag);
            return this;
        }

        @Override
        public ProcyonConfiguration retainRedundantCasts(boolean flag) {
            configuration.put(RETAIN_REDUNDANT_CASTS_KEY, flag);
            return this;
        }

        @Override
        public ProcyonConfiguration retainPointlessSwitches(boolean flag) {
            configuration.put(RETAIN_POINTLESS_SWITCHES_KEY, flag);
            return this;
        }

        @Override
        public ProcyonConfiguration unicodeOutputEnabled(boolean flag) {
            configuration.put(UNICODE_OUTPUT_ENABLED_KEY, flag);
            return this;
        }

        @Override
        public ProcyonConfiguration showDebugLineNumbers(boolean flag) {
            configuration.put(SHOW_DEBUG_LINE_NUMBERS_KEY, flag);
            return this;
        }

        @Override
        public ProcyonConfiguration mergeVariables(boolean flag) {
            configuration.put(MERGE_VARIABLES_KEY, flag);
            return this;
        }

        @Override
        public ProcyonConfiguration simplifyMemberReferences(boolean flag) {
            configuration.put(SIMPLIFY_MEMBER_REFERENCES_KEY, flag);
            return this;
        }

        @Override
        public ProcyonConfiguration disableForEachTransforms(boolean flag) {
            configuration.put(DISABLE_FOR_EACH_TRANSFORMS_KEY, flag);
            return this;
        }
    }
}