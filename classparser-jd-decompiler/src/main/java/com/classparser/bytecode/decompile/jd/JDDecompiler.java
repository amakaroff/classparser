package com.classparser.bytecode.decompile.jd;

import com.classparser.bytecode.api.Decompiler;
import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.bytecode.decompile.jd.configuration.JDBuilderConfiguration;
import com.classparser.bytecode.exception.DecompilationException;
import com.classparser.bytecode.utils.ClassNameConverter;
import com.classparser.util.ConfigurationUtils;
import jd.common.preferences.CommonPreferences;
import jd.common.printer.text.PlainTextPrinter;
import jd.core.loader.Loader;
import jd.core.loader.LoaderException;
import jd.core.model.classfile.ClassFile;
import jd.core.model.layout.block.LayoutBlock;
import jd.core.model.reference.ReferenceMap;
import jd.core.printer.InstructionPrinter;
import jd.core.printer.Printer;
import jd.core.process.analyzer.classfile.ClassFileAnalyzer;
import jd.core.process.analyzer.classfile.ReferenceAnalyzer;
import jd.core.process.deserializer.ClassFileDeserializer;
import jd.core.process.deserializer.ClassFormatException;
import jd.core.process.layouter.ClassFileLayouter;
import jd.core.process.writer.ClassFileWriter;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.classparser.bytecode.decompile.jd.configuration.JDConfiguration.COUNT_INDENT_SPACES_KEY;
import static com.classparser.bytecode.decompile.jd.configuration.JDConfiguration.MERGE_EMPTY_LINES_KEY;
import static com.classparser.bytecode.decompile.jd.configuration.JDConfiguration.REALIGNMENT_LINE_NUMBER_KEY;
import static com.classparser.bytecode.decompile.jd.configuration.JDConfiguration.SHOW_DEFAULT_CONSTRUCTOR_KEY;
import static com.classparser.bytecode.decompile.jd.configuration.JDConfiguration.SHOW_LINE_NUMBERS_KEY;
import static com.classparser.bytecode.decompile.jd.configuration.JDConfiguration.SHOW_PREFIX_THIS_KEY;
import static com.classparser.bytecode.decompile.jd.configuration.JDConfiguration.UNICODE_ESCAPE_KEY;

/**
 * Adapter of JD decompiler for {@link Decompiler} API
 * This decompiler was written of Emmanuel Dupuy
 * Decompiler version: 0.7.1 (Sep 2, 2015)
 * <p>
 * This decompiler support java 7 syntax and can't decompiler local classes
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public final class JDDecompiler implements Decompiler {

    private final ConfigurationUtils utils;

    public JDDecompiler() {
        this.utils = new ConfigurationUtils(getDefaultConfiguration());
    }

    @Override
    public String decompile(byte[] bytecode, Collection<byte[]> classes) {
        if (bytecode != null && classes != null) {
            List<ClassFile> innerClasses = createListOfClassFiles(classes);

            Loader loader = new JDLoader(bytecode);
            ReferenceMap referenceMap = new ReferenceMap();
            String className = ClassNameConverter.getClassName(bytecode);

            try {
                ClassFile classFile = ClassFileDeserializer.Deserialize(loader, className);
                resolveInnerClasses(classFile, innerClasses);

                ClassFileAnalyzer.Analyze(referenceMap, classFile);
                ReferenceAnalyzer.Analyze(referenceMap, classFile);

                CommonPreferences preferences = getCommonPreferences();
                JDPrinter jdPrinter = new JDPrinter();
                Printer printer = new InstructionPrinter(new PlainTextPrinter(preferences, jdPrinter));
                ArrayList<LayoutBlock> list = new ArrayList<>();

                int maxLineNumber = ClassFileLayouter.Layout(preferences, referenceMap, classFile, list);
                int minorVersion = classFile.getMinorVersion();
                int majorVersion = classFile.getMajorVersion();

                ClassFileWriter.Write(loader, printer, referenceMap, maxLineNumber, majorVersion, minorVersion, list);

                return jdPrinter.getSource();
            } catch (ClassFormatException | NullPointerException | StringIndexOutOfBoundsException exception) {
                throw new DecompilationException("JD can't decompile class: " + className, exception);
            } catch (LoaderException exception) {
                throw new DecompilationException("Decompilation process is interrupted", exception);
            } catch (Throwable throwable) {
                throw new DecompilationException("Some shit happens with JD decompiler!", throwable);
            }
        } else {
            throw new DecompilationException("Bytecode of classes for decompilation can't be a null!");
        }

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
     * Transforms collection of bytecode to list of {@link ClassFile} instances
     *
     * @param classes collection of bytecode
     * @return list of {@link ClassFile}'s
     */
    private List<ClassFile> createListOfClassFiles(Collection<byte[]> classes) {
        List<ClassFile> innerClasses = new ArrayList<>();
        for (byte[] bytecode : classes) {
            try {
                String className = ClassNameConverter.getClassName(bytecode);
                try {
                    innerClasses.add(ClassFileDeserializer.Deserialize(new JDLoader(bytecode), className));
                } catch (ClassFormatException exception) {
                    System.err.println("Class " + className + " can't be decompiled!");
                }
            } catch (LoaderException exception) {
                throw new DecompilationException("Error loading inner classes", exception);
            }
        }

        return innerClasses;
    }

    /**
     * Resolves of ClassFiles list and correctly sets all inner and anonymous classes
     *
     * @param classFile    main class file
     * @param innerClasses list of inner class files
     */
    private void resolveInnerClasses(ClassFile classFile, List<ClassFile> innerClasses) {
        String className = ClassNameConverter.toJavaClassSimpleName(classFile.getThisClassName()) + '$';
        Iterator<ClassFile> iterator = innerClasses.iterator();
        ArrayList<ClassFile> currentInnerClasses = new ArrayList<>();
        while (iterator.hasNext()) {
            ClassFile innerClass = iterator.next();
            String innerClassName = ClassNameConverter.toJavaClassSimpleName(innerClass.getThisClassName());
            if (!innerClassName.replace(className, "").contains("$")) {
                innerClass.setOuterClass(classFile);
                currentInnerClasses.add(innerClass);
                iterator.remove();
            }
        }

        classFile.setInnerClassFiles(currentInnerClasses);

        if (!innerClasses.isEmpty()) {
            for (ClassFile currentInnerClass : currentInnerClasses) {
                resolveInnerClasses(currentInnerClass, innerClasses);
            }
        }
    }

    /**
     * Creates decompiler settings
     *
     * @return decompiler settings
     */
    private CommonPreferences getCommonPreferences() {
        return new CommonPreferences(
                utils.getConfigOption(SHOW_DEFAULT_CONSTRUCTOR_KEY, Boolean.class),
                utils.getConfigOption(REALIGNMENT_LINE_NUMBER_KEY, Boolean.class),
                utils.getConfigOption(SHOW_PREFIX_THIS_KEY, Boolean.class),
                utils.getConfigOption(MERGE_EMPTY_LINES_KEY, Boolean.class),
                utils.getConfigOption(UNICODE_ESCAPE_KEY, Boolean.class),
                utils.getConfigOption(SHOW_LINE_NUMBERS_KEY, Boolean.class)
        );
    }

    /**
     * Creates default configuration for decompiler
     *
     * @return default configuration
     * @see JDBuilderConfiguration for default values
     */
    private Map<String, Object> getDefaultConfiguration() {
        return JDBuilderConfiguration
                .createBuilder()
                .displayDefaultConstructor(true)
                .realignmentLineNumber(true)
                .displayPrefixThis(true)
                .mergeEmptyLines(true)
                .unicodeEscape(false)
                .displayLineNumbers(false)
                .setCountIndentSpaces(4)
                .getConfiguration();
    }

    /**
     * Class uses for loading bytecode into decompiler
     */
    private class JDLoader implements Loader {

        private final byte[] bytecode;

        /**
         * Default constructor for creating jd loader
         *
         * @param bytecode bytecode of class
         */
        JDLoader(byte[] bytecode) {
            this.bytecode = bytecode;
        }

        @Override
        public DataInputStream load(String className) {
            return new DataInputStream(new ByteArrayInputStream(bytecode));
        }

        @Override
        public boolean canLoad(String className) {
            return true;
        }
    }

    /**
     * Class provides functionality by parsing java decompiled code
     * and lead to java convention style
     */
    private class JDPrinter extends PrintStream {

        private static final String JD_INDENT = "  ";

        private final PrintStream stub;

        private final StringBuilder builder;

        private final String indent;

        /**
         * Default constructor for create {@link JDPrinter} instance
         */
        JDPrinter() {
            super(System.out);
            this.indent = utils.getConfigOption(COUNT_INDENT_SPACES_KEY, String.class);
            this.builder = new StringBuilder();
            this.stub = null;
        }

        @Override
        public PrintStream append(CharSequence csq) {
            if (StringUtils.contains(csq, '{')) {
                int index = StringUtils.getFirstLeftNonCharNumber(builder, ' ');
                if (builder.charAt(index) == '\n') {
                    builder.deleteCharAt(index);
                }
            } else if (csq.equals(JD_INDENT)) {
                builder.append(indent);
                return stub;
            } else if (csq.equals("throws") || csq.equals("implements") || csq.equals("extends")) {
                builder.deleteCharAt(StringUtils.getNumberLeftOfLineSeparator(builder));
                builder.delete(StringUtils.getFirstLeftNonCharNumber(builder, ' '), builder.length() - 1);
            }
            builder.append(csq);

            return stub;
        }

        /**
         * Obtains decompiled code from corrective stream
         *
         * @return decompiled code
         */
        String getSource() {
            return StringUtils.normalizeOpenBlockCharacter(builder);
        }
    }
}