package com.classparser.reflection.parser.structure;

import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.configuration.ReflectionParserManager;
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

    private final Method staticInitializerCheckerMethod;

    public BlockParser(IndentParser indentParser, ReflectionParserManager manager) {
        this.indentParser = indentParser;
        this.configurationManager = manager.getConfigurationManager();
        this.staticInitializerCheckerMethod = loadCheckerMethod();
    }

    /**
     * Parses and obtains information about initialization blocks
     * Information may be incorrectly
     *
     * @param clazz any class
     * @return string line with initialization blocks
     */
    public String parseStaticBlock(Class<?> clazz) {
        if (hasStaticInitializer(clazz)) {
            String oneIndent = configurationManager.getIndentSpaces();
            String lineSeparator = configurationManager.getLineSeparator();
            String indent = indentParser.getIndent(clazz) + oneIndent;
            return indent + "static {" + lineSeparator + indent + oneIndent +
                    "/* Compiled code */" + lineSeparator + indent + '}' + lineSeparator;
        }

        return "";
    }

    /**
     * Checks if static initializer block is exists
     *
     * @param clazz any class
     * @return true if static init block is exists
     */
    private boolean hasStaticInitializer(Class<?> clazz) {
        if (configurationManager.isEnabledStaticBlockDisplaying()) {
            if (staticInitializerCheckerMethod != null) {
                try {
                    if (staticInitializerCheckerMethod.isAccessible()) {
                        return (boolean) staticInitializerCheckerMethod.invoke(clazz);
                    } else {
                        staticInitializerCheckerMethod.setAccessible(true);
                        try {
                            return (boolean) staticInitializerCheckerMethod.invoke(clazz);
                        } finally {
                            staticInitializerCheckerMethod.setAccessible(false);
                        }
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