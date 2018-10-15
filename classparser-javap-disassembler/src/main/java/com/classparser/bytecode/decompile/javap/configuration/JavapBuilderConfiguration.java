package com.classparser.bytecode.decompile.javap.configuration;

import com.classparser.bytecode.decompile.javap.JavaPrinterDisassembler;
import com.sun.tools.javap.InstructionDetailWriter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class provides java API special builder for creating
 * configuration map for {@link JavaPrinterDisassembler}
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public class JavapBuilderConfiguration {

    /**
     * Creates instance of builder
     *
     * @return {@link JavaPrinterDisassembler} instance
     */
    public static JavapConfiguration createBuilder() {
        return new Builder();
    }

    /**
     * Inner builder class implements {@link JavapConfiguration}
     */
    private static class Builder implements JavapConfiguration {

        private final Map<String, Object> configuration;

        private Builder() {
            this.configuration = new ConcurrentHashMap<>();
        }

        @Override
        public JavapConfiguration displayAllAttributesOfCode(boolean flag) {
            configuration.put(DISPLAY_ATTRIBUTES_OF_CODE_KEY, flag);
            return this;
        }

        @Override
        public JavapConfiguration displaySystemInformation(boolean flag) {
            configuration.put(DISPLAY_SYSTEM_INFORMATION_KEY, flag);
            return this;
        }

        @Override
        public JavapConfiguration displayVerboseInformation(boolean flag) {
            configuration.put(DISPLAY_VERBOSE_INFORMATION_KEY, flag);
            return this;
        }

        @Override
        public JavapConfiguration displayConstants(boolean flag) {
            configuration.put(DISPLAY_CONSTANTS_KEY, flag);
            return this;
        }

        @Override
        public JavapConfiguration displayDecompiledCode(boolean flag) {
            configuration.put(DISPLAY_DECOMPILE_CODE_KEY, flag);
            return this;
        }

        @Override
        public JavapConfiguration displayCodeLineAndLocalVariableTable(boolean flag) {
            configuration.put(DISPLAY_CODE_LINE_AND_LOCAL_VARIABLE_KEY, flag);
            return this;
        }

        @Override
        public JavapConfiguration appendDisplayOnlyElementsWithAccessModifier(AccessModifier accessModifier) {
            if (accessModifier != null) {
                Set<AccessModifier> accessModifiers = getOrCreateSetByKey(DISPLAY_MODIFIER_ACCESSOR_KEY);
                accessModifiers.add(accessModifier);
            }

            return this;
        }

        @Override
        public JavapConfiguration setIndentSpaces(int count) {
            if (count > 0) {
                configuration.put(INDENT_COUNT_SPACES_KEY, count);
            }
            return this;
        }

        @Override
        public JavapConfiguration displayDescriptors(boolean flag) {
            configuration.put(DISPLAY_DESCRIPTORS_KEY, flag);
            return this;
        }

        @Override
        public JavapConfiguration appendDisplayDetails(InstructionDetailWriter.Kind kind) {
            if (kind != null) {
                Set<InstructionDetailWriter.Kind> kinds = getOrCreateSetByKey(APPEND_DISPLAY_DETAILS_KEY);
                kinds.add(kind);
            }

            return this;
        }

        /**
         * Creates and obtains set from configuration map by key
         *
         * @param key key in configuration map
         * @param <T> type of set
         * @return sets from configuration map
         */
        @SuppressWarnings("unchecked")
        private <T> Set<T> getOrCreateSetByKey(String key) {
            Object element = configuration.get(key);

            if (element == null) {
                element = new HashSet<>();
            }
            configuration.put(key, element);

            return (Set<T>) element;
        }

        @Override
        public Map<String, Object> getConfiguration() {
            return configuration;
        }
    }
}