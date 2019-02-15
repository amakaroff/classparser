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
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructContext;
import org.jetbrains.java.decompiler.struct.lazy.LazyLoader;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Adapter of Fernflower decompiler for {@link Decompiler} API
 * This decompiler was written of Egor Ushakov
 * Decompiler version: 4.2.0.Final (Oct 26, 2018)
 * <p>
 * Fernflower decompiler support java 8 syntax and can decompile all inner classes
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public final class FernflowerDecompiler implements Decompiler {

    private volatile Map<String, Object> configurationMap;

    public FernflowerDecompiler() {
        this.configurationMap = getDefaultConfiguration();
    }

    @Override
    public String decompile(byte[] bytecode, Collection<byte[]> classes) {
        if (bytecode != null && classes != null) {
            IResultSaver decompiledCodeSaver = new ConsoleDecompiler(null, null);
            IFernflowerLogger logger = new PrintStreamLogger(System.out);

            Fernflower fernflower = new Fernflower(null, decompiledCodeSaver, configurationMap, logger);

            StructContext structContext = fernflower.getStructContext();
            StructClass mainStructClass = createAndSetStructClass(structContext, bytecode);
            for (byte[] nestedClass : classes) {
                createAndSetStructClass(structContext, nestedClass);
            }

            return decompile(fernflower, mainStructClass);
        } else {
            throw new DecompilationException("Bytecode of classes for decompilation can't be a null!");
        }
    }

    /**
     * Starts decompile context process
     *
     * @param fernflower fernflower decompiler instance
     */
    private String decompile(Fernflower fernflower, StructClass structClass) {
        try {
            fernflower.decompileContext();
            return fernflower.getClassContent(structClass);
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
     * @return struct of class
     */
    private StructClass createAndSetStructClass(StructContext structContext, byte[] bytecode) {
        StructClass structClass = createClassStruct(bytecode);

        Map<String, StructClass> classes = structContext.getClasses();
        classes.put(structClass.qualifiedName, structClass);

        return structClass;
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
                if (configurationMap != null) {
                    Map<String, Object> currentConfiguration = getDefaultConfiguration();
                    currentConfiguration.putAll(configurationMap);
                    this.configurationMap = currentConfiguration;
                } else {
                    throw new NullPointerException("Configuration Map is can't be null!");
                }
            } else {
                throw new NullPointerException("Decompiler configuration is can't be null!");
            }
        } else {
            throw new NullPointerException("Configuration manager is can't be null!");
        }
    }
}