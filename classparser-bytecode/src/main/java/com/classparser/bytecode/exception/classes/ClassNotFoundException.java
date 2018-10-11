package com.classparser.bytecode.exception.classes;

/**
 * Unchecked exception for cases if class will not found
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public class ClassNotFoundException extends RuntimeException {

    /**
     * Name of class which is not found
     */
    private final String className;

    /**
     * Constructor with parameter store error message in exception
     * and name of class which is not found
     *
     * @param message   error message
     * @param className name of class
     */
    public ClassNotFoundException(String message, String className) {
        super(message);
        this.className = className;
    }

    /**
     * Constructor with parameter store error message in exception
     * Cause of root exception and name of class which is not found
     *
     * @param message error message
     * @param cause cause of exception
     * @param className name of class
     */
    public ClassNotFoundException(String message, Throwable cause, String className) {
        super(message, cause);
        this.className = className;
    }

    /**
     * Obtains name of class which was is not found
     *
     * @return name of class
     */
    public String getClassName() {
        return className;
    }
}