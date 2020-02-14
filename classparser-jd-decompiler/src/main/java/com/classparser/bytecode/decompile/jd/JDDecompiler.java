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
    public String decompile(byte[] byteCode, Collection<byte[]> classes) {
        if (byteCode != null && classes != null) {
            org.jd.core.v1.api.Decompiler decompiler = new ClassFileToJavaSourceDecompiler();
            
            Loader loader = new JDLoader(byteCode, classes);
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
        
        configuration.put("realignLineNumbers", false);
        configuration.put("typeMaker", new TypeMaker(loader));
        
        return configuration;
    }

    @Override
    public void setConfigurationManager(ConfigurationManager configurationManager) {
        this.utils.reloadConfiguration(configurationManager.getCustomDecompilerConfiguration());
    }

    /**
     * Creates decompiler settings
     *
     * @return decompiler settings
     */
    /*private CommonPreferences getCommonPreferences() {
        return new CommonPreferences(
                utils.getConfigOption(SHOW_DEFAULT_CONSTRUCTOR_KEY, Boolean.class),
                utils.getConfigOption(REALIGNMENT_LINE_NUMBER_KEY, Boolean.class),
                utils.getConfigOption(SHOW_PREFIX_THIS_KEY, Boolean.class),
                utils.getConfigOption(MERGE_EMPTY_LINES_KEY, Boolean.class),
                utils.getConfigOption(UNICODE_ESCAPE_KEY, Boolean.class),
                utils.getConfigOption(SHOW_LINE_NUMBERS_KEY, Boolean.class)
        );
    }*/

    /**
     * Creates default configuration for decompiler
     *
     * @return default configuration
     * @see JDBuilderConfiguration for default values
     */
    private Map<String, Object> getDefaultConfiguration() {
        return JDBuilderConfiguration
                .getBuilderConfiguration()
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
    
    private static class JDPrinter implements Printer {
        
        private final StringBuilder sourceBuilder = new StringBuilder();
        
        private int indent;
        
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
            indent++;
        }

        @Override
        public void unindent() {
            indent--;
        }

        @Override
        public void startLine(int lineNumber) {
            for (int i = 0; i < indent; i++) {
                sourceBuilder.append("    ");
            }
        }

        @Override
        public void endLine() {
            sourceBuilder.append("\r\n");
        }

        @Override
        public void extraLine(int count) {

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