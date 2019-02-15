package com.classparser.bytecode.collector;

import com.classparser.bytecode.api.BytecodeCollector;
import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.bytecode.exception.ByteCodeParserException;
import com.classparser.bytecode.utils.ClassNameConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Dumper collector collects bytecode of classes uses jdk dumper mechanism
 * Dumper supports lambda expression and method handle for HotSpot/J9
 *
 * <code>-Djdk.internal.lambda.dumpProxyClasses=DUMP_CLASS_FILES</code>
 * <code>-Djava.lang.invoke.MethodHandle.DUMP_CLASS_FILES=true</code>
 * <p>
 * This options should be enabled for working of this collector
 */
public class DumperBytecodeCollector implements BytecodeCollector {

    private static final String DUMP_MH_PROPERTY = "java.lang.invoke.MethodHandle.DUMP_CLASS_FILES";

    private static final String DUMP_LAMBDA_PROPERTY = "jdk.internal.lambda.dumpProxyClasses";

    private static final String DUMP_PATH;

    static {
        DUMP_PATH = System.getProperty("user.dir") + File.separatorChar + "DUMP_CLASS_FILES";
        if (isDumpPropertiesEnabled()) {
            Runtime.getRuntime().addShutdownHook(new Thread(DumperBytecodeCollector::clearDumpDirectory));
        }
    }

    private volatile ConfigurationManager configurationManager;

    /**
     * Checks if one property for dumping is enabled
     *
     * @return true if dump property is enabled
     */
    private static boolean isDumpPropertiesEnabled() {
        return Boolean.getBoolean(DUMP_MH_PROPERTY) || System.getProperty(DUMP_LAMBDA_PROPERTY) != null;
    }

    /**
     * Full clearing dump directory and removing all files and directories
     * Calls after program works
     */
    private static void clearDumpDirectory() {
        Path path = Paths.get(DUMP_PATH);

        if (Files.exists(path)) {
            try {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        deleteIgnoreAccess(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        deleteIgnoreAccess(dir);
                        return FileVisitResult.CONTINUE;
                    }

                    /**
                     * Perform delete file by path ignore access exceptions
                     *
                     * @param path file system path
                     * @throws IOException if delete was interrupted with exception
                     */
                    private void deleteIgnoreAccess(Path path) throws IOException {
                        try {
                            Files.deleteIfExists(path);
                        } catch (AccessDeniedException ignore) {
                        }
                    }
                });
            } catch (IOException exception) {
                throw new ByteCodeParserException("Process clearing of dump directories was interrupted!", exception);
            }
        }
    }

    @Override
    public int getOrder() {
        return 500;
    }

    @Override
    public boolean isEnable() {
        ConfigurationManager configurationManager = this.configurationManager;
        return configurationManager != null && configurationManager.isEnableDumperByteCodeCollector();
    }

    @Override
    public void setConfigurationManager(ConfigurationManager configurationManager) {
        if (configurationManager != null) {
            this.configurationManager = configurationManager;
        } else {
            throw new NullPointerException("Configuration Manager is can't be null!");
        }
    }

    @Override
    public byte[] getBytecode(Class<?> clazz) {
        if (clazz != null) {
            checkingDumpingPropertyForEnabling();

            String filePath = ClassNameConverter.toFileJavaClassName(clazz);
            String fullClassDumpPath = DUMP_PATH + File.separator + filePath;
            Path path = Paths.get(fullClassDumpPath);
            if (Files.exists(path)) {
                try {
                    return Files.readAllBytes(path);
                } catch (IOException exception) {
                    System.err.println("Can't read file: " + fullClassDumpPath);
                    exception.printStackTrace();

                    return null;
                }
            }
        } else {
            throw new NullPointerException("Class can't be a null!");
        }

        return null;
    }

    private void checkingDumpingPropertyForEnabling() {
        boolean isMethodHandleDumpEnabled = Boolean.getBoolean(DUMP_MH_PROPERTY);
        if (!isMethodHandleDumpEnabled) {
            System.err.println("Please add property \"-D" + DUMP_MH_PROPERTY + "=true\"" +
                    " for obtaining dump of method handle classes.");
        }

        String lambdaDumpPathProperty = System.getProperty(DUMP_LAMBDA_PROPERTY);
        if (lambdaDumpPathProperty == null) {
            System.err.println("Please add property \"-D" + DUMP_LAMBDA_PROPERTY + "=DUMP_CLASS_FILES\"" +
                    " for obtaining dump of lambda classes.");
        }
    }
}