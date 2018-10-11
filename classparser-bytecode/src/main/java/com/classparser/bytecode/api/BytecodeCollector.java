package com.classparser.bytecode.api;

import com.classparser.bytecode.configuration.ConfigurationManager;

/**
 * Interface provide methods for obtains bytecode of classes
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public interface BytecodeCollector extends Comparable<BytecodeCollector> {

    /**
     * Tryings find bytecode of class
     *
     * @param clazz - class for which is getting bytecode
     * @return bytecode of class or null if bytecode is not found
     */
    byte[] getBytecode(Class<?> clazz);

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
    boolean isEnable();

    /**
     * Setter for configuration manger
     * Will be set after initialization
     *
     * @param configurationManager configuration manager instance
     */
    void setConfigurationManager(ConfigurationManager configurationManager);

    @Override
    default int compareTo(BytecodeCollector collector) {
        return Integer.compare(this.getOrder(), collector.getOrder());
    }
}