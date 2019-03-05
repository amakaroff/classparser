package com.classparser.bytecode.decompile.procyon;

import com.classparser.bytecode.api.BytecodeCollector;
import com.classparser.bytecode.api.Decompiler;
import com.classparser.bytecode.collector.ChainBytecodeCollector;
import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.bytecode.decompile.procyon.configuration.ProcyonBuilderConfiguration;
import com.classparser.bytecode.exception.DecompilationException;
import com.classparser.bytecode.utils.ClassNameConverter;
import com.classparser.util.ConfigurationUtils;
import com.strobel.assembler.metadata.Buffer;
import com.strobel.assembler.metadata.DeobfuscationUtilities;
import com.strobel.assembler.metadata.ITypeLoader;
import com.strobel.assembler.metadata.MetadataSystem;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.decompiler.DecompilationOptions;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;
import com.strobel.decompiler.languages.Language;
import com.strobel.decompiler.languages.java.BraceStyle;
import com.strobel.decompiler.languages.java.JavaFormattingOptions;
import com.strobel.decompiler.languages.java.JavaLanguage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.classparser.bytecode.decompile.procyon.configuration.ProcyonConfiguration.*;

/**
 * Adapter of Procyon decompiler for {@link Decompiler} API
 * This decompiler was written of Mike Strobel
 * Decompiler version: 0.5.32 (Jul 26, 2018)
 * <p>
 * Procyon decompiler supports java 8 syntax
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public final class ProcyonDecompiler implements Decompiler {

    private final ConfigurationUtils utils;

    private volatile ConfigurationManager configurationManager;

    /**
     * Default constructor for creating {@link ProcyonDecompiler} instance
     */
    public ProcyonDecompiler() {
        this.utils = new ConfigurationUtils(getDefaultConfiguration());
    }

    @Override
    public String decompile(byte[] bytecode, Collection<byte[]> classes) {
        if (bytecode != null && classes != null) {
            String className = ClassNameConverter.getClassName(bytecode);
            Map<String, byte[]> bytecodeMap = createInnerClassesByteCodeMap(classes);
            bytecodeMap.put(className, bytecode);

            PlainTextOutput output = new PlainTextOutput();
            DecompilerSettings settings = getDecompilerSettings();

            ITypeLoader typeLoader = new ProcyonTypeLoader(className, bytecodeMap);
            MetadataSystem metadataSystem = new MetadataSystem(typeLoader);
            TypeReference type = metadataSystem.lookupType(className);

            TypeDefinition resolvedType = type.resolve();
            DeobfuscationUtilities.processType(resolvedType);
            DecompilationOptions options = new DecompilationOptions();
            options.setSettings(settings);

            if (settings.getJavaFormattingOptions() == null) {
                settings.setJavaFormattingOptions(JavaFormattingOptions.createDefault());
            }

            settings.getLanguage().decompileType(resolvedType, output, options);

            return output.toString();
        } else {
            throw new DecompilationException("Bytecode of classes for decompilation can't be a null!");
        }
    }

    /**
     * Creates map of inner classes where key is name of class,
     * and value is bytecode of this class
     *
     * @param classes collection of inner classes bytecode
     * @return inner classes map
     */
    private Map<String, byte[]> createInnerClassesByteCodeMap(Collection<byte[]> classes) {
        Map<String, byte[]> bytecodeMap = new HashMap<>();

        for (byte[] bytecode : classes) {
            bytecodeMap.put(ClassNameConverter.getClassName(bytecode), bytecode);
        }

        return bytecodeMap;
    }

    /**
     * Parses and creates settings for procyon decompiler
     *
     * @return decompiler settings
     */
    private DecompilerSettings getDecompilerSettings() {
        DecompilerSettings settings = new DecompilerSettings();

        settings.setExcludeNestedTypes(utils.getConfigOption(EXCLUDE_NESTED_TYPES_KEY, Boolean.class));
        settings.setFlattenSwitchBlocks(utils.getConfigOption(FLATTEN_SWITCH_BLOCKS_KEY, Boolean.class));
        settings.setForceExplicitImports(utils.getConfigOption(FORCE_EXPLICIT_IMPORTS_KEY, Boolean.class));
        settings.setForceExplicitTypeArguments(utils.getConfigOption(FORCE_EXPLICIT_TYPE_ARGUMENTS_KEY, Boolean.class));
        settings.setLanguage(utils.getConfigOption(LANGUAGE_KEY, Language.class));
        settings.setJavaFormattingOptions(utils.getConfigOption(JAVA_FORMATTER_OPTIONS_KEY, JavaFormattingOptions.class));
        settings.setShowSyntheticMembers(utils.getConfigOption(DISPLAY_SYNTHETIC_MEMBERS_KEY, Boolean.class));
        settings.setAlwaysGenerateExceptionVariableForCatchBlocks(
                utils.getConfigOption(ALWAYS_GENERATE_EXCEPTION_VARIABLE_FOR_CATCH_BLOCKS_KEY, Boolean.class)
        );
        settings.setIncludeErrorDiagnostics(utils.getConfigOption(INCLUDE_ERROR_DIAGNOSTICS_KEY, Boolean.class));
        settings.setIncludeLineNumbersInBytecode(
                utils.getConfigOption(INCLUDE_LINE_NUMBERS_IN_BYTECODE_KEY, Boolean.class)
        );
        settings.setRetainRedundantCasts(utils.getConfigOption(RETAIN_REDUNDANT_CASTS_KEY, Boolean.class));
        settings.setRetainPointlessSwitches(utils.getConfigOption(RETAIN_POINTLESS_SWITCHES_KEY, Boolean.class));
        settings.setUnicodeOutputEnabled(utils.getConfigOption(UNICODE_OUTPUT_ENABLED_KEY, Boolean.class));
        settings.setShowDebugLineNumbers(utils.getConfigOption(SHOW_DEBUG_LINE_NUMBERS_KEY, Boolean.class));
        settings.setMergeVariables(utils.getConfigOption(MERGE_VARIABLES_KEY, Boolean.class));
        settings.setSimplifyMemberReferences(utils.getConfigOption(SIMPLIFY_MEMBER_REFERENCES_KEY, Boolean.class));
        settings.setDisableForEachTransforms(utils.getConfigOption(DISABLE_FOR_EACH_TRANSFORMS_KEY, Boolean.class));

        return settings;
    }

    /**
     * Creates default configuration for decompiler
     *
     * @return default configuration
     * @see ProcyonBuilderConfiguration for default values
     */
    private Map<String, Object> getDefaultConfiguration() {
        JavaFormattingOptions options = JavaFormattingOptions.createDefault();
        options.ClassBraceStyle = BraceStyle.EndOfLine;
        options.InterfaceBraceStyle = BraceStyle.EndOfLine;
        options.EnumBraceStyle = BraceStyle.EndOfLine;

        return ProcyonBuilderConfiguration
                .createBuilder()
                .uploadClassReference(true)
                .excludeNestedTypes(false)
                .flattenSwitchBlocks(true)
                .forceExplicitImports(true)
                .forceExplicitTypeArguments(true)
                .setLanguage(new JavaLanguage())
                .setJavaFormatterOptions(options)
                .showSyntheticMembers(true)
                .alwaysGenerateExceptionVariableForCatchBlocks(true)
                .includeErrorDiagnostics(false)
                .includeLineNumbersInBytecode(false)
                .retainRedundantCasts(true)
                .retainPointlessSwitches(true)
                .unicodeOutputEnabled(true)
                .showDebugLineNumbers(false)
                .mergeVariables(false)
                .simplifyMemberReferences(true)
                .disableForEachTransforms(false)
                .getConfiguration();
    }

    @Override
    public void setConfigurationManager(ConfigurationManager configurationManager) {
        if (configurationManager != null) {
            this.configurationManager = configurationManager;
            this.utils.reloadConfiguration(configurationManager.getCustomDecompilerConfiguration());
        } else {
            throw new NullPointerException("Configuration manager is can't be a null!");
        }
    }

    /**
     * Implementation of {@link ITypeLoader} uses chain bytecode load mechanism
     */
    private class ProcyonTypeLoader implements ITypeLoader {

        private static final int START_POSITION = 0;

        private final Map<String, byte[]> bytecodeMap;

        private final String outerClassName;

        private final boolean isLoadReferenceOnClass;

        private final BytecodeCollector collector;

        /**
         * Default constructor for create instance of {@link ProcyonTypeLoader}
         *
         * @param outerClassName name of main decompiled class
         * @param bytecodeMap    map of all inner classes for outer class
         */
        ProcyonTypeLoader(String outerClassName, Map<String, byte[]> bytecodeMap) {
            this.outerClassName = outerClassName;
            this.bytecodeMap = bytecodeMap;
            this.isLoadReferenceOnClass = utils.getConfigOption(UPLOAD_CLASS_REFERENCE_KEY, Boolean.class);
            this.collector = new ChainBytecodeCollector(configurationManager);
        }

        @Override
        public boolean tryLoadType(String baseClassName, Buffer buffer) {
            byte[] bytecode = bytecodeMap.get(baseClassName);

            if (bytecode == null) {
                if (isLoadReferenceOnClass) {
                    if (baseClassName.contains(outerClassName + '$')) {
                        return false;
                    }

                    bytecode = loadByteCode(ClassNameConverter.toJavaClassName(baseClassName));
                    if (bytecode == null) {
                        return false;
                    }
                } else {
                    return false;
                }
            }

            buffer.putByteArray(bytecode, START_POSITION, bytecode.length);
            buffer.position(START_POSITION);

            return true;
        }

        /**
         * Try load class by full class name
         *
         * @param className name of class
         * @return class instance or null if class not found
         */
        private Class<?> loadClass(String className) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException ignore) {
                return null;
            }
        }

        /**
         * Try load bytecode of class
         *
         * @param className name of class
         * @return bytecode of class or null if class or bytecode is not found
         */
        private byte[] loadByteCode(String className) {
            Class<?> clazz = loadClass(className);

            if (clazz != null) {
                return collector.getBytecode(clazz);
            }

            return null;
        }
    }
}