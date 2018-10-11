package com.classparser.bytecode.saver;

import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.bytecode.utils.ClassNameConverter;
import com.classparser.exception.file.FileCreatingException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class provides functionality by store founded bytecode to class files
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public class BytecodeFileSaver {

    private final ConfigurationManager configurationManager;

    public BytecodeFileSaver(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    /**
     * Writes bytecode to file
     *
     * @param fileName java class file name
     * @param bytecode bytecode of class
     */
    private void writeByteCodeToFile(String fileName, byte[] bytecode) {
        try (FileOutputStream stream = new FileOutputStream(fileName)) {
            stream.write(bytecode);
        } catch (IOException exception) {
            throw new FileCreatingException("\"Can't create file by path: " + fileName, exception, fileName);
        }
    }

    /**
     * Constructs class file name
     *
     * @param bytecode bytecode of class
     * @return java class file path
     */
    protected String getClassFileName(byte[] bytecode) {
        String classFileName = configurationManager.getDirectoryForSaveBytecode()
                + File.separatorChar
                + ClassNameConverter.toFileJavaClassName(ClassNameConverter.toJavaClassName(bytecode));
        createClassFileNameDirectory(classFileName);
        return classFileName;
    }

    /**
     * Creates directories by file path if it's absent
     *
     * @param classFileName java file class path
     */
    protected void createClassFileNameDirectory(String classFileName) {
        String path = classFileName
                .substring(0, classFileName.lastIndexOf("."))
                .substring(0, classFileName.lastIndexOf(File.separatorChar));

        Path directoryPath = Paths.get(path);
        try {
            Files.createDirectories(directoryPath).toFile();
        } catch (IOException exception) {
            String fullPath = directoryPath.toAbsolutePath().toString();
            throw new FileCreatingException("Directory: \"" + path + "\" can't created", exception, fullPath);
        }
    }

    /**
     * Performs process by store bytecode to file
     *
     * @param bytecode bytecode of class
     */
    public void saveToFile(byte[] bytecode) {
        if (bytecode != null) {
            writeByteCodeToFile(getClassFileName(bytecode), bytecode);
        }
    }
}