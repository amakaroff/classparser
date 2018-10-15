package com.classparser.bytecode.decompile.javap;

import com.classparser.bytecode.api.Decompiler;
import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.bytecode.decompile.javap.configuration.JavapBuilderConfiguration;
import com.classparser.bytecode.decompile.javap.configuration.JavapConfiguration;
import com.classparser.bytecode.utils.ClassNameConverter;
import com.classparser.util.ConfigurationUtils;
import com.classparser.util.Reflection;
import com.sun.tools.javap.Context;
import com.sun.tools.javap.InstructionDetailWriter;
import com.sun.tools.javap.JavapTask;
import com.sun.tools.javap.Messages;
import com.sun.tools.javap.Options;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.classparser.bytecode.decompile.javap.configuration.JavapConfiguration.*;

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

    private static final DiagnosticListener<JavaFileObject> STUB = (empty) -> {
    };

    private final ConfigurationUtils utils;

    /**
     * Default constructor for initialize {@link JavaPrinterDisassembler}
     */
    public JavaPrinterDisassembler() {
        this.utils = new ConfigurationUtils(new HashMap<>(), getDefaultConfiguration());
    }

    @Override
    public String decompile(byte[] bytecode) {
        return decompile(bytecode, Collections.emptyList());
    }

    @Override
    public String decompile(byte[] bytecode, Collection<byte[]> classes) {
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
        Options options = getOptions(context);

        if (!classes.isEmpty()) {
            options.showInnerClasses = true;
        }

        StringPrintWriter stringPrintWriter = new StringPrintWriter();
        JavapTask task = new BytecodeJavaPrinterTask(stringPrintWriter, null, STUB, null,
                classesList, bytecodeMap, context);

        setOptionsField(task, options);

        task.run();

        return stringPrintWriter.getDisassembledCode();
    }

    /**
     * Parses and creates options for java printer disassembler
     *
     * @return disassembler options
     */
    private Options getOptions(Context context) {
        Options options = new JavaPrinterOptions(context);

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

        context.put(Options.class, options);

        return options;
    }

    @Override
    public void setConfigurationManager(ConfigurationManager configurationManager) {
        this.utils.reloadConfiguration(configurationManager.getCustomDecompilerConfiguration());
    }

    /**
     * Creates default java printer configuration
     * Describe of option can Java Printer configuration
     *
     * @return default configuration map
     * @see JavapConfiguration
     */
    private Map<String, Object> getDefaultConfiguration() {
        return JavapBuilderConfiguration
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
     * Set value to field uses reflection mechanism
     *
     * @param object     any object
     * @param fieldValue value what should be set to field
     */
    private void setOptionsField(Object object, Object fieldValue) {
        Field field = Reflection.getField(JavapTask.class, "options");
        Reflection.set(field, object, fieldValue);
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

        private final Map<String, byte[]> bytecodeMap;

        /**
         * Default constructor for initialize of {@link BytecodeJavaPrinterTask}
         *
         * @param writer
         * @param javaFileManager
         * @param diagnosticListener
         * @param options
         * @param classes
         * @param bytecodeMap        bytecode map of classes
         * @param context            current disassemble context
         */
        public BytecodeJavaPrinterTask(Writer writer,
                                       JavaFileManager javaFileManager,
                                       DiagnosticListener<? super JavaFileObject> diagnosticListener,
                                       Iterable<String> options,
                                       Iterable<String> classes,
                                       Map<String, byte[]> bytecodeMap,
                                       Context context) {
            super(writer, javaFileManager, diagnosticListener, options, classes);
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
            super(new CharArrayWriter());
            this.stringBuilder = new StringBuilder();
        }

        @Override
        public void println(Object obj) {
            stringBuilder.append(String.valueOf(obj)).append("\n");
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
     * {@link Options} stub for create public constructor
     */
    private static class JavaPrinterOptions extends Options {

        /**
         * Default constructor for initialize of {@link JavaPrinterOptions}
         *
         * @param context current context of disassemble process
         */
        JavaPrinterOptions(Context context) {
            super(context);
        }
    }

    /**
     * {@link Messages} stub for store in context
     */
    private static class JavaPrinterMessages implements Messages {

        @Override
        public String getMessage(String s, Object... objects) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getMessage(Locale locale, String s, Object... objects) {
            throw new UnsupportedOperationException();
        }
    }
}