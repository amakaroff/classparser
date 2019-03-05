package com.classparser.bytecode.configuration;

import com.classparser.bytecode.BytecodeParser;
import com.classparser.bytecode.api.Decompiler;
import com.classparser.bytecode.api.JavaAgent;
import com.classparser.bytecode.configuration.api.BytecodeParserConfiguration;
import com.classparser.configuration.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class provides java API special builder for creating
 * configuration map for {@link BytecodeParser}
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public class BytecodeParserBuilderConfiguration {

    /**
     * Gets builder instance
     *
     * @return builder instance
     */
    public static BytecodeParserConfiguration createBuilder() {
        return new Builder();
    }

    /**
     * Private inner builder provides method for configuration
     */
    private static class Builder implements BytecodeParserConfiguration {

        private final Map<String, Object> configuration;

        private Builder() {
            this.configuration = new ConcurrentHashMap<>();
        }

        @Override
        public Map<String, Object> getConfiguration() {
            return configuration;
        }

        @Override
        public BytecodeParserConfiguration decompileAllInnerClasses(boolean flag) {
            configuration.put(DECOMPILE_ALL_INNER_CLASSES_KEY, flag);
            return this;
        }

        @Override
        public BytecodeParserConfiguration saveByteCodeToFile(boolean flag) {
            configuration.put(SAVE_LOADED_BYTECODE_TO_FILE_KEY, flag);
            return this;
        }

        @Override
        public BytecodeParserConfiguration setDirectoryToSaveByteCode(String path) {
            if (path != null) {
                configuration.put(DIRECTORY_FOR_SAVING_BYTECODE_KEY, path);
            } else {
                throw new NullPointerException("Saving directory path is can't be null!");
            }

            return this;
        }

        @Override
        public BytecodeParserConfiguration setDecompilerConfiguration(Configuration configuration) {
            if (configuration != null) {
                this.configuration.put(DECOMPILER_CONFIGURATION_KEY, configuration);
            } else {
                throw new NullPointerException("Configuration is can't be a null!");
            }

            return this;
        }

        @Override
        public BytecodeParserConfiguration setDecompiler(Decompiler decompiler) {
            if (decompiler != null) {
                configuration.put(DECOMPILER_KEY, decompiler);
            } else {
                throw new NullPointerException("Decompiler is can't be a null!");
            }
            return this;
        }

        @Override
        public BytecodeParserConfiguration enableClassFileBytecodeCollector(boolean flag) {
            configuration.put(ENABLED_CLASS_FILE_BYTECODE_COLLECTOR_KEY, flag);
            return this;
        }

        @Override
        public BytecodeParserConfiguration enableFromInstrumentationBytecodeCollector(boolean flag) {
            configuration.put(ENABLED_INSTRUMENTATION_BYTECODE_COLLECTOR_KEY, flag);
            return this;
        }

        @Override
        public BytecodeParserConfiguration enableDumperBytecodeCollector(boolean flag) {
            configuration.put(ENABLED_DUMPER_BYTECODE_COLLECTOR_KEY, flag);
            return this;
        }

        @Override
        public BytecodeParserConfiguration cacheAgentJar(boolean flag) {
            configuration.put(CACHE_AGENT_JAR_KEY, flag);
            return this;
        }

        @Override
        public BytecodeParserConfiguration setAgentClass(JavaAgent agent) {
            if (agent != null) {
                configuration.put(AGENT_KEY, agent);
            } else {
                throw new NullPointerException("Java Agent is can't be a null!");
            }

            return this;
        }

        @Override
        public BytecodeParserConfiguration setToolsJarPath(String path) {
            if (path != null) {
                configuration.put(TOOLS_JAR_PATH_KEY, path);
            } else {
                throw new NullPointerException("Tools jar path is can't be a null!");
            }

            return this;
        }
    }
}