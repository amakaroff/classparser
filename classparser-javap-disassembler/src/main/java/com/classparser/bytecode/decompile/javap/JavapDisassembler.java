package com.classparser.bytecode.decompile.javap;

import com.classparser.bytecode.api.Decompiler;
import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.bytecode.decompile.javap.configuration.JavapBuilderConfiguration;
import com.classparser.bytecode.decompile.javap.configuration.JavapConfiguration;
import com.classparser.bytecode.utils.ClassNameConverter;
import com.classparser.util.ConfigurationUtils;
import com.classparser.util.Reflection;
import com.sun.tools.javap.*;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.*;

import static com.classparser.bytecode.decompile.javap.configuration.JavapConfiguration.*;

/**
 * Adapter of Javap disassembler for {@link Decompiler} API
 * This disassembler is a standard tool and deliver with jdk
 * Disassembler version depend on current jdk
 * <p>
 * This disassembler can disassemble all types byte code of classes
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public final class JavapDisassembler implements Decompiler {

    private static final DiagnosticListener<JavaFileObject> STUB = (empty) -> {
    };

    private final ConfigurationUtils utils;

    /**
     * Default constructor for initialize {@link JavapDisassembler}
     */
    public JavapDisassembler() {
        this.utils = new ConfigurationUtils(new HashMap<>(), getDefaultConfiguration());
    }

    @Override
    public String decompile(byte[] byteCode) {
        return decompile(byteCode, Collections.emptyList());
    }

    @Override
    public String decompile(byte[] byteCode, Collection<byte[]> nestedClassesByteCodes) {
        Map<String, byte[]> byteCodeMap = new HashMap<>();
        String className = ClassNameConverter.getClassName(byteCode);
        byteCodeMap.put(className, byteCode);
        for (byte[] innerClassByteCode : nestedClassesByteCodes) {
            byteCodeMap.put(ClassNameConverter.getClassName(innerClassByteCode), innerClassByteCode);
        }

        List<String> classesList = new ArrayList<>();
        classesList.add(className);

        Context context = new Context();
        context.put(Messages.class, new JavapMessages());

        Options options = getOptions(context);

        if (!nestedClassesByteCodes.isEmpty()) {
            options.showInnerClasses = true;
        }

        JavapTask task = new ByteCodeJavapTask(byteCodeMap, context);
        StringPrintWriter stringPrintWriter = new StringPrintWriter();

        setField(task, "log", stringPrintWriter);
        setField(task, "classes", classesList);
        setField(task, "options", options);
        setField(task, "diagnosticListener", STUB);

        task.run();

        return stringPrintWriter.getDisassembledCode();
    }

    /**
     * Parses and creates options for javap disassembler
     *
     * @return disassembler options
     */
    private Options getOptions(Context context) {
        Options options = new JavapOptions(context);

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
     * Creates default javap configuration
     * Describe of option can Javap configuration
     *
     * @return default configuration map
     * @see JavapConfiguration
     */
    private Map<String, Object> getDefaultConfiguration() {
        return JavapBuilderConfiguration
                .getBuilderConfiguration()
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
     * @param fieldName  object field name
     * @param fieldValue value what should be set to field
     */
    private void setField(Object object, String fieldName, Object fieldValue) {
        Field field = Reflection.getField(JavapTask.class, fieldName);
        Reflection.set(field, object, fieldValue);
    }

    /**
     * Implementation of {@link SimpleJavaFileObject} which create class input stream
     * from byte code of class
     */
    private static class ByteArrayJavaFileObject extends SimpleJavaFileObject {

        private static final String EMPTY_URI = "file://class";

        private final byte[] byteCode;

        private final String className;

        /**
         * Default constructor for initialize of {@link ByteArrayJavaFileObject}
         *
         * @param byteCode byte code of class
         */
        public ByteArrayJavaFileObject(byte[] byteCode) {
            super(URI.create(EMPTY_URI), Kind.CLASS);
            this.byteCode = byteCode;
            this.className = ClassNameConverter.getClassName(byteCode);
        }

        @Override
        public String getName() {
            return className;
        }

        @Override
        public InputStream openInputStream() {
            return new ByteArrayInputStream(byteCode);
        }
    }

    /**
     * Implementation of {@link JavapTask} uses {@link ByteArrayJavaFileObject}
     * for loading class files to current process of task
     */
    private static class ByteCodeJavapTask extends JavapTask {

        private final Map<String, byte[]> byteCodeMap;

        /**
         * Default constructor for initialize of {@link ByteCodeJavapTask}
         *
         * @param byteCodeMap byte code map of classes
         * @param context     current disassemble context
         */
        public ByteCodeJavapTask(Map<String, byte[]> byteCodeMap, Context context) {
            this.byteCodeMap = byteCodeMap;
            this.context = context;
        }

        @Override
        protected JavaFileObject open(String className) {
            byte[] bytes = byteCodeMap.get(className);
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
        public StringPrintWriter() {
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
        public String getDisassembledCode() {
            return stringBuilder.toString();
        }
    }

    /**
     * {@link Options} stub for create public constructor
     */
    private static class JavapOptions extends Options {

        /**
         * Default constructor for initialize of {@link JavapOptions}
         *
         * @param context current context of disassemble process
         */
        protected JavapOptions(Context context) {
            super(context);
        }
    }

    /**
     * {@link Messages} stub for store in context
     */
    private static class JavapMessages implements Messages {

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