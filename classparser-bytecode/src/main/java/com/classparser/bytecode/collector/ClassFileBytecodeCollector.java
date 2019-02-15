package com.classparser.bytecode.collector;

import com.classparser.bytecode.api.BytecodeCollector;
import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.bytecode.exception.ByteCodeParserException;
import com.classparser.bytecode.utils.ClassNameConverter;
import com.classparser.exception.file.FileReadingException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Collector uses java class path try obtain bytecode from .class file
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public class ClassFileBytecodeCollector implements BytecodeCollector {

    private static final int BYTE_BUFFER_SIZE = 1024 * 8;

    private volatile ConfigurationManager configurationManager;

    @Override
    public byte[] getBytecode(Class<?> clazz) {
        if (clazz != null) {
            ClassLoader loader = getClassLoader(clazz);

            if (loader != null) {
                String className = ClassNameConverter.toJarJavaClassName(clazz);
                try (InputStream stream = loader.getResourceAsStream(className)) {
                    if (stream != null) {
                        return readBytesFromInputStream(stream);
                    }
                } catch (IOException exception) {
                    throw new FileReadingException("Can't read bytecode for class : " + className, exception, className);
                }
            }
        } else {
            throw new NullPointerException("Class can't be a null!");
        }

        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean isEnable() {
        ConfigurationManager configurationManager = this.configurationManager;
        return configurationManager != null && configurationManager.isEnableClassFileBytecodeCollector();
    }

    @Override
    public void setConfigurationManager(ConfigurationManager configurationManager) {
        if (configurationManager != null) {
            this.configurationManager = configurationManager;
        } else {
            throw new NullPointerException("Configuration Manager is can't be null!");
        }
    }


    /**
     * Reads bytes from {@link InputStream}
     *
     * @param stream any input stream
     * @return byte array was read from input stream
     */
    private byte[] readBytesFromInputStream(InputStream stream) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(BYTE_BUFFER_SIZE);
        int batchSize;
        byte[] data = new byte[BYTE_BUFFER_SIZE];
        try {
            batchSize = stream.read(data, 0, data.length);
            while (batchSize != -1) {
                buffer.write(data, 0, batchSize);
                batchSize = stream.read(data, 0, data.length);
            }

            buffer.flush();
        } catch (IOException exception) {
            throw new ByteCodeParserException("Occurred problems at class loading!", exception);
        }

        return buffer.toByteArray();
    }

    /**
     * Obtains class loader which was load this class
     *
     * @param clazz any class
     * @return class loader which was load class
     */
    private ClassLoader getClassLoader(Class<?> clazz) {
        ClassLoader loader = ClassLoader.getSystemClassLoader();

        if (clazz != null) {
            loader = clazz.getClassLoader();

            if (loader == null) {
                loader = ClassLoader.getSystemClassLoader();
                while (loader != null && loader.getParent() != null) {
                    loader = loader.getParent();
                }
            }
        }

        return loader;
    }
}