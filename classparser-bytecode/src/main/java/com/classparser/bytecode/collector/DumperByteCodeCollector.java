package com.classparser.bytecode.collector;

import com.classparser.bytecode.api.ByteCodeCollector;
import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.bytecode.exception.ByteCodeParserException;
import com.classparser.bytecode.utils.ClassNameConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Dumper collector collects byte code of classes uses jdk dumper mechanism
 * Dumper supports lambda expression and method handle for HotSpot/J9
 * <p>
 * <code>-Djdk.internal.lambda.dumpProxyClasses=DUMP_CLASS_FILES</code>
 * <code>-Djava.lang.invoke.MethodHandle.DUMP_CLASS_FILES=true</code>
 * <p>
 * These options should be enabled for working of this collector
 */
public class DumperByteCodeCollector implements ByteCodeCollector {

    private static final String DUMP_MH_PROPERTY = "java.lang.invoke.MethodHandle.DUMP_CLASS_FILES";

    private static final String DUMP_LAMBDA_PROPERTY = "jdk.internal.lambda.dumpProxyClasses";

    private static final String DUMP_PATH;

    private ConfigurationManager configurationManager;

    static {
        DUMP_PATH = System.getProperty("user.dir") + File.separatorChar + "DUMP_CLASS_FILES";
        if (isDumpPropertiesEnabled()) {
            Runtime.getRuntime().addShutdownHook(new Thread(DumperByteCodeCollector::clearDumpDirectory));
        }
    }

    @Override
    public int getOrder() {
        return 500;
    }

    @Override
    public boolean isEnabled() {
        return configurationManager.isEnableDumperByteCodeCollector() && isDumpPropertiesEnabled();
    }

    private static boolean isDumpPropertiesEnabled() {
        return Boolean.getBoolean(DUMP_MH_PROPERTY) || System.getProperty(DUMP_LAMBDA_PROPERTY) != null;
    }

    @Override
    public void setConfigurationManager(ConfigurationManager configurationManager) {
        if (configurationManager != null) {
            this.configurationManager = configurationManager;
        }
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
                     * Perform delete a file by the path ignore access exceptions
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
    public byte[] getByteCode(Class<?> clazz) {
        if (clazz != null) {
            String filePath = ClassNameConverter.toFileJavaClassName(clazz);
            Path path = Paths.get(DUMP_PATH, filePath);
            if (Files.exists(path)) {
                try {
                    return Files.readAllBytes(path);
                } catch (IOException exception) {
                    System.out.println("Can't read file: " + path.toString());
                    exception.printStackTrace();

                    return null;
                }
            }
        }

        return null;
    }
}