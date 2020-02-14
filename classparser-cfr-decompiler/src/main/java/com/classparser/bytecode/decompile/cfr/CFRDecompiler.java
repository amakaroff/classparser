package com.classparser.bytecode.decompile.cfr;

import com.classparser.bytecode.api.BytecodeCollector;
import com.classparser.bytecode.api.Decompiler;
import com.classparser.bytecode.collector.ChainBytecodeCollector;
import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.bytecode.decompile.cfr.configuration.CFRBuilderConfiguration;
import com.classparser.bytecode.exception.decompile.DecompilationException;
import com.classparser.bytecode.utils.ClassNameConverter;
import com.classparser.util.ConfigurationUtils;
import org.benf.cfr.reader.api.ClassFileSource;
import org.benf.cfr.reader.apiunreleased.ClassFileSource2;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.state.ClassFileSourceImpl;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.state.TypeUsageCollectorImpl;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.util.CannotLoadClassException;
import org.benf.cfr.reader.util.bytestream.BaseByteData;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.getopt.GetOptParser;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.output.Dumper;
import org.benf.cfr.reader.util.output.IllegalIdentifierDump;
import org.benf.cfr.reader.util.output.StdIODumper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.classparser.bytecode.decompile.cfr.configuration.CFRConfiguration.INT_OPTIONS;

/**
 * Adapter of CFR decompiler for {@link Decompiler} API
 * This decompiler was written of Lee Benfield
 * Decompiler version: 0.132
 * <p>
 * CFR decompiler supports java 8 syntax
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public final class CFRDecompiler implements Decompiler {

    private final ConfigurationUtils utils;

    private volatile ConfigurationManager configurationManager;

    public CFRDecompiler() {
        this.utils = new ConfigurationUtils(new HashMap<>(), getDefaultConfiguration());
    }

    @Override
    public String decompile(byte[] byteCode) {
        return decompile(byteCode, Collections.emptyList());
    }

    @Override
    public String decompile(byte[] byteCode, Collection<byte[]> classes) {
        if (byteCode != null && classes != null) {
            String className = ClassNameConverter.getClassName(byteCode);

            GetOptParser getOptParser = new GetOptParser();
            String[] defaultOptions = getDefaultOptions(className);
            Pair<List<String>, Options> parse = getOptParser.parse(defaultOptions, OptionsImpl.getFactory());

            Options options = parse.getSecond();
            ClassFileSource2 classFileSource = new ClassFileSourceImpl(options);
            DCCommonState dcCommonState = new CFRDCCommonState(options, classFileSource, byteCode, classes);

            ClassFile classFile = dcCommonState.getClassFileMaybePath(className);
            TypeUsageCollector collectingDumper = new TypeUsageCollectorImpl(options, classFile);
            IllegalIdentifierDump illegalIdentifierDump = IllegalIdentifierDump.Factory.get(options);
            dcCommonState.configureWith(classFile);

            classFile.loadInnerClasses(dcCommonState);
            classFile.analyseTop(dcCommonState);
            classFile.collectTypeUsages(collectingDumper);

            TypeUsageInformation typeUsageInformation = collectingDumper.getTypeUsageInformation();
            Dumper dumper = new CFRBuilderDumper(typeUsageInformation, options, illegalIdentifierDump);
            classFile.dump(dumper);

            return dumper.toString();
        }

        throw new DecompilationException("Byte code of classes for decompilation can't be a null!");
    }

    /**
     * Parses a current configuration and create decompiler options
     *
     * @param className decompiled class name
     * @return array of decompiler options
     */
    private String[] getDefaultOptions(String className) {
        List<String> options = new ArrayList<>();

        options.add(className);

        Set<String> optionKeys = getDefaultConfiguration().keySet();
        for (String key : optionKeys) {
            options.add("--" + key);
            options.add(utils.getConfigOption(key, getTypeByKey(key)).toString());
        }

        return options.toArray(new String[0]);
    }

    /**
     * Returns value type of key
     *
     * @param key property key
     * @return type of value
     */
    private Class<?> getTypeByKey(String key) {
        if (INT_OPTIONS.contains(key)) {
            return Integer.class;
        } else {
            return Boolean.class;
        }
    }

    /**
     * Creates default configuration for decompiler
     * Commented properties it's decompiler bug
     *
     * @return default configuration
     * @see CFRBuilderConfiguration for default values
     */
    private Map<String, Object> getDefaultConfiguration() {
        return CFRBuilderConfiguration
                .getBuilderConfiguration()
                .replaceStringConcatToStringBuffer(false)
                .replaceStringConcatToStringBuilder(false)
                .decompileSugarEnumInSwitch(true)
                .decompileSugarInEnums(true)
                .decompileSugarStringInEnums(true)
                .decompileSugarInArrayIteration(true)
                .decompilerTryWithResourceConstruction(true)
                .decompileSugarInCollectionIteration(true)
                .decompileLambdaFunctions(true)
                .decompileInnerClasses(true)
                .skipBatchInnerClasses(false)
                .hideUTF8Characters(true)
                .hideVeryLongStrings(false)
                .removeBoilerplateFunctions(true)
                .removeInnerClassesSynthetics(true)
                .hideBridgeMethods(true)
                .relinkConstString(true)
                .liftInitialisationToAllConstructors(true)
                .removeDeadMethods(false)
                .removeBadGenerics(false)
                .decompileSugarInAsserts(true)
                .decompileBoxing(true)
                .showCFRVersion(false)
                .decompileSugarInFinally(true)
                .removeSupportCodeForMonitors(false)
                .replaceMonitorWithComments(false)
                .lenientSituationsWhereThrowException(true)
                .dumpClassPathForDebuggingPurposes(true)
                .showDecompilerMessages(true)
                .forceBasicBlockSorting(true)
                .allowForLoopsToAggressivelyRollMutations(true)
                .forceTopSortAggressive(true)
                .forceCodePropagate(true)
                .forceReturnIngifs(true)
                .ignoreExceptionsAlways(false)
                .ignoreExceptions(false)
                .forceExceptionPrune(true)
                .removeNestedExceptionsHandlers(false)
                .splitLifetimesAnalysisCausedType(false)
                .recoverTypeHintsForIterators(true)
                //.showDebugInfo(0) //Not working in current version
                .doNotDisplayStateWhile(true)
                .allowMoreAggressiveOptions(true)
                .enableEclipseTransformations(false)
                .generateOverrideAnnotations(true)
                .decorateMethodsWithExplicitTypes(true)
                .allowTransformationsWhichCorrectErrors(true)
                .allowCodeUsesLabelledBlocks(true)
                .reverseOldJavaClassObjectConstruction(true)
                .hideDefaultImports(true)
                .decompileSpecificallyWithRecoveryOptions(0)
                .renameAll(false)
                .renameDuplicateFields(false)
                .renameSmallMembers(0)
                .renameInvalidIdentifiers(false)
                .renameEnumIdentifiers(false)
                .countAtWhichToTriggerAggressiveReductions(15000)
                //.removeReturnFromStaticInit(true) //Not working in current version
                .useLocalVariableTableIfExits(true)
                .pullCodeIntoCaseStatements(true)
                .elideThingsInScalaOutput(true)
                .getConfiguration();
    }

    @Override
    public void setConfigurationManager(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
        this.utils.reloadConfiguration(configurationManager.getCustomDecompilerConfiguration());
    }

    /**
     * Class extends {@link StdIODumper} and implements any fixes by correcting decompiled code
     */
    private static class CFRBuilderDumper extends StdIODumper {

        private static final String CEF_INFO = "/*\n * Decompiled with CFR.\n */\n";

        private final StringBuilder builder;

        private boolean skipLineSeparator;

        /**
         * Default constructor for initialize {@link CFRBuilderDumper}
         *
         * @param typeUsageInformation  information about use type
         * @param options               decompiler options
         * @param illegalIdentifierDump illegal identifier dump instance
         */
        private CFRBuilderDumper(TypeUsageInformation typeUsageInformation,
                                 Options options,
                                 IllegalIdentifierDump illegalIdentifierDump) {
            super(typeUsageInformation, options, illegalIdentifierDump, 0);
            this.builder = new StringBuilder();
            this.skipLineSeparator = false;
        }

        @Override
        protected void write(String data) {
            builder.append(getCorrectData(data));
        }

        /**
         * Tryings fix line separator for signature of class
         *
         * @param data any class data
         * @return corrected data
         */
        private String getCorrectData(String data) {
            if ((data.equals("class ") ||
                 data.equals("interface ") ||
                 data.equals("enum ") ||
                 data.equals("@interface ")) && !skipLineSeparator) {
                skipLineSeparator = true;
            }

            if (data.equals("{")) {
                skipLineSeparator = false;
            }

            if (skipLineSeparator && data.contains("    ")) {
                data = "";
            }

            if (skipLineSeparator && data.contains("\n")) {
                data = " ";
            }

            return data;
        }

        @Override
        public String toString() {
            return builder.toString().replace(CEF_INFO, "");
        }

        @Override
        public void close() {
            builder.delete(0, builder.length());
        }

        @Override
        public Dumper withTypeUsageInformation(TypeUsageInformation innerclassTypeUsageInformation) {
            return this;
        }
    }

    /**
     * Class extends {@link DCCommonState} and overrides same methods by loading {@link ClassFile} instances
     * by class full name
     */
    private class CFRDCCommonState extends DCCommonState {

        private final String outerClassName;

        private final BytecodeCollector codeCollector;

        private final Map<String, ClassFile> classFileCache;

        /**
         * Default constructor for initialize {@link CFRDCCommonState} instance
         *
         * @param options         decompiler options
         * @param classFileSource {@link ClassFileSource} instance of based decompile class
         * @param byteCode        byte code of based decompile class
         * @param innerClasses    collection of all inner classes
         */
        private CFRDCCommonState(Options options,
                                 ClassFileSource2 classFileSource,
                                 byte[] byteCode,
                                 Collection<byte[]> innerClasses) {
            super(options, classFileSource);
            this.outerClassName = ClassNameConverter.toJavaClassName(byteCode);
            this.codeCollector = new ChainBytecodeCollector(configurationManager);
            this.classFileCache = new HashMap<>();

            convertClassFileFromByteCode(byteCode);
            for (byte[] innerClass : innerClasses) {
                convertClassFileFromByteCode(innerClass);
            }
        }

        @Override
        public ClassFile getClassFile(String path) throws CannotLoadClassException {
            return loadClassFileAtPath(path);
        }

        /**
         * Performs obtain {@link ClassFile} instance by class name
         *
         * @param className class name
         * @return {@link ClassFile} instance
         * @throws CannotLoadClassException if can't obtain {@link ClassFile} instance by name
         */
        public ClassFile loadClassFileAtPath(String className) throws CannotLoadClassException {
            className = ClassNameConverter.toJavaClassName(className);

            ClassFile classFile = classFileCache.get(className);
            if (classFile != null) {
                return classFile;
            }

            if (className.contains(outerClassName + '$')) {
                throw new CannotLoadClassException("", null);
            }

            return convertClassFileFromByteCode(getByteCode(className));
        }

        /**
         * Tryings load class by name
         *
         * @param className class full name
         * @return class instance
         * @throws CannotLoadClassException if class not found
         */
        private Class<?> loadClass(String className) throws CannotLoadClassException {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException exception) {
                throw new CannotLoadClassException("Can't load class: " + className, exception);
            }
        }

        /**
         * Tryings collect byte code of class by name
         *
         * @param className name of class
         * @return byte code of class
         * @throws CannotLoadClassException if byte code of class is not found
         */
        private byte[] getByteCode(String className) throws CannotLoadClassException {
            Class<?> clazz = loadClass(className);
            byte[] byteCode = codeCollector.getBytecode(clazz);

            if (byteCode == null) {
                throw new CannotLoadClassException("Can't load class: " + className, null);
            }

            return byteCode;
        }

        @Override
        public ClassFile getClassFileMaybePath(String pathOrName) throws CannotLoadClassException {
            return loadClassFileAtPath(pathOrName);
        }

        /**
         * Performs converting byte code of class to {@link ClassFile} instance
         * and put this entity to {@link #classFileCache}
         *
         * @param byteCode byte code of class
         * @return {@link ClassFile} instance
         */
        private ClassFile convertClassFileFromByteCode(byte[] byteCode) {
            String className = ClassNameConverter.toJavaClassName(byteCode);

            ByteData data = new BaseByteData(byteCode);
            ClassFile classFile = new ClassFile(data, className, this);
            classFileCache.put(className, classFile);

            return classFile;
        }
    }
}