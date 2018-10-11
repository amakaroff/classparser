package com.classparser.bytecode.collector;

import com.classparser.bytecode.api.BytecodeCollector;
import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.bytecode.exception.ByteCodeParserException;
import com.classparser.bytecode.utils.ClassNameConverter;
import com.classparser.exception.file.FileReadingException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Collector uses java class path try obtain bytecode from .class file
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public class ClassFileBytecodeCollector implements BytecodeCollector {

    private static final String SHIELDED_SPACE = "%20";

    private static final String FILE_PROTOCOL = "file://";

    private static final int BYTE_BUFFER_SIZE = 1024;

    private ConfigurationManager configurationManager;

    @Override
    public byte[] getBytecode(Class<?> clazz) {
        String filePath = getFilePath(clazz);

        if (!filePath.isEmpty()) {
            if (isContainsArchiveInPath(filePath)) {
                return getByteCodeFromJar(filePath);
            } else {
                return getByteCodeFromFile(filePath);
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
        return configurationManager.isEnableClassFileBytecodeCollector();
    }

    @Override
    public void setConfigurationManager(ConfigurationManager configurationManager) {
        if (configurationManager != null) {
            this.configurationManager = configurationManager;
        }
    }

    /**
     * Tryings obtain bytecode from .class file
     *
     * @param path path to class file
     * @return bytecode of class
     */
    private byte[] getByteCodeFromFile(String path) {
        try (FileInputStream stream = new FileInputStream(path)) {
            return readBytesFromInputStream(stream);
        } catch (IOException exception) {
            throw new FileReadingException("Can't read file by path: " + path, exception, path);
        }
    }

    /**
     * Tryings obtain bytecode from java archive
     *
     * @param path full path to java file in archive
     * @return bytecode of class
     */
    private byte[] getByteCodeFromJar(String path) {
        String archiveName = getArchivePath(path);
        String className = getClassNameFromArchivePath(path);

        try {
            JarFile file = new JarFile(archiveName);
            JarEntry jarEntry = file.getJarEntry(className);
            try (InputStream fileStream = file.getInputStream(jarEntry)) {
                return readBytesFromInputStream(fileStream);
            }
        } catch (Exception exception) {
            throw new FileReadingException("Can't read file by path: " + path, exception, path);
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
     * Obtains absolute path to archive from java class file in jar path
     *
     * @param jarFilePath class path inside archive
     * @return absolute archive path
     */
    private String getArchivePath(String jarFilePath) {
        if (isNotEmpty(jarFilePath) && isContainsArchiveInPath(jarFilePath)) {
            String path = jarFilePath.substring(0, getSeparatorPosition(jarFilePath));
            if ('/' != File.separatorChar) {
                path = path.replace('/', File.separatorChar);
            }

            path = FILE_PROTOCOL + path.replace(SHIELDED_SPACE, " ");
            try {
                URL urlPath = new URL(path);
                return urlPath.getFile();
            } catch (MalformedURLException exception) {
                throw new ByteCodeParserException("Jar path: \"" + path + "\" is undefined", exception);
            }
        }

        return "";
    }

    /**
     * Obtains class path in java archive
     *
     * @param jarFilePath java class path in archive
     * @return class path inside archive
     */
    private String getClassNameFromArchivePath(String jarFilePath) {
        if (isNotEmpty(jarFilePath) && isContainsArchiveInPath(jarFilePath)) {
            return jarFilePath.substring(getSeparatorPosition(jarFilePath) + 2);
        }

        return "";
    }

    /**
     * Finds separator position between archive path and class path inside archive
     *
     * @param jarFilePath java class path in archive
     * @return separator position
     */
    private int getSeparatorPosition(String jarFilePath) {
        String archiveType = getArchiveType(jarFilePath);
        return jarFilePath.lastIndexOf('.' + archiveType + '!') + archiveType.length() + 1;
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

    /**
     * Obtains archive type from java class path in archive
     *
     * @param path java class path in archive
     * @return archive type
     */
    private String getArchiveType(String path) {
        if (isNotEmpty(path)) {
            if (path.contains(".jar!")) {
                return "jar";
            }

            if (path.contains(".war!")) {
                return "war";
            }

            if (path.contains(".ear!")) {
                return "ear";
            }

            if (path.contains(".zip!")) {
                return "zip";
            }
        }

        return "";
    }

    /**
     * Checks if file path contains archive part
     *
     * @param path file path
     * @return true if file path contains archive part
     */
    private boolean isContainsArchiveInPath(String path) {
        return isNotEmpty(getArchiveType(path));
    }

    /**
     * Checks if string is not empty
     *
     * @param line string line
     * @return true if string is not empty
     */
    private boolean isNotEmpty(String line) {
        return line != null && !line.isEmpty();
    }
}