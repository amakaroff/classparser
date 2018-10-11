package com.classparser.bytecode.exception.decompile;

import com.classparser.bytecode.exception.ByteCodeParserException;

/**
 * Unchecked exception can be throws when decompilation process
 * has been interrupted with any error
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public class DecompilationException extends ByteCodeParserException {

    /**
     * Constructor with parameter store error message in exception
     *
     * @param message error message
     */
    public DecompilationException(String message) {
        super(message);
    }

    /**
     * Constructor with parameter store error message in exception and
     * cause of root exception
     *
     * @param message error message
     * @param cause   cause of exception
     */
    public DecompilationException(String message, Throwable cause) {
        super(message, cause);
    }
}