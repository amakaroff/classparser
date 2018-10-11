package com.classparser.bytecode.exception;

import com.classparser.exception.ParsingException;

/**
 * Base exception uses for any problems with parsing of byte code
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public class ByteCodeParserException extends ParsingException {

    /**
     * Constructor with parameter store message of exception
     *
     * @param message exception message
     */
    public ByteCodeParserException(String message) {
        super(message);
    }

    /**
     * Constructor with parameters cause and message of exception
     *
     * @param message reason message of exception
     * @param cause   cause of this exception
     */
    public ByteCodeParserException(String message, Throwable cause) {
        super(message, cause);
    }
}