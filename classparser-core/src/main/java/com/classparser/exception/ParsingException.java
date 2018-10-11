package com.classparser.exception;

/**
 * Base exception was throws when parsing process was interrupted with any errors
 *
 * @author Aleksei Makarov
 */
public class ParsingException extends RuntimeException {

    /**
     * Constructor with parameter store message of exception
     *
     * @param message exception message
     */
    public ParsingException(String message) {
        super(message);
    }

    /**
     * Constructor with parameters cause and message of exception
     *
     * @param message reason message of exception
     * @param cause   cause of this exception
     */
    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
