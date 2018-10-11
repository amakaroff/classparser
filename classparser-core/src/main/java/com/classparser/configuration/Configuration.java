package com.classparser.configuration;

import java.util.Map;

/**
 * Basic interface should implemented for classes uses any configuration
 *
 * @author Aleksei Makarov
 */
public interface Configuration {

    /**
     * Get configuration map with options
     * <p>
     * key - String
     * value - Object
     *
     * @return configuration map
     */
    Map<String, Object> getConfiguration();
}