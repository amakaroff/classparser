package com.classparser.bytecode.collector;

import com.classparser.bytecode.api.ByteCodeCollector;
import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.bytecode.exception.ByteCodeParserException;
import com.classparser.bytecode.utils.ClassNameConverter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Collector uses java class path try obtain byte code from .class file
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public class ClassFileByteCodeCollector implements ByteCodeCollector {

    private static final int BYTE_BUFFER_SIZE = 1024;

    private ConfigurationManager configurationManager;

    @Override
    public byte[] getByteCode(Class<?> clazz) {
        if (isClassFileExists(clazz)) {
            try (InputStream inputStream = openInputStreamToFile(clazz)) {
                return readBytesFromInputStream(inputStream);
            } catch (IOException exception) {
                throw new ByteCodeParserException("Can't open stream to file with class " + clazz, exception);
            }
        } else {
            return null;
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean isEnabled() {
        return configurationManager.isEnableClassFileByteCodeCollector();
    }

    @Override
    public void setConfigurationManager(ConfigurationManager configurationManager) {
        if (configurationManager != null) {
            this.configurationManager = configurationManager;
        }
    }

    /**
     * Open input stream to resource with byte code of selected class
     *
     * @param clazz any class instance
     * @return stream with byte code of class
     */
    private InputStream openInputStreamToFile(Class<?> clazz) {
        ClassLoader classLoader = getClassLoader(clazz);
        return classLoader.getResourceAsStream(ClassNameConverter.toJarJavaClassName(clazz));
    }

    /**
     * Reads bytes from {@link InputStream}
     *
     * @param stream any input stream
     * @return byte array was read from input stream
     */
    private byte[] readBytesFromInputStream(InputStream stream) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
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
     * Checks is java class file exists in classpath
     *
     * @param clazz any class
     * @return path to class or empty string if path is not found
     */
    private boolean isClassFileExists(Class<?> clazz) {
        ClassLoader loader = getClassLoader(clazz);

        if (loader != null && clazz != null) {
            URL resource = loader.getResource(ClassNameConverter.toJarJavaClassName(clazz));
            return resource != null;
        }

        return false;
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