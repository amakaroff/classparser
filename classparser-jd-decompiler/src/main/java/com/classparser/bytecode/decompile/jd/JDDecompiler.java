package com.classparser.bytecode.decompile.jd;

import com.classparser.bytecode.api.Decompiler;
import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.bytecode.decompile.jd.configuration.JDBuilderConfiguration;
import com.classparser.bytecode.exception.decompile.DecompilationException;
import com.classparser.bytecode.utils.ClassNameConverter;
import com.classparser.util.ConfigurationUtils;
import org.jd.core.v1.ClassFileToJavaSourceDecompiler;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.api.printer.Printer;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.classparser.bytecode.decompile.jd.configuration.JDConfiguration.COUNT_INDENT_SPACES_KEY;
import static com.classparser.bytecode.decompile.jd.configuration.JDConfiguration.MERGE_EMPTY_LINES_KEY;
import static com.classparser.bytecode.decompile.jd.configuration.JDConfiguration.REALIGNMENT_LINE_NUMBER_KEY;
import static com.classparser.bytecode.decompile.jd.configuration.JDConfiguration.SHOW_LINE_NUMBERS_KEY;
import static com.classparser.bytecode.decompile.jd.configuration.JDConfiguration.TYPE_MAKER_KEY;

/**
 * Adapter of JD decompiler for {@link Decompiler} API
 * This decompiler was written of Emmanuel Dupuy
 * Decompiler version: 1.3.1
 * <p>
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public final class JDDecompiler implements Decompiler {

    private final ConfigurationUtils utils;

    /**
     * Default constructor for creating {@link JDDecompiler} instance
     */
    public JDDecompiler() {
        this.utils = new ConfigurationUtils(new HashMap<>(), getDefaultConfiguration());
    }

    @Override
    public String decompile(byte[] byteCode) {
        return decompile(byteCode, Collections.emptyList());
    }

    @Override
    public String decompile(byte[] byteCode, Collection<byte[]> nestedClassesByteCodes) {
        if (byteCode != null && nestedClassesByteCodes != null) {
            org.jd.core.v1.api.Decompiler decompiler = new ClassFileToJavaSourceDecompiler();

            Loader loader = new JDLoader(byteCode, nestedClassesByteCodes);
            JDPrinter printer = new JDPrinter();
            String mainClassName = ClassNameConverter.getClassName(byteCode);
            try {
                decompiler.decompile(loader, printer, mainClassName, getConfiguration(loader));

                return printer.getSource();
            } catch (Exception exception) {
                throw new DecompilationException("Decompilcation process was interrupt with exception", exception);
            }
        }

        throw new DecompilationException("Byte code of classes for decompilation can't be a null!");
    }

    private Map<String, Object> getConfiguration(Loader loader) {
        Map<String, Object> configuration = new HashMap<>();

        configuration.put("realignLineNumbers", utils.getConfigOption(REALIGNMENT_LINE_NUMBER_KEY, Boolean.class));
        if (utils.hasOptionExists(TYPE_MAKER_KEY)) {
            configuration.put("typeMaker", utils.getConfigOption(TYPE_MAKER_KEY, TypeMaker.class));
        } else {
            configuration.put("typeMaker", new TypeMaker(loader));
        }

        return configuration;
    }

    @Override
    public void setConfigurationManager(ConfigurationManager configurationManager) {
        this.utils.reloadConfiguration(configurationManager.getCustomDecompilerConfiguration());
    }

    /**
     * Creates default configuration for decompiler
     *
     * @return default configuration
     * @see JDBuilderConfiguration for default values
     */
    private Map<String, Object> getDefaultConfiguration() {
        return JDBuilderConfiguration
                .getBuilderConfiguration()
                .realignmentLineNumber(true)
                .mergeEmptyLines(true)
                .displayLineNumbers(false)
                .setCountIndentSpaces(4)
                .getConfiguration();
    }

    /**
     * Class uses for loading byte code into decompiler
     */
    private static class JDLoader implements Loader {

        private Map<String, byte[]> byteCodesMap;

        /**
         * Default constructor for creating jd loader
         *
         * @param mainByteCode byte code of class
         */
        public JDLoader(byte[] mainByteCode, Collection<byte[]> nestedClassesByteCodes) {
            Map<String, byte[]> byteCodesMap = new HashMap<>();

            byteCodesMap.put(ClassNameConverter.getClassName(mainByteCode), mainByteCode);
            for (byte[] nestedClassesByteCode : nestedClassesByteCodes) {
                byteCodesMap.put(ClassNameConverter.getClassName(nestedClassesByteCode), nestedClassesByteCode);
            }

            this.byteCodesMap = byteCodesMap;
        }

        @Override
        public byte[] load(String className) {
            return byteCodesMap.get(className);
        }

        @Override
        public boolean canLoad(String className) {
            return true;
        }
    }

    private class JDPrinter implements Printer {

        private final String NEW_LINE = System.lineSeparator();

        private final StringBuilder sourceBuilder = new StringBuilder();

        private final boolean isMergeEmptyLines = utils.getConfigOption(MERGE_EMPTY_LINES_KEY, Boolean.class);

        private final boolean isDisplayLineNumbers = utils.getConfigOption(SHOW_LINE_NUMBERS_KEY, Boolean.class);

        private final String indentSpaces = utils.getConfigOption(COUNT_INDENT_SPACES_KEY, String.class);

        private int indentCounter;

        @Override
        public void start(int maxLineNumber, int majorVersion, int minorVersion) {
        }

        @Override
        public void end() {
        }

        @Override
        public void printText(String text) {
            sourceBuilder.append(text);
        }

        @Override
        public void printNumericConstant(String constant) {
            sourceBuilder.append(constant);
        }

        @Override
        public void printStringConstant(String constant, String ownerInternalName) {
            sourceBuilder.append(constant);
        }

        @Override
        public void printKeyword(String keyword) {
            sourceBuilder.append(keyword);
        }

        @Override
        public void printDeclaration(int type, String internalTypeName, String name, String descriptor) {
            sourceBuilder.append(name);
        }

        @Override
        public void printReference(int type, String internalTypeName, String name, String descriptor, String ownerInternalName) {
            sourceBuilder.append(name);
        }

        @Override
        public void indent() {
            indentCounter++;
        }

        @Override
        public void unindent() {
            indentCounter--;
        }

        @Override
        public void startLine(int lineNumber) {
            if (isDisplayLineNumbers) {
                sourceBuilder.append(lineNumber);
            }

            for (int i = 0; i < indentCounter; i++) {
                sourceBuilder.append(indentSpaces);
            }
        }

        @Override
        public void endLine() {
            sourceBuilder.append(NEW_LINE);
        }

        @Override
        public void extraLine(int count) {
            if (!isMergeEmptyLines) {
                while (count-- > 0) {
                    sourceBuilder.append(NEW_LINE);
                }
            }
        }

        @Override
        public void startMarker(int type) {
        }

        @Override
        public void endMarker(int type) {
        }

        public String getSource() {
            return sourceBuilder.toString();
        }
    }
}