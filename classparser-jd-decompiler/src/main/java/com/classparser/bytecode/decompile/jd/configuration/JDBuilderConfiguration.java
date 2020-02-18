package com.classparser.bytecode.decompile.jd.configuration;

import com.classparser.bytecode.decompile.jd.JDDecompiler;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker;

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
     * Creates an instance of builder
     *
     * @return {@link JDConfiguration} instance
     */
    public static JDConfiguration getBuilderConfiguration() {
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
        public JDConfiguration realignmentLineNumber(boolean flag) {
            configuration.put(REALIGNMENT_LINE_NUMBER_KEY, flag);
            return this;
        }

        @Override
        public JDConfiguration mergeEmptyLines(boolean flag) {
            configuration.put(MERGE_EMPTY_LINES_KEY, flag);
            return this;
        }

        @Override
        public JDConfiguration displayLineNumbers(boolean flag) {
            configuration.put(SHOW_LINE_NUMBERS_KEY, flag);
            return this;
        }

        @Override
        public JDConfiguration setCountIndentSpaces(int indent) {
            if (indent >= 0) {
                StringBuilder builder = new StringBuilder();

                for (int i = 0; i < indent; i++) {
                    builder.append(' ');
                }

                configuration.put(COUNT_INDENT_SPACES_KEY, builder.toString());
                return this;
            } else {
                throw new IllegalArgumentException("The indent cannot be less, than zero");
            }
        }

        @Override
        public JDConfiguration setTypeMaker(TypeMaker typeMaker) {
            if (typeMaker != null) {
                configuration.put(TYPE_MAKER_KEY, typeMaker);
                return this;
            } else {
                throw new IllegalArgumentException("The type maker cannot be null");
            }
        }
    }
}