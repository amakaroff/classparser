package com.classparser.bytecode;

import com.classparser.api.ClassParser;
import com.classparser.bytecode.api.BytecodeCollector;
import com.classparser.bytecode.api.Decompiler;
import com.classparser.bytecode.collector.ChainBytecodeCollector;
import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.bytecode.exception.ByteCodeParserException;
import com.classparser.bytecode.saver.BytecodeFileSaver;
import com.classparser.bytecode.utils.ClassNameConverter;
import com.classparser.bytecode.utils.InnerClassesCollector;
import com.classparser.configuration.Configuration;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link ClassParser} provides
 * functionality for parsing class by bytecode decompilation
 * That parsing mechanism can parse only normal class and can't
 * parse arrays and primitives
 * <p>
 * This class is thread safe
 *
 * @author Aleksey Makarov
 * @since 1.0.0
 */
public class BytecodeParser implements ClassParser {

    private static final String DUMP_PROPERTY = "java.lang.invoke.MethodHandle.DUMP_CLASS_FILES";

    private static final String DUMPER_CLASS = "java.lang.invoke.InvokerBytecodeGenerator";

    private final ConfigurationManager configurationManager;

    private final BytecodeFileSaver bytecodeFileSaver;

    private final InnerClassesCollector classesCollector;

    private final BytecodeCollector bytecodeCollector;

    static {
        if (Boolean.getBoolean(DUMP_PROPERTY)) {
            System.setOut(new PrintStream(System.out) {
                @Override
                public void println(String data) {
                    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                    if (!isInvokeFromDumper(stackTrace)) {
                        super.println(data);
                    }
                }

                private boolean isInvokeFromDumper(StackTraceElement[] stackTrace) {
                    return stackTrace.length > 3 && stackTrace[2].getClassName().startsWith(DUMPER_CLASS);
                }
            });
        }
    }

    public BytecodeParser() {
        this.configurationManager = new ConfigurationManager();
        this.bytecodeCollector = new ChainBytecodeCollector(configurationManager);
        this.classesCollector = new InnerClassesCollector(configurationManager);
        this.bytecodeFileSaver = new BytecodeFileSaver(configurationManager);
    }

    @Override
    public String parseClass(Class<?> clazz) throws ByteCodeParserException {
        validateCorrectnessOfClass(clazz);

        byte[] bytecode = getBytecodeOfClass(clazz);
        List<byte[]> bytecodeOfInnerClasses = getBytecodeOfInnerClasses(clazz);

        if (configurationManager.isSaveToFile()) {
            saveByteCodeToFile(bytecode, bytecodeOfInnerClasses);
        }

        Decompiler decompiler = configurationManager.getDecompiler();
        decompiler.setConfigurationManager(configurationManager);

        return decompiler.decompile(bytecode, bytecodeOfInnerClasses);
    }

    /**
     * Saves collected bytecode to file
     *
     * @param bytecode               bytecode of file
     * @param bytecodeOfInnerClasses bytecode of inner classes
     */
    private void saveByteCodeToFile(byte[] bytecode, List<byte[]> bytecodeOfInnerClasses) {
        bytecodeFileSaver.saveToFile(bytecode);
        for (byte[] bytecodeOfInnerClass : bytecodeOfInnerClasses) {
            bytecodeFileSaver.saveToFile(bytecodeOfInnerClass);
        }
    }

    /**
     * Obtains bytecode of inner classes for class
     *
     * @param clazz any class
     * @return list with bytecode of inner classes
     */
    private List<byte[]> getBytecodeOfInnerClasses(Class<?> clazz) {
        if (configurationManager.isNeedToDecompileInnerClasses()) {
            List<byte[]> bytecodeOfInnerClasses = new ArrayList<>();

            for (Class<?> innerClass : classesCollector.getInnerClasses(clazz)) {
                byte[] bytecodeOfInnerClass = bytecodeCollector.getBytecode(innerClass);
                if (bytecodeOfInnerClass != null) {
                    bytecodeOfInnerClasses.add(bytecodeOfInnerClass);
                }
            }

            return bytecodeOfInnerClasses;
        }

        return Collections.emptyList();
    }

    /**
     * Obtains bytecode of class
     *
     * @param clazz any class
     * @return bytecode of class
     */
    private byte[] getBytecodeOfClass(Class<?> clazz) {
        byte[] bytecode = bytecodeCollector.getBytecode(clazz);
        if (bytecode == null) {
            String className = ClassNameConverter.toJavaClassName(clazz);
            throw new ByteCodeParserException("Byte code of class: \"" + className + "\" is not found!");
        }

        return bytecode;
    }

    /**
     * Checks of for class can be obtain bytecode
     *
     * @param clazz any class
     */
    private void validateCorrectnessOfClass(Class<?> clazz) {
        if (clazz == null) {
            throw new NullPointerException("Class for parsing can't be a null!");
        }

        if (clazz.isPrimitive()) {
            String className = ClassNameConverter.toJavaClassName(clazz);
            throw new IllegalArgumentException("Primitive type: \"" + className + "\" can't be decompiled");
        }

        if (clazz.isArray()) {
            String simpleName = ClassNameConverter.toJavaClassSimpleName(clazz);
            throw new IllegalArgumentException("Array type: \"" + simpleName + "\" can't be decompiled");
        }
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        configurationManager.reloadConfiguration(configuration);
    }
}