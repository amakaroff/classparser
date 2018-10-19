package com.classparser.bytecode.decompile.fernflower;

import com.classparser.bytecode.api.Decompiler;
import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.bytecode.decompile.fernflower.configuration.FernflowerBuilderConfiguration;
import com.classparser.bytecode.decompile.fernflower.configuration.FernflowerConfiguration;
import com.classparser.bytecode.exception.DecompilationException;
import com.classparser.configuration.Configuration;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructContext;
import org.jetbrains.java.decompiler.struct.lazy.LazyLoader;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter of Fernflower decompiler for {@link Decompiler} API
 * This decompiler was written of Egor Ushakov
 * Decompiler version: ? (18.10.2017)
 * <p>
 * Fernflower decompiler support java 8 syntax and can decompile all inner classes
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public final class FernflowerDecompiler implements Decompiler {

    private final Map<String, Object> configurationMap;

    public FernflowerDecompiler() {
        this.configurationMap = new ConcurrentHashMap<>();
    }

    @Override
    public String decompile(byte[] bytecode, Collection<byte[]> classes) {
        if (bytecode != null && classes != null) {
            DecompiledCodeSaver decompiledCodeSaver = new DecompiledCodeSaver();
            IFernflowerLogger logger = new PrintStreamLogger(System.out);
            Map<String, Object> configuration = getConfiguration();

            Fernflower fernflower = new Fernflower(null, decompiledCodeSaver, configuration, logger);

            StructContext structContext = fernflower.getStructContext();
            uploadBytecode(structContext, bytecode);
            for (byte[] nestedClass : classes) {
                uploadBytecode(structContext, nestedClass);
            }

            decompile(fernflower);

            return decompiledCodeSaver.getDecompiledCode();
        }

        throw new DecompilationException("Bytecode of classes for decompilation can't be a null!");
    }

    /**
     * Obtains current configuration for decompiler
     *
     * @return decompiler configuration
     */
    private Map<String, Object> getConfiguration() {
        if (configurationMap.isEmpty()) {
            return getDefaultConfiguration();
        } else {
            return configurationMap;
        }
    }

    /**
     * Starts decompile context process
     *
     * @param fernflower fernflower decompiler instance
     */
    private void decompile(Fernflower fernflower) {
        try {
            fernflower.decompileContext();
        } finally {
            fernflower.clearContext();
        }
    }

    /**
     * Creates default fernflower configuration
     * Describe of option can Fernflower configuration
     *
     * @return default configuration map
     * @see FernflowerConfiguration
     */
    private Map<String, Object> getDefaultConfiguration() {
        return FernflowerBuilderConfiguration
                .createBuilder()
                .displayBridgeMethods(false)
                .displayMemberSyntheticClasses(false)
                .decompileInnerClasses(true)
                .collapseClassReferences(true)
                .decompileAssertions(true)
                .displayEmptySuperInvocation(false)
                .displayEmptyDefaultConstructor(false)
                .decompileGenericSignatures(true)
                .deInlineFinallyStructures(true)
                .assumeReturnNotThrowingExceptions(true)
                .decompileEnumerations(false)
                .removeGetClassInvocation(false)
                .displayOutputNumericLiterals(false)
                .encodeNonASCIICharacters(true)
                .interpretInt1AsBooleanTrue(false)
                .allowForSetSyntheticAttribute(false)
                .considerNamelessTypes(false)
                .reconstructVariableNamesFromDebugInformation(true)
                .removeEmptyExceptionRanges(false)
                .setUpperLimitForDecompilation(0)
                .renameAmbiguousClassesAndClassElements(false)
                .checkNonNullAnnotation(true)
                .decompileLambdaExpressionsToAnonymousClasses(false)
                .setCountIndentSpaces(4)
                .setLogLevel(IFernflowerLogger.Severity.ERROR)
                .getConfiguration();
    }

    /**
     * Uploads bytecode to current fernflower decompiler context
     *
     * @param structContext struct context
     * @param bytecode      bytecode of class
     */
    private void uploadBytecode(StructContext structContext, byte[] bytecode) {
        StructClass structClass = createClassStruct(bytecode);

        Map<String, StructClass> classes = structContext.getClasses();
        classes.put(structClass.qualifiedName, structClass);
    }

    /**
     * Creates class structure from bytecode
     *
     * @param bytecode bytecode of class
     * @return struct class instance
     */
    private StructClass createClassStruct(byte[] bytecode) {
        try {
            LazyLoader lazyLoader = new LazyLoader((p1, p2) -> bytecode);
            StructClass structClass = new StructClass(bytecode, true, lazyLoader);
            LazyLoader.Link link = new LazyLoader.Link(LazyLoader.Link.CLASS, structClass.qualifiedName, "");
            lazyLoader.addClassLink(structClass.qualifiedName, link);

            return structClass;
        } catch (IOException exception) {
            throw new DecompilationException("Invalid bytes of java class!", exception);
        }
    }

    @Override
    public void setConfigurationManager(ConfigurationManager configurationManager) {
        if (configurationManager != null) {
            Configuration configuration = configurationManager.getCustomDecompilerConfiguration();
            if (configuration != null) {
                Map<String, Object> configurationMap = configuration.getConfiguration();
                if (configurationMap != null && !configurationMap.isEmpty()) {
                    Map<String, Object> defaultConfiguration = getDefaultConfiguration();
                    defaultConfiguration.putAll(configurationMap);
                    this.configurationMap.putAll(defaultConfiguration);
                }
            }
        }
    }

    /**
     * Class uses for obtaining decompiled code as string
     */
    private class DecompiledCodeSaver extends ConsoleDecompiler {

        private String decompiledCode;

        /**
         * Default constructor for initialize of decompiled code saver
         */
        DecompiledCodeSaver() {
            super(null, null);
        }

        @Override
        public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
            this.decompiledCode = content;
        }

        /**
         * Obtains decompiled code from saver
         *
         * @return decompiled code
         */
        String getDecompiledCode() {
            return decompiledCode;
        }
    }
}