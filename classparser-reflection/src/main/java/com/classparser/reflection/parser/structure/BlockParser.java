package com.classparser.reflection.parser.structure;

import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.configuration.ReflectionParserManager;
import com.classparser.reflection.parser.base.IndentParser;

import java.io.ObjectStreamClass;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * Class provides functionality for parsing meta information about
 * initialization blocks in classes
 *
 * @author Aleksey Makarov
 * @since 1.0.0
 */
public class BlockParser {

    private final MethodHandle methodHandle;

    private final IndentParser indentParser;

    private final ConfigurationManager configurationManager;

    public BlockParser(IndentParser indentParser, ReflectionParserManager manager) {
        this.indentParser = indentParser;
        this.configurationManager = manager.getConfigurationManager();
        this.methodHandle = loadHasStaticInitializerHandle();
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
        if (methodHandle != null) {
            try {
                return (boolean) methodHandle.invokeExact(clazz);
            } catch (Throwable exception) {
                if (exception instanceof Error) {
                    throw (Error) exception;
                }

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
    private MethodHandle loadHasStaticInitializerHandle() {
        try {
            Method method = ObjectStreamClass.class.getDeclaredMethod("hasStaticInitializer", Class.class);
            try {
                method.setAccessible(true);
                return MethodHandles.lookup().unreflect(method);
            } finally {
                method.setAccessible(false);
            }
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }
}