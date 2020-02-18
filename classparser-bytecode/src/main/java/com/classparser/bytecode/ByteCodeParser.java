package com.classparser.bytecode;

import com.classparser.api.ClassParser;
import com.classparser.bytecode.api.ByteCodeCollector;
import com.classparser.bytecode.api.Decompiler;
import com.classparser.bytecode.collector.ChainByteCodeCollector;
import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.bytecode.exception.ByteCodeParserException;
import com.classparser.bytecode.exception.classes.IllegalClassException;
import com.classparser.bytecode.saver.BytecodeFileSaver;
import com.classparser.bytecode.utils.ClassNameConverter;
import com.classparser.bytecode.utils.InnerClassesCollector;
import com.classparser.configuration.Configuration;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implementation of {@link ClassParser} provides
 * functionality for parsing class by byte code decompilation
 * That parsing mechanism can parse only normal class and can't
 * parse arrays and primitives
 * <p>
 * This class is thread safe
 *
 * @author Aleksey Makarov
 * @since 1.0.0
 */
public class ByteCodeParser implements ClassParser {

    private static final String DUMP_PROPERTY = "java.lang.invoke.MethodHandle.DUMP_CLASS_FILES";

    private static final String DUMPER_CLASS = "java.lang.invoke.InvokerBytecodeGenerator";

    static {
        if (Boolean.getBoolean(DUMP_PROPERTY)) {
            String illegalMessage = "Dumping class files to DUMP_CLASS_FILES/...";
            Pattern messagePattern = Pattern.compile("dump: DUMP_CLASS_FILES\\\\.*?\\.class");
            
            System.setOut(new PrintStream(System.out) {
                @Override
                public void println(String data) {
                    if (isIllegalMessage(data)) {
                        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                        if (isInvokeFromDumper(stackTrace)) {
                            return;
                        }
                    }

                    super.println(data);
                }
                
                private boolean isIllegalMessage(String message) {
                    return message.equals(illegalMessage) || messagePattern.matcher(message).matches(); 
                }

                private boolean isInvokeFromDumper(StackTraceElement[] stackTrace) {
                    return stackTrace.length > 3 && stackTrace[2].getClassName().startsWith(DUMPER_CLASS);
                }
            });
        }
    }

    private final ConfigurationManager configurationManager;
    private final BytecodeFileSaver saver;
    private final InnerClassesCollector classesCollector;
    private final ByteCodeCollector bytecodeCollector;

    public ByteCodeParser() {
        this.configurationManager = new ConfigurationManager();
        this.bytecodeCollector = new ChainByteCodeCollector(configurationManager);
        this.classesCollector = new InnerClassesCollector(configurationManager);
        this.saver = new BytecodeFileSaver(configurationManager);
    }

    @Override
    public String parseClass(Class<?> clazz) throws ByteCodeParserException {
        checkToCorrectClass(clazz);

        byte[] byteCode = getByteCodeOfClass(clazz);
        List<byte[]> byteCodeOfInnerClasses = getByteCodeOfInnerClasses(clazz);

        if (configurationManager.isSaveToFile()) {
            saveByteCodeToFile(byteCode, byteCodeOfInnerClasses);
        }

        Decompiler decompiler = configurationManager.getDecompiler();
        decompiler.setConfigurationManager(configurationManager);

        return decompiler.decompile(byteCode, byteCodeOfInnerClasses);
    }

    /**
     * Saves collected byte code to file
     *
     * @param byteCode               byte code of file
     * @param byteCodeOfInnerClasses byte code of inner classes
     */
    private void saveByteCodeToFile(byte[] byteCode, List<byte[]> byteCodeOfInnerClasses) {
        saver.saveToFile(byteCode);
        for (byte[] byteCodeOfInnerClass : byteCodeOfInnerClasses) {
            saver.saveToFile(byteCodeOfInnerClass);
        }
    }

    /**
     * Obtains byte code of inner classes for class
     *
     * @param clazz any class
     * @return list with byte code of inner classes
     */
    private List<byte[]> getByteCodeOfInnerClasses(Class<?> clazz) {
        if (configurationManager.isDecompileInnerClasses()) {
            List<byte[]> byteCodeOfInnerClasses = new ArrayList<>();

            for (Class<?> innerClass : classesCollector.getInnerClasses(clazz)) {
                byte[] byteCodeOfInnerClass = bytecodeCollector.getByteCode(innerClass);
                if (byteCodeOfInnerClass != null) {
                    byteCodeOfInnerClasses.add(byteCodeOfInnerClass);
                }
            }

            return byteCodeOfInnerClasses;
        }

        return Collections.emptyList();
    }

    /**
     * Obtains byte code of class
     *
     * @param clazz any class
     * @return byte code of class
     */
    private byte[] getByteCodeOfClass(Class<?> clazz) {
        byte[] byteCode = bytecodeCollector.getByteCode(clazz);
        if (byteCode == null) {
            String className = ClassNameConverter.toJavaClassName(clazz);
            throw new ByteCodeParserException("Byte code of class: \"" + className + "\" is not found");
        }

        return byteCode;
    }

    /**
     * Checks of for class can be obtained byte code
     *
     * @param clazz any class
     */
    private void checkToCorrectClass(Class<?> clazz) {
        if (clazz == null) {
            throw new NullPointerException("Class for parsing is can't be a null");
        }

        if (clazz.isPrimitive()) {
            String className = ClassNameConverter.toJavaClassName(clazz);
            throw new IllegalClassException("Primitive type: \"" + className + "\" can'tbe decompiled", clazz);
        }

        if (clazz.isArray()) {
            String simpleName = ClassNameConverter.toJavaClassSimpleName(clazz);
            throw new IllegalClassException("Array type: \"" + simpleName + "\" can't be decompiled", clazz);
        }
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        configurationManager.reloadConfiguration(configuration);
    }
}