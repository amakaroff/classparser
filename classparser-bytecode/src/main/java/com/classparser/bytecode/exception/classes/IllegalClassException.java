package com.classparser.bytecode.exception.classes;

/**
 * Unchecked exception uses in cases where class does not fit
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public class IllegalClassException extends RuntimeException {

    /**
     * Type of class which is illegal for any operation
     */
    private final Class<?> clazz;

    /**
     * Constructor with parameter store error message in exception
     * and class which is illegal for any case
     *
     * @param message error message
     * @param clazz   illegal class
     */
    public IllegalClassException(String message, Class<?> clazz) {
        super(message);
        this.clazz = clazz;
    }

    /**
     * Constructor with parameter store error message in exception
     * Cause of root exception and class which is illegal for any case
     *
     * @param message error message
     * @param cause   cause of exception
     * @param clazz   illegal class
     */
    public IllegalClassException(String message, Throwable cause, Class<?> clazz) {
        super(message, cause);
        this.clazz = clazz;
    }

    /**
     * Obtains illegal class
     *
     * @return illegal class
     */
    public Class<?> getIllegalClass() {
        return clazz;
    }
}