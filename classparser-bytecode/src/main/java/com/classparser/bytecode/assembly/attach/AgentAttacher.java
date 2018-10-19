package com.classparser.bytecode.assembly.attach;

import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.bytecode.exception.ByteCodeParserException;
import com.classparser.util.Reflection;
import com.sun.tools.attach.VirtualMachine;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
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
            return true;
        } catch (ClassNotFoundException ignore) {
            return false;
        }
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
    public String getCurrentJVMProcessID() {
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
    private synchronized void findToolsJarAndAttach(String agentPath, String parameters) {
        Path toolsPath = getToolsPath();
        if (toolsPath != null) {
            try {
                Class<?> virtualMachineClass = getToolsJarClassLoader(toolsPath).loadClass(VIRTUAL_MACHINE_CLASS_NAME);
                attachUsesDynamicallyTools(virtualMachineClass, agentPath, parameters);
            } catch (MalformedURLException exception) {
                throw new ByteCodeParserException("Can't resolve url path to tools jar!", exception);
            } catch (ClassNotFoundException exception) {
                throw new ByteCodeParserException("Can't find class: " + VIRTUAL_MACHINE_CLASS_NAME + " in tools jar!",
                        exception);
            }
        } else {
            throw new ByteCodeParserException("Can't find tools.jar for attach java agent!");
        }
    }

    /**
     * Dynamics attach java agent to JVM uses founded tools.jar
     *
     * @param virtualMachineClass virtual machine class
     * @param agentPath           path to agent jar
     * @param parameters          agent attach parameters
     */
    private void attachUsesDynamicallyTools(Class<?> virtualMachineClass, String agentPath, String parameters) {
        Method attachMethod = Reflection.getMethod(virtualMachineClass, "attach", String.class);
        Method loadAgentMethod = Reflection.getMethod(virtualMachineClass, "loadAgent", String.class, String.class);
        Method detachMethod = Reflection.getMethod(virtualMachineClass, "detach");

        String currentJVMProcessID = getCurrentJVMProcessID();
        try {
            Object virtualMachineInstance = Reflection.invokeStatic(attachMethod, currentJVMProcessID);
            try {
                Reflection.invoke(loadAgentMethod, virtualMachineInstance, agentPath, parameters);
            } finally {
                Reflection.invoke(detachMethod, virtualMachineInstance);
            }
        } catch (Exception exception) {
            throw new ByteCodeParserException("Can't attach java agent to JVM process!", exception);
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
                    toolsJarClassLoader = new URLClassLoader(new URL[]{toolsPath.toUri().toURL()},
                            getClass().getClassLoader());
                }
            }
        }

        return toolsJarClassLoader;
    }
}