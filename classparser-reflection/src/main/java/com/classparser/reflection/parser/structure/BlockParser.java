package com.classparser.reflection.parser.structure;

import com.classparser.reflection.ParseContext;
import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.parser.base.IndentParser;

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

    public BlockParser(ConfigurationManager configurationManager) {
        this.indentParser = new IndentParser(configurationManager);
        this.configurationManager = configurationManager;
    }

    /**
     * Parses and obtains information about initialization blocks
     * Information may be incorrectly
     *
     * @param clazz   any class
     * @param context context of parsing class process
     * @return string line with initialization blocks
     */
    public String parseStaticBlock(Class<?> clazz, ParseContext context) {
        if (isShouldBeDisplayed(clazz) && hasStaticInitializer(clazz)) {
            String oneIndent = configurationManager.getIndentSpaces();
            String lineSeparator = configurationManager.getLineSeparator();
            String indent = indentParser.getIndent(clazz, context) + oneIndent;
            return indent + "static {" + lineSeparator + indent + oneIndent +
                    "/* Compiled code */" + lineSeparator + indent + '}' + lineSeparator;
        }

        return "";
    }

    /**
     * Is static initializer should be displayed
     * @param clazz any class
     * @return true for display static initializer
     */
    private boolean isShouldBeDisplayed(Class<?> clazz) {
        return configurationManager.isDisplayStaticBlock() &&
                (configurationManager.isDisplayEnumAsClass() || !clazz.isEnum());
    }

    /**
     * Checks if static initializer block is exists
     *
     * @param clazz any class
     * @return true if static init block is exists
     */
    private boolean hasStaticInitializer(Class<?> clazz) {
        if (configurationManager.isDisplayStaticBlock()) {
            Method methodChecker = loadCheckerMethod();
            if (methodChecker != null) {
                try {
                    methodChecker.setAccessible(true);
                    try {
                        return (boolean) methodChecker.invoke(null, clazz);
                    } finally {
                        methodChecker.setAccessible(false);
                    }
                } catch (Exception exception) {
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * Load private method by checking of static init block
     *
     * @return method #hasStaticInitializer() or null if method can't be a loaded
     */
    private Method loadCheckerMethod() {
        try {
            return ObjectStreamClass.class.getDeclaredMethod("hasStaticInitializer", Class.class);
        } catch (NoSuchMethodException exception) {
            return null;
        }
    }
}