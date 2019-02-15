package com.classparser.bytecode.assembly.attach;

import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.bytecode.exception.ByteCodeParserException;
import com.sun.tools.attach.VirtualMachine;

import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class provides functionality by dynamically attach java agent to JVM
 *
 * @author Aleksei Makarov
 * @author Vadim Kiselev
 * @since 1.0.0
 */
public class AgentAttacher {

    private static final String VIRTUAL_MACHINE_CLASS_NAME = "com.sun.tools.attach.VirtualMachine";

    private static final char JVM_NAME_ID_SEPARATOR = '@';

    private static final String JAVA_HOME = System.getProperty("java.home");

    private static final String JAVA_TOOLS_PATH = "/../lib/tools.jar";

    private static final String JDK_TOOLS_PATH = "/lib/tools.jar";

    private static final String MAC_OS_TOOLS_PATH = "/../Classes/classes.jar";

    private static volatile ClassLoader toolsJarClassLoader;

    private final ConfigurationManager configurationManager;

    public AgentAttacher(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    /**
     * Performs dynamically java agent attach to current JVM without parameters
     *
     * @param agentPath path to agent jar
     */
    public void attach(String agentPath) {
        attach(agentPath, "");
    }

    /**
     * Performs dynamically java agent attach to current JVM with some parameters
     *
     * @param agentPath  path to agent jar
     * @param parameters agent attach parameters
     */
    public void attach(String agentPath, String parameters) {
        Path path = Paths.get(agentPath);
        if (Files.exists(path)) {
            if (isExistsInClassPathToolJar()) {
                attachUsesToolJar(agentPath, parameters);
            } else {
                findToolsJarAndAttach(agentPath, parameters);
            }
        } else {
            throw new ByteCodeParserException("Could't find agent jar by follow path: " + agentPath);
        }
    }

    /**
     * Checks if tools.jar exists in java classpath
     *
     * @return true if tools.jar exists in classpath
     */
    private boolean isExistsInClassPathToolJar() {
        try {
            Class.forName(VIRTUAL_MACHINE_CLASS_NAME);
        } catch (ClassNotFoundException ignore) {
            return false;
        }

        return true;
    }

    /**
     * Performs dynamically attach java agent uses tools.jar
     *
     * @param agentPath  path to agent jar
     * @param parameters agent attach parameters
     */
    private void attachUsesToolJar(String agentPath, String parameters) {
        String processID = getCurrentJVMProcessID();
        try {
            VirtualMachine virtualMachine = VirtualMachine.attach(processID);
            try {
                virtualMachine.loadAgent(agentPath, parameters);
            } finally {
                virtualMachine.detach();
            }
        } catch (Exception exception) {
            throw new ByteCodeParserException("Can't attach java agent to JVM process!", exception);
        }
    }

    /**
     * Obtains current JVM process ID from MXBean
     *
     * @return current JVM process ID
     */
    private String getCurrentJVMProcessID() {
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int processID = nameOfRunningVM.indexOf(JVM_NAME_ID_SEPARATOR);
        return nameOfRunningVM.substring(0, processID);
    }

    /**
     * Tryings find tools.jar in file system if it not define in java classpath
     * and perform attach
     *
     * @param agentPath  path to agent jar
     * @param parameters agent attach parameters
     */
    private void findToolsJarAndAttach(String agentPath, String parameters) {
        Path toolsPath = getToolsPath();
        if (toolsPath != null) {
            try {
                ClassLoader toolsJarClassLoader = getToolsJarClassLoader(toolsPath);
                ClassLoader classLoader = getClass().getClassLoader();

                Thread thread = Thread.currentThread();
                try {
                    thread.setContextClassLoader(toolsJarClassLoader);
                    attachUsesToolJar(agentPath, parameters);
                } finally {
                    thread.setContextClassLoader(classLoader);
                }
            } catch (MalformedURLException exception) {
                throw new ByteCodeParserException("Can't resolve url path to tools jar!", exception);
            }
        } else {
            throw new ByteCodeParserException("Can't find tools.jar for attach java agent!");
        }
    }

    /**
     * Tryings obtains tools.jar path in system
     *
     * @return tools.jar path or null if jar is not found
     */
    private Path getToolsPath() {
        Path defaultPath = Paths.get(JAVA_HOME + JAVA_TOOLS_PATH);
        if (Files.exists(defaultPath)) {
            return defaultPath;
        }

        Path jdkPath = Paths.get(JAVA_HOME + JDK_TOOLS_PATH);
        if (Files.exists(defaultPath)) {
            return jdkPath;
        }

        Path macPath = Paths.get(JAVA_HOME + MAC_OS_TOOLS_PATH);
        if (Files.exists(defaultPath)) {
            return macPath;
        }

        String toolsJarPath = configurationManager.getToolsJarPath();
        if (!toolsJarPath.isEmpty()) {
            Path customPath = Paths.get(toolsJarPath);
            if (Files.exists(customPath)) {
                return customPath;
            }
        }

        return null;
    }

    /**
     * Obtains and initializes class loader which will load dynamic attach classes
     *
     * @param toolsPath path to tools.jar
     * @return tools.jar class loader instance
     * @throws MalformedURLException if path to tools.jar is invalid
     */
    private ClassLoader getToolsJarClassLoader(Path toolsPath) throws MalformedURLException {
        if (toolsJarClassLoader == null) {
            synchronized (AgentAttacher.class) {
                if (toolsJarClassLoader == null) {
                    ClassLoader classLoader = getClass().getClassLoader();
                    URL[] urls = {toolsPath.toUri().toURL()};

                    toolsJarClassLoader = new URLClassLoader(urls, classLoader);
                }
            }
        }

        return toolsJarClassLoader;
    }
}