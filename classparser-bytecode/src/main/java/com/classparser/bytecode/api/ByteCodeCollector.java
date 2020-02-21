package com.classparser.bytecode.api;

import com.classparser.bytecode.configuration.ConfigurationManager;

/**
 * Interface provide methods for obtains byte code of classes
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public interface ByteCodeCollector extends Comparable<ByteCodeCollector> {

    /**
     * Tryings find byte code of class
     *
     * @param clazz - class for which is getting byte code
     * @return byte code of class or null if byte code is not found
     */
    byte[] getByteCode(Class<?> clazz);

    /**
     * Order value by which will call collectors chain
     * From lower to upper
     *
     * @return order value
     */
    int getOrder();

    /**
     * Checks if collector is enabled
     *
     * @return true if collector is enabled
     */
    boolean isEnabled();

    /**
     * Setter for configuration manger
     * Will be set after initialization
     *
     * @param configurationManager configuration manager instance
     */
    void setConfigurationManager(ConfigurationManager configurationManager);

    @Override
    default int compareTo(ByteCodeCollector collector) {
        return Integer.compare(this.getOrder(), collector.getOrder());
    }
}