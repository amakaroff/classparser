package com.classparser.bytecode.configuration;

import com.classparser.bytecode.agent.DefaultJavaAgent;
import com.classparser.bytecode.api.JavaAgent;
import com.classparser.bytecode.api.Decompiler;
import com.classparser.bytecode.decompile.fernflower.FernflowerDecompiler;
import com.classparser.configuration.Configuration;
import com.classparser.util.ConfigurationUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.classparser.bytecode.configuration.api.BytecodeParserConfiguration.*;

/**
 * Basic configuration manager for {@link com.classparser.bytecode.BytecodeParser}
 * Provides methods for checking current configuration on parser
 * <p>
 * This class is thread safe
 * </p>
 *
 * @author Aleksey Makarov
 * @since 1.0.0
 */
public class ConfigurationManager {

    private static final String HOME_DIR_KEY = "user.dir";

    private final ConfigurationUtils utils;

    public ConfigurationManager() {
        this.utils = new ConfigurationUtils(getDefaultConfiguration());
    }

    /**
     * Obtain default configuration
     * <ul>
     *      <li>{@link #isNeedToDecompileInnerClasses()} ()} - yes</li>
     *      <li>{@link #getDecompiler()} - {@link FernflowerDecompiler}</li>
     *      <li>{@link #isEnableClassFileBytecodeCollector()} - yes</li>
     *      <li>{@link #isEnableInstrumentationBytecodeCollector()} - yes</li>
     *      <li>{@link #isEnableDumperByteCodeCollector()} - no</li>
     *      <li>{@link #isSaveToFile()} - no</li>
     *      <li>{@link #getCustomDecompilerConfiguration()} - Empty</li>
     *      <li>{@link #isCacheAgentJar()} - true</li>
     *      <li>{@link #getDirectoryForSaveBytecode()} - ${user.dir}/classes</li>
     *      <li>{@link #getAgent()} - {@link DefaultJavaAgent}</li>
     *      <li>{@link #getToolsJarPath()} - Empty</li>
     * </ul>
     *
     * @return default configuration
     */
    protected Map<String, Object> getDefaultConfiguration() {
        return BytecodeParserBuilderConfiguration
                .createBuilder()
                .decompileAllInnerClasses(true)
                .setDecompiler(new FernflowerDecompiler())
                .enableClassFileBytecodeCollector(true)
                .enableDumperBytecodeCollector(true)
                .enableFromInstrumentationBytecodeCollector(true)
                .saveByteCodeToFile(false)
                .setDecompilerConfiguration(HashMap::new)
                .cacheAgentJar(false)
                .setDirectoryToSaveByteCode(System.getProperty(HOME_DIR_KEY) + File.separatorChar + "classes")
                .setAgentClass(new DefaultJavaAgent(this))
                .setToolsJarPath("")
                .getConfiguration();
    }

    /**
     * Reload current configuration
     *
     * @param configuration new bytecode parser configuration instance
     */
    public void reloadConfiguration(Configuration configuration) {
        this.utils.reloadConfiguration(configuration);
    }

    /**
     * Checks if necessary find and decompile inner classes in parser process
     *
     * @return true if decompile inner classes is needed
     */
    public boolean isNeedToDecompileInnerClasses() {
        return utils.getConfigOption(DECOMPILE_ALL_INNER_CLASSES_KEY, Boolean.class);
    }

    /**
     * Checks if necessary safe found bytecode of class to file
     *
     * @return true if needed save found class to file
     */
    public boolean isSaveToFile() {
        return utils.getConfigOption(SAVE_LOADED_BYTECODE_TO_FILE_KEY, Boolean.class);
    }

    /**
     * Obtains path to directory where will be store founded bytecode of classes
     *
     * @return path to directory where necessary store bytecode
     */
    public String getDirectoryForSaveBytecode() {
        return utils.getConfigOption(DIRECTORY_FOR_SAVING_BYTECODE_KEY, String.class);
    }

    /**
     * Obtains custom configuration for decompiler
     *
     * @return custom decompiler configuration
     */
    public Configuration getCustomDecompilerConfiguration() {
        return utils.getConfigOption(DECOMPILER_CONFIGURATION_KEY, Configuration.class);
    }

    /**
     * Obtains current decompiler
     *
     * @return decompiler
     */
    public Decompiler getDecompiler() {
        return utils.getConfigOption(DECOMPILER_KEY, Decompiler.class);
    }

    /**
     * Checks if class file bytecode collector should be enabled
     *
     * @return true if class file collector is enable
     */
    public boolean isEnableClassFileBytecodeCollector() {
        return utils.getConfigOption(ENABLED_CLASS_FILE_BYTECODE_COLLECTOR_KEY, Boolean.class);
    }

    /**
     * Checks if instrumentation bytecode collector should be enabled
     *
     * @return true if instrumentation collector is enable
     */
    public boolean isEnableInstrumentationBytecodeCollector() {
        return utils.getConfigOption(ENABLED_INSTRUMENTATION_BYTECODE_COLLECTOR_KEY, Boolean.class);
    }

    /**
     * Checks if custom bytecode collector should be enabled
     *
     * @return true if custom collector is enable
     */
    public boolean isEnableDumperByteCodeCollector() {
        return utils.getConfigOption(ENABLED_DUMPER_BYTECODE_COLLECTOR_KEY, Boolean.class);
    }

    /**
     * Checks if java agent jar should be cached
     *
     * @return true if agent jar should be cached
     */
    public boolean isCacheAgentJar() {
        return utils.getConfigOption(CACHE_AGENT_JAR_KEY, Boolean.class);
    }

    /**
     * Obtains java agent instance for current configuration
     *
     * @return java agent instance
     */
    public JavaAgent getAgent() {
        return utils.getConfigOption(AGENT_KEY, JavaAgent.class);
    }

    /**
     * Obtains path to tools.jar
     *
     * @return path to tools.jar
     */
    public String getToolsJarPath() {
        return utils.getConfigOption(TOOLS_JAR_PATH_KEY, String.class);
    }
}