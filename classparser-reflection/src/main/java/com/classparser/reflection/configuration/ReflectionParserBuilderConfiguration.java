package com.classparser.reflection.configuration;

import com.classparser.reflection.ReflectionParser;
import com.classparser.reflection.configuration.api.ReflectionParserConfiguration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class provides java API special builder for creating
 * configuration map for {@link ReflectionParser}
 *
 * @author Aleksey Makarov
 * @since 1.0.0
 */
public class ReflectionParserBuilderConfiguration {

    /**
     * Obtains builder instance
     *
     * @return builder for configuration
     */
    public static ReflectionParserConfiguration createBuilder() {
        return new Builder();
    }

    /**
     * Private inner builder provides method for configuration
     */
    private static class Builder implements ReflectionParserConfiguration {

        private final Map<String, Object> configuration;

        private Builder() {
            this.configuration = new ConcurrentHashMap<>();
        }

        @Override
        public Map<String, Object> getConfiguration() {
            return configuration;
        }

        @Override
        public ReflectionParserConfiguration displayAnnotationOnTypes(boolean flag) {
            configuration.put(ANNOTATION_TYPE_DISPLAY_KEY, flag);
            return this;
        }

        @Override
        public ReflectionParserConfiguration displayInnerClasses(boolean flag) {
            configuration.put(INNER_CLASSES_DISPLAY_KEY, flag);
            return this;
        }

        @Override
        public ReflectionParserConfiguration displaySyntheticEntities(boolean flag) {
            configuration.put(SYNTHETIC_ENTITIES_DISPLAY_KEY, flag);
            return this;
        }

        @Override
        public ReflectionParserConfiguration displayDefaultValueInAnnotation(boolean flag) {
            configuration.put(DISPLAY_DEFAULT_VALUE_IN_ANNOTATIONS_KEY, flag);
            return this;
        }

        @Override
        public ReflectionParserConfiguration displayGenericSignatures(boolean flag) {
            configuration.put(DISPLAY_GENERIC_SIGNATURES_KEY, flag);
            return this;
        }

        @Override
        public ReflectionParserConfiguration displayVarArgs(boolean flag) {
            configuration.put(DISPLAY_VAR_ARGS_KEY, flag);
            return this;
        }

        @Override
        public ReflectionParserConfiguration displayValueForFields(boolean flag) {
            configuration.put(DISPLAY_VALUE_IN_STATIC_FIELDS_KEY, flag);
            return this;
        }

        @Override
        public ReflectionParserConfiguration enableStaticBlockDisplaying(boolean flag) {
            configuration.put(ENABLED_STATIC_BLOCK_DISPLAYING, flag);
            return this;
        }

        @Override
        public ReflectionParserConfiguration enableImportSection(boolean flag) {
            configuration.put(DISPLAY_IMPORT_SECTION_KEY, flag);
            return this;
        }

        @Override
        public ReflectionParserConfiguration displayDefaultInheritance(boolean flag) {
            configuration.put(DISPLAY_DEFAULT_INHERITANCE_KEY, flag);
            return this;
        }

        @Override
        public ReflectionParserConfiguration hideExhaustiveModifiers(boolean flag) {
            configuration.put(HIDE_EXHAUSTIVE_MODIFIERS_KEY, flag);
            return this;
        }

        @Override
        public ReflectionParserConfiguration setCountIndentSpaces(int indent) {
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < indent; i++) {
                builder.append(' ');
            }

            configuration.put(COUNT_INDENT_SPACES_KEY, builder.toString());
            return this;
        }

        @Override
        public ReflectionParserConfiguration defineLineSeparator(String character) {
            if (character != null) {
                configuration.put(LINE_SEPARATOR_KEY, character);
            } else {
                throw new NullPointerException("Line separator is can't be a null!");
            }

            return this;
        }
    }
}