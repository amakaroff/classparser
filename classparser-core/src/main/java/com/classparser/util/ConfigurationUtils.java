package com.classparser.util;

import com.classparser.configuration.Configuration;
import com.classparser.exception.option.OptionNotFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Class provides methods for usable working with Map configuration
 *
 * @author Aleksei Makarov
 */
public class ConfigurationUtils {

    /**
     * Any custom configuration
     */
    private final Map<String, Object> defaultConfiguration;

    /**
     * Fully, correctly configuration
     */
    private volatile Map<String, Object> configuration;

    /**
     * Default constructor for initialize {@link ConfigurationUtils}
     *
     * @param defaultConfiguration map with all correctly options
     */
    public ConfigurationUtils(Map<String, Object> defaultConfiguration) {
        this(new HashMap<>(), defaultConfiguration);
    }

    /**
     * Default constructor for initialize {@link ConfigurationUtils}
     *
     * @param configuration        any custom configuration
     * @param defaultConfiguration map with all correctly options
     */
    public ConfigurationUtils(Map<String, Object> configuration, Map<String, Object> defaultConfiguration) {
        this.configuration = configuration;
        this.defaultConfiguration = defaultConfiguration;
    }

    /**
     * Check has objected is an instance of any class
     *
     * @param object any object
     * @param type   checking type
     * @param <T>    generic of type
     * @return true if object is instance of class
     */
    private <T> boolean isInstance(Object object, Class<T> type) {
        return type.isInstance(object);
    }

    /**
     * Reload the current configuration for utils
     *
     * @param newConfiguration new configuration map
     */
    public void reloadConfiguration(Configuration newConfiguration) {
        if (newConfiguration != null) {
            Map<String, Object> configurationMap = newConfiguration.getConfiguration();
            if (configurationMap != null && !configurationMap.isEmpty()) {
                this.configuration = configurationMap;
            }
        }
    }


    /**
     * Obtain and check any option from configuration
     * If option not exists, will use default option
     *
     * @param config name of option
     * @param type   checking type for option
     * @param <T>    type of option
     * @return option object
     * @throws OptionNotFoundException if option is not defined in basic and default configuration
     */
    public <T> T getConfigOption(String config, Class<T> type) throws OptionNotFoundException {
        Map<String, Object> configuration = this.configuration;
        Object option = configuration.get(config);
        if (configuration.containsKey(config)) {
            if (isInstance(option, type)) {
                return type.cast(option);
            } else {
                System.err.println("Option {" + config + "} have invalid default class type {" + type + "}!" +
                        "Will be get the default option.");
            }
        }

        option = defaultConfiguration.get(config);
        if (option == null) {
            throw new OptionNotFoundException("Default option: \"" + config + "\" it isn't put down", config);
        }

        return type.cast(option);
    }

    /**
     * Checks if option exists in custom and default configurations
     *
     * @param config the name of option
     * @return true if option exists
     */
    public boolean hasOptionExists(String config) {
        return configuration.containsKey(config) || defaultConfiguration.containsKey(config);
    }
}