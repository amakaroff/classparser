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
 * Class provides functionality by store founded byte code to class files
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
     * Writes byte code to file
     *
     * @param fileName java class file name
     * @param byteCode byte code of class
     */
    private void writeByteCodeToFile(String fileName, byte[] byteCode) {
        try (FileOutputStream stream = new FileOutputStream(fileName)) {
            stream.write(byteCode);
        } catch (IOException exception) {
            throw new FileCreatingException("\"Can't create a file by the path: " + fileName, exception, fileName);
        }
    }

    /**
     * Constructs class file name
     *
     * @param byteCode byte code of class
     * @return java class file path
     */
    protected String getClassFileName(byte[] byteCode) {
        String classFileName = configurationManager.getDirectoryForSaveByteCode()
                               + File.separatorChar
                               + ClassNameConverter.toFileJavaClassName(ClassNameConverter.toJavaClassName(byteCode));
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
            throw new FileCreatingException("Directory: \"" + path + "\" can't create", exception, fullPath);
        }
    }

    /**
     * Performs process by store byte code to file
     *
     * @param byteCode byte code of class
     */
    public void saveToFile(byte[] byteCode) {
        if (byteCode != null) {
            writeByteCodeToFile(getClassFileName(byteCode), byteCode);
        }
    }
}