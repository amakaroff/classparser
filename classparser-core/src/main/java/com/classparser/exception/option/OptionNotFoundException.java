package com.classparser.exception.option;

/**
 * Unchecked exception throws if option in the configuration is not exists
 *
 * @author Aleksei Makarov
 */
public class OptionNotFoundException extends RuntimeException {

    /**
     * Name of option which is not found
     */
    private final String optionName;

    /**
     * Constructor with parameter store error message in exception
     * and name of option which is not found
     *
     * @param message    error message
     * @param optionName name of option
     */
    public OptionNotFoundException(String message, String optionName) {
        super(message);
        this.optionName = optionName;
    }

    /**
     * Obtain the name of option which is not found
     *
     * @return name of option
     */
    public String getNameOfOption() {
        return optionName;
    }
}