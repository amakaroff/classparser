package com.classparser.reflection.parser.structure;

import com.classparser.reflection.ParseContext;
import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.parser.base.IndentParser;
import com.classparser.util.Reflection;

import java.io.ObjectStreamClass;
import java.lang.reflect.Method;

/**
 * Class provides functionality for parsing meta information about
 * initialization blocks in classes
 *
 * @author Aleksey Makarov
 * @since 1.0.0
 */
public class BlockParser {

    private final IndentParser indentParser;

    private final ConfigurationManager configurationManager;

    private final Method method;

    public BlockParser(IndentParser indentParser, ConfigurationManager configurationManager) {
        this.indentParser = indentParser;
        this.configurationManager = configurationManager;
        this.method = loadHasStaticInitializerHandle();
    }

    /**
     * Parses and obtains information about initialization blocks
     * Information may be incorrectly
     *
     * @param clazz any class
     * @return string line with initialization blocks
     */
    public String parseStaticBlock(Class<?> clazz, ParseContext context) {
        if (isStaticInitializerAllowed(clazz) && hasStaticInitializer(clazz)) {
            String oneIndent = configurationManager.getIndentSpaces();
            String lineSeparator = configurationManager.getLineSeparator();
            String indent = indentParser.getIndent(clazz, context) + oneIndent;
            return indent + "static {" + lineSeparator + indent + oneIndent +
                    "/* Compiled code */" + lineSeparator + indent + '}' + lineSeparator;
        }

        return "";
    }

    private boolean isStaticInitializerAllowed(Class<?> clazz) {
        return configurationManager.isDisplayStaticBlock() &&
                (!clazz.isEnum() || configurationManager.isParseEnumAsClass());
    }

    /**
     * Checks if static initializer block is exists
     *
     * @param clazz any class
     * @return true if static init block is exists
     */
    private boolean hasStaticInitializer(Class<?> clazz) {
        if (method != null) {
            try {
                return (boolean) Reflection.invoke(method, clazz);
            } catch (Exception exception) {
                return false;
            }
        }

        return false;
    }

    /**
     * Load private method by checking of static init block
     *
     * @return method #hasStaticInitializer() or null if method can't be a loaded
     */
    private Method loadHasStaticInitializerHandle() {
        try {
            return ObjectStreamClass.class.getDeclaredMethod("hasStaticInitializer", Class.class);
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }
}