package com.classparser.bytecode.decompile.javap;

import com.classparser.bytecode.api.Decompiler;
import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.bytecode.decompile.javap.configuration.JavaPrinterBuilderConfiguration;
import com.classparser.bytecode.decompile.javap.configuration.JavaPrinterConfiguration;
import com.classparser.bytecode.exception.DecompilationException;
import com.classparser.bytecode.utils.ClassNameConverter;
import com.classparser.util.ConfigurationUtils;
import com.sun.tools.javap.Context;
import com.sun.tools.javap.InstructionDetailWriter;
import com.sun.tools.javap.JavapTask;
import com.sun.tools.javap.Messages;
import com.sun.tools.javap.Options;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.classparser.bytecode.decompile.javap.configuration.JavaPrinterConfiguration.*;

/**
 * Adapter of Java Printer disassembler for {@link Decompiler} API
 * This disassembler is standard tool and deliver with jdk
 * Disassembler version uses on depend current jdk
 * <p>
 * This disassembler can disassemble all types bytecode of classes
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public final class JavaPrinterDisassembler implements Decompiler {

    private final ConfigurationUtils utils;

    public JavaPrinterDisassembler() {
        this.utils = new ConfigurationUtils(getDefaultConfiguration());
    }

    @Override
    public String decompile(byte[] bytecode, Collection<byte[]> classes) {
        if (bytecode != null && classes != null) {
            Map<String, byte[]> bytecodeMap = new HashMap<>();
            String className = ClassNameConverter.getClassName(bytecode);
            bytecodeMap.put(className, bytecode);
            for (byte[] innerClassByteCode : classes) {
                bytecodeMap.put(ClassNameConverter.getClassName(innerClassByteCode), innerClassByteCode);
            }

            List<String> classesList = new ArrayList<>();
            classesList.add(className);

            Context context = new Context();
            context.put(Messages.class, new JavaPrinterMessages());
            context.put(Options.class, prepareAndGetOptions(context));

            List<String> options = new ArrayList<>();
            if (!classes.isEmpty()) {
                options.add("-XDinner");
            }

            StringPrintWriter stringPrintWriter = new StringPrintWriter();
            JavapTask task = new BytecodeJavaPrinterTask(stringPrintWriter, options, classesList, bytecodeMap, context);

            task.run();

            return stringPrintWriter.getDisassembledCode();
        } else {
            throw new DecompilationException("Bytecode of classes for decompilation can't be a null!");
        }
    }

    /**
     * Parses and creates options for java printer disassembler
     */
    private Options prepareAndGetOptions(Context context) {
        Options options = Options.instance(context);

        options.showAllAttrs = utils.getConfigOption(DISPLAY_ATTRIBUTES_OF_CODE_KEY, Boolean.class);
        options.showDisassembled = utils.getConfigOption(DISPLAY_DECOMPILE_CODE_KEY, Boolean.class);
        options.showLineAndLocalVariableTables =
                utils.getConfigOption(DISPLAY_CODE_LINE_AND_LOCAL_VARIABLE_KEY, Boolean.class);
        options.sysInfo = utils.getConfigOption(DISPLAY_SYSTEM_INFORMATION_KEY, Boolean.class);
        options.verbose = utils.getConfigOption(DISPLAY_VERBOSE_INFORMATION_KEY, Boolean.class);
        options.showConstants = utils.getConfigOption(DISPLAY_CONSTANTS_KEY, Boolean.class);
        options.indentWidth = utils.getConfigOption(INDENT_COUNT_SPACES_KEY, Integer.class);

        for (Object access : utils.getConfigOption(DISPLAY_MODIFIER_ACCESSOR_KEY, Set.class)) {
            if (access instanceof AccessModifier) {
                AccessModifier accessModifier = (AccessModifier) access;
                options.showAccess += accessModifier.getModifier();
                options.accessOptions.add(accessModifier.getName());
            }
        }

        for (Object object : utils.getConfigOption(APPEND_DISPLAY_DETAILS_KEY, Set.class)) {
            if (object instanceof InstructionDetailWriter.Kind) {
                InstructionDetailWriter.Kind kind = (InstructionDetailWriter.Kind) object;
                options.details.add(kind);
            }
        }

        options.showDescriptors = utils.getConfigOption(DISPLAY_DESCRIPTORS_KEY, Boolean.class);

        return options;
    }

    @Override
    public void setConfigurationManager(ConfigurationManager configurationManager) {
        if (configurationManager != null) {
            this.utils.reloadConfiguration(configurationManager.getCustomDecompilerConfiguration());
        } else {
            throw new NullPointerException("Configuration manager is can't be a null!");
        }
    }

    /**
     * Creates default java printer configuration
     * Describe of option can Java Printer configuration
     *
     * @return default configuration map
     * @see JavaPrinterConfiguration
     */
    private Map<String, Object> getDefaultConfiguration() {
        return JavaPrinterBuilderConfiguration
                .createBuilder()
                .displayAllAttributesOfCode(false)
                .displayCodeLineAndLocalVariableTable(false)
                .displayConstants(true)
                .displayDecompiledCode(true)
                .displaySystemInformation(false)
                .displayVerboseInformation(false)
                .displayDescriptors(false)
                .setIndentSpaces(4)
                .appendDisplayOnlyElementsWithAccessModifier(AccessModifier.PUBLIC)
                .appendDisplayOnlyElementsWithAccessModifier(AccessModifier.PRIVATE)
                .appendDisplayOnlyElementsWithAccessModifier(AccessModifier.PROTECTED)
                .appendDisplayOnlyElementsWithAccessModifier(AccessModifier.PACKAGE)
                .appendDisplayDetails(InstructionDetailWriter.Kind.TYPE_ANNOS)
                .appendDisplayDetails(InstructionDetailWriter.Kind.TRY_BLOCKS)
                .getConfiguration();
    }

    /**
     * Implementation of {@link SimpleJavaFileObject} which create class input stream
     * from bytecode of class
     */
    private static class ByteArrayJavaFileObject extends SimpleJavaFileObject {

        private static final String EMPTY_URI = "file://class";

        private final byte[] bytecode;

        private final String className;

        /**
         * Default constructor for initialize of {@link ByteArrayJavaFileObject}
         *
         * @param bytecode bytecode of class
         */
        ByteArrayJavaFileObject(byte[] bytecode) {
            super(URI.create(EMPTY_URI), Kind.CLASS);
            this.bytecode = bytecode;
            this.className = ClassNameConverter.getClassName(bytecode);
        }

        @Override
        public String getName() {
            return className;
        }

        @Override
        public InputStream openInputStream() {
            return new ByteArrayInputStream(bytecode);
        }
    }

    /**
     * Implementation of {@link JavapTask} uses {@link ByteArrayJavaFileObject}
     * for loading class files to current process of task
     */
    private static class BytecodeJavaPrinterTask extends JavapTask {

        private static final DiagnosticListener<JavaFileObject> STUB = (empty) -> {
        };

        private final Map<String, byte[]> bytecodeMap;

        /**
         * Default constructor for initialize of {@link BytecodeJavaPrinterTask}
         *
         * @param writer      out class writer
         * @param options     java printer task options
         * @param classes     classes for disassemble process
         * @param bytecodeMap bytecode map of classes
         * @param context     current disassemble context
         */
        BytecodeJavaPrinterTask(Writer writer,
                                Iterable<String> options,
                                Iterable<String> classes,
                                Map<String, byte[]> bytecodeMap,
                                Context context) {
            super(writer, null, STUB, options, classes);
            this.bytecodeMap = bytecodeMap;
            this.context = context;
        }

        @Override
        protected JavaFileObject open(String className) {
            byte[] bytes = bytecodeMap.get(className);
            return new ByteArrayJavaFileObject(bytes);
        }
    }

    /**
     * Implementation of {@link PrintWriter} which store result of {@link #println(Object)}
     * operation to StringBuilder
     */
    private static class StringPrintWriter extends PrintWriter {

        private final StringBuilder stringBuilder;

        /**
         * Default constructor for initialize of {@link StringPrintWriter}
         */
        StringPrintWriter() {
            super(new StringWriter());
            this.stringBuilder = new StringBuilder();
        }

        @Override
        public void println(Object object) {
            stringBuilder.append(object).append("\n");
        }

        /**
         * Obtains disassembled code of class
         *
         * @return disassembled code of class
         */
        String getDisassembledCode() {
            return stringBuilder.toString();
        }
    }

    /**
     * {@link Messages} stub for store in context
     */
    private static class JavaPrinterMessages implements Messages {

        @Override
        public String getMessage(String string, Object... objects) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getMessage(Locale locale, String string, Object... objects) {
            throw new UnsupportedOperationException();
        }
    }
}