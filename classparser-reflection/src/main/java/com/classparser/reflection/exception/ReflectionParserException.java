package com.classparser.reflection.exception;

import com.classparser.exception.ParsingException;

/**
 * This exception can be throw, if some problems occurred at
 * reflection parsing class mechanism
 *
 * @author Aleksey Makarov
 * @since 1.0.0
 */
public class ReflectionParserException extends ParsingException {

    /**
     * Constructor with parameter store message of exception
     *
     * @param message reason message of exception
     */
    public ReflectionParserException(String message) {
        super(message);
    }

    /**
     * Constructor with parameters cause and message of exception
     *
     * @param message reason message of exception
     * @param cause   cause of this exception
     */
    public ReflectionParserException(String message, Throwable cause) {
        super(message, cause);
    }
}