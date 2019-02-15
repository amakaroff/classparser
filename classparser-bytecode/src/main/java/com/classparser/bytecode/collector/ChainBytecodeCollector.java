package com.classparser.bytecode.collector;

import com.classparser.bytecode.api.BytecodeCollector;
import com.classparser.bytecode.configuration.ConfigurationManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Bytecode collector uses chain of responsibility pattern
 * for obtaining bytecode of classes called all collectors
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public class ChainBytecodeCollector implements BytecodeCollector {

    private final ConfigurationManager configurationManager;

    /**
     * Constructor for init instance
     *
     * @param configurationManager configuration manager instance
     */
    public ChainBytecodeCollector(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    @Override
    public int getOrder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEnable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setConfigurationManager(ConfigurationManager configurationManager) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getBytecode(Class<?> clazz) {
        List<BytecodeCollector> collectors = getCollectors();

        if (clazz != null) {
            for (BytecodeCollector collector : collectors) {
                byte[] bytecode = collector.getBytecode(clazz);

                if (bytecode != null) {
                    return bytecode;
                }
            }
        }

        return null;
    }

    /**
     * Eager loads collectors to chain of calls
     * 1. Class file collector
     * 2. Dumper collector
     * 3. Instrumentation collector
     * <p>
     * Collectors will be loaded uses {@link ServiceLoader}
     *
     * @return list contains chain of collectors
     */
    private List<BytecodeCollector> getCollectors() {
        List<BytecodeCollector> collectors = new ArrayList<>(3);

        if (configurationManager != null) {
            ServiceLoader<BytecodeCollector> load = ServiceLoader.load(BytecodeCollector.class);
            for (BytecodeCollector bytecodeCollector : load) {
                bytecodeCollector.setConfigurationManager(configurationManager);
                if (bytecodeCollector.isEnable()) {
                    collectors.add(bytecodeCollector);
                }
            }

            Collections.sort(collectors);
        } else {
            throw new NullPointerException("Configuration Manager is not initialized for collector!");
        }

        return collectors;
    }
}