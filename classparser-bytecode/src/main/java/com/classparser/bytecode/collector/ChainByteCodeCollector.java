package com.classparser.bytecode.collector;

import com.classparser.bytecode.api.ByteCodeCollector;
import com.classparser.bytecode.configuration.ConfigurationManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Byte code collector uses chain of responsibility pattern
 * for obtaining byte code of classes called all collectors
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public class ChainByteCodeCollector implements ByteCodeCollector {

    private final ConfigurationManager configurationManager;

    /**
     * Constructor for init instance
     *
     * @param configurationManager configuration manager instance
     */
    public ChainByteCodeCollector(ConfigurationManager configurationManager) {
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
    public byte[] getByteCode(Class<?> clazz) {
        List<ByteCodeCollector> collectors = getCollectors();

        if (clazz != null) {
            for (ByteCodeCollector collector : collectors) {
                byte[] byteCode = collector.getByteCode(clazz);

                if (byteCode != null) {
                    return byteCode;
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
     * Collectors will be loaded use the {@link ServiceLoader}
     *
     * @return list contains a chain of collectors
     */
    private List<ByteCodeCollector> getCollectors() {
        List<ByteCodeCollector> collectors = new ArrayList<>();

        if (configurationManager != null) {
            ServiceLoader<ByteCodeCollector> load = ServiceLoader.load(ByteCodeCollector.class);
            for (ByteCodeCollector bytecodeCollector : load) {
                bytecodeCollector.setConfigurationManager(configurationManager);
                if (bytecodeCollector.isEnable()) {
                    collectors.add(bytecodeCollector);
                }
            }

            Collections.sort(collectors);
        }

        return collectors;
    }
}