package com.classparser.bytecode.configuration.api;

import com.classparser.bytecode.BytecodeParser;
import com.classparser.bytecode.agent.DefaultJavaAgent;
import com.classparser.bytecode.api.Decompiler;
import com.classparser.bytecode.api.JavaAgent;
import com.classparser.bytecode.decompile.fernflower.FernflowerDecompiler;
import com.classparser.configuration.Configuration;

/**
 * Builder configuration for class: {@link BytecodeParser}
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public interface BytecodeParserConfiguration extends Configuration {

    String DECOMPILE_ALL_INNER_CLASSES_KEY = "dic";

    String SAVE_LOADED_BYTECODE_TO_FILE_KEY = "stf";

    String DIRECTORY_FOR_SAVING_BYTECODE_KEY = "dts";

    String DECOMPILER_CONFIGURATION_KEY = "cdc";

    String DECOMPILER_KEY = "acd";

    String ENABLED_CLASS_FILE_BYTECODE_COLLECTOR_KEY = "cbc";

    String ENABLED_INSTRUMENTATION_BYTECODE_COLLECTOR_KEY = "ibc";

    String ENABLED_DUMPER_BYTECODE_COLLECTOR_KEY = "dbc";

    String AGENT_KEY = "jaa";

    String CACHE_AGENT_JAR_KEY = "caj";

    String TOOLS_JAR_PATH_KEY = "tjp";

    /**
     * Needs decompile inner, nested, anonymous and local classes
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    BytecodeParserConfiguration decompileAllInnerClasses(boolean flag);

    /**
     * Savings collect bytecode to file
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    BytecodeParserConfiguration saveByteCodeToFile(boolean flag);

    /**
     * Sets directory to save bytecode of classes
     * <p>
     * Default value: "${user.dir}/classes/"
     *
     * @param path path to bytecode folder store
     * @return builder instance
     */
    BytecodeParserConfiguration setDirectoryToSaveByteCode(String path);

    /**
     * Sets custom decompile configuration
     * <p>
     * Default value: depend on decompiler
     *
     * @param configuration decompiler configuration instance
     * @return builder instance
     */
    BytecodeParserConfiguration setDecompilerConfiguration(Configuration configuration);

    /**
     * Sets decompiler
     * <p>
     * Default value: {@link FernflowerDecompiler}
     *
     * @param decompiler decompiler instance
     * @return builder instance
     */
    BytecodeParserConfiguration setDecompiler(Decompiler decompiler);

    /**
     * Enables search bytecode from files
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    BytecodeParserConfiguration enableClassFileBytecodeCollector(boolean flag);

    /**
     * Enables getting bytecode uses instrumentation
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    BytecodeParserConfiguration enableFromInstrumentationBytecodeCollector(boolean flag);

    /**
     * Enables custom collect of bytecode
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    BytecodeParserConfiguration enableDumperBytecodeCollector(boolean flag);

    /**
     * Enables caching agent jar file
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    BytecodeParserConfiguration cacheAgentJar(boolean flag);

    /**
     * Sets custom java agent class
     * <p>
     * Default value: {@link DefaultJavaAgent}
     *
     * @param agent java agent instance
     * @return builder instance
     */
    BytecodeParserConfiguration setAgentClass(JavaAgent agent);

    /**
     * Sets custom path to tools.jar file
     *
     * @param path path to tools.jar
     * @return builder instance
     */
    BytecodeParserConfiguration setToolsJarPath(String path);
}