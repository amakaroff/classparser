package com.classparser.bytecode.decompile.fernflower;

import com.classparser.bytecode.api.Decompiler;
import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.bytecode.decompile.fernflower.configuration.FernflowerBuilderConfiguration;
import com.classparser.bytecode.decompile.fernflower.configuration.FernflowerConfiguration;
import com.classparser.bytecode.exception.decompile.DecompilationException;
import com.classparser.configuration.Configuration;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructContext;
import org.jetbrains.java.decompiler.struct.lazy.LazyLoader;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter of Fernflower decompiler for {@link Decompiler} API
 * This decompiler was written of Egor Ushakov
 * Decompiler version: ? (Feb 3, 2020)
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
    public String decompile(byte[] byteCode) {
        return decompile(byteCode, Collections.emptyList());
    }

    @Override
    public String decompile(byte[] byteCode, Collection<byte[]> nestedClassesByteCodes) {
        if (byteCode != null && nestedClassesByteCodes != null) {
            IFernflowerLogger logger = new PrintStreamLogger(System.out);
            IResultSaver nothingSaver = new NothingSaver(null, null, logger);
            Map<String, Object> configuration = getConfiguration();

            Fernflower fernflower = new Fernflower(null, nothingSaver, configuration, logger);

            StructClass structClass = createStructClass(byteCode);
            for (byte[] byteCodeOfNestedClass : nestedClassesByteCodes) {
                createStructClass(byteCodeOfNestedClass);
            }

            fernflower.decompileContext();
            String decompiledClass = fernflower.getClassContent(structClass);
            fernflower.clearContext();

            return decompiledClass;
        }

        throw new DecompilationException("Byte code of classes for decompilation can't be a null!");
    }

    /**
     * Obtains the current configuration for decompiler
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
     * Creates default fernflower configuration
     * Describe of option can Fernflower configuration
     *
     * @return default configuration map
     * @see FernflowerConfiguration
     */
    private Map<String, Object> getDefaultConfiguration() {
        return FernflowerBuilderConfiguration
                .getBuilderConfiguration()
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
                .ensureSynchronizedMonitor(true)
                .decompileEnumerations(false)
                .removeGetClassInvocation(false)
                .displayOutputNumericLiterals(false)
                .encodeNonASCIICharacters(true)
                .interpretInt1AsBooleanTrue(false)
                .allowForSetSyntheticAttribute(false)
                .considerNamelessTypes(false)
                .reconstructVariableNamesFromDebugInformation(true)
                .useMethodParameterNamesFromByteCodeStructure(true)
                .removeEmptyExceptionRanges(false)
                .setUpperLimitForDecompilation(0)
                .renameAmbiguousClassesAndClassElements(false)
                .checkNonNullAnnotation(true)
                .decompileLambdaExpressionsToAnonymousClasses(false)
                .performByteCodeSourceMapping(true)
                .ignoreInvalidByteCode(false)
                .verifyAnonymousClasses(true)
                .setCountIndentSpaces(4)
                .setLogLevel(IFernflowerLogger.Severity.ERROR)
                .getConfiguration();
    }

    /**
     * Uploads byte code to current fernflower decompiler context
     *
     * @param byteCode byte code of class
     */
    private StructClass createStructClass(byte[] byteCode) {
        StructClass structClass = createClassStruct(byteCode);

        StructContext structContext = DecompilerContext.getStructContext();
        Map<String, StructClass> classes = structContext.getClasses();
        classes.put(structClass.qualifiedName, structClass);

        return structClass;
    }

    /**
     * Creates class structure from byte code
     *
     * @param byteCode byte code of class
     * @return struct class instance
     */
    private StructClass createClassStruct(byte[] byteCode) {
        try {
            LazyLoader lazyLoader = new LazyLoader((externalPath, internalPath) -> byteCode);
            StructClass structClass = new StructClass(byteCode, true, lazyLoader);
            LazyLoader.Link link = new LazyLoader.Link("", structClass.qualifiedName);
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
    private static class NothingSaver extends ConsoleDecompiler {
        /**
         * Default constructor for initialize of decompiled code saver
         *
         * @param destination destination stub
         * @param options     options stub
         */
        public NothingSaver(File destination, Map<String, Object> options, IFernflowerLogger logger) {
            super(destination, options, logger);
        }

        @Override
        public void saveFolder(String path) {
        }
    }
}