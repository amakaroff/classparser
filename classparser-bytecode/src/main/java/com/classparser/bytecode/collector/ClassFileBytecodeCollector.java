package com.classparser.bytecode.collector;

import com.classparser.bytecode.api.BytecodeCollector;
import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.bytecode.exception.ByteCodeParserException;
import com.classparser.bytecode.utils.ClassNameConverter;
import com.classparser.exception.file.FileReadingException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Collector uses java class path try obtain bytecode from .class file
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public class ClassFileBytecodeCollector implements BytecodeCollector {

    private static final int BYTE_BUFFER_SIZE = 1024 * 8;

    private ConfigurationManager configurationManager;

    @Override
    public byte[] getBytecode(Class<?> clazz) {
        String filePath = getFilePath(clazz);

        if (!filePath.isEmpty()) {
            ClassLoader loader = getClassLoader(clazz);

            if (loader != null && clazz != null) {
                try (InputStream stream = loader.getResourceAsStream(ClassNameConverter.toJarJavaClassName(clazz))) {
                    return readBytesFromInputStream(stream);
                } catch (IOException exception) {
                    throw new FileReadingException("Can't read bytecode by path : " + filePath, exception, filePath);
                }
            }
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
        }
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
     * Obtains file path where storage java class
     *
     * @param clazz any class
     * @return path to class or empty string if path is not found
     */
    private String getFilePath(Class<?> clazz) {
        ClassLoader loader = getClassLoader(clazz);

        if (loader != null && clazz != null) {
            URL resource = loader.getResource(ClassNameConverter.toJarJavaClassName(clazz));
            if (resource != null) {
                return resource.getFile();
            }
        }

        return "";
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