package com.classparser.bytecode.decompile.jd.configuration;

import com.classparser.bytecode.decompile.jd.JDDecompiler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class provides java API special builder for creating
 * configuration map for {@link JDDecompiler}
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public class JDBuilderConfiguration {

    /**
     * Creates instance of builder
     *
     * @return {@link JDConfiguration} instance
     */
    public static JDConfiguration createBuilder() {
        return new Builder();
    }

    /**
     * Inner builder class implements {@link JDConfiguration}
     */
    private static class Builder implements JDConfiguration {

        private final Map<String, Object> configuration;

        private Builder() {
            this.configuration = new ConcurrentHashMap<>();
        }

        @Override
        public Map<String, Object> getConfiguration() {
            return configuration;
        }

        @Override
        public JDConfiguration displayDefaultConstructor(boolean flag) {
            configuration.put(SHOW_DEFAULT_CONSTRUCTOR_KEY, flag);
            return this;
        }

        @Override
        public JDConfiguration realignmentLineNumber(boolean flag) {
            configuration.put(REALIGNMENT_LINE_NUMBER_KEY, flag);
            return this;
        }

        @Override
        public JDConfiguration displayPrefixThis(boolean flag) {
            configuration.put(SHOW_PREFIX_THIS_KEY, flag);
            return this;
        }

        @Override
        public JDConfiguration mergeEmptyLines(boolean flag) {
            configuration.put(MERGE_EMPTY_LINES_KEY, flag);
            return this;
        }

        @Override
        public JDConfiguration unicodeEscape(boolean flag) {
            configuration.put(UNICODE_ESCAPE_KEY, flag);
            return this;
        }

        @Override
        public JDConfiguration displayLineNumbers(boolean flag) {
            configuration.put(SHOW_LINE_NUMBERS_KEY, flag);
            return this;
        }

        @Override
        public JDConfiguration setCountIndentSpaces(int indent) {
            StringBuilder builder = new StringBuilder();

            for (int index = 0; index < indent; index++) {
                builder.append(' ');
            }

            configuration.put(COUNT_INDENT_SPACES_KEY, builder.toString());
            return this;
        }
    }
}