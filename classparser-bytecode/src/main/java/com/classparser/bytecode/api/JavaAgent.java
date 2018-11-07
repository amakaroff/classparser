package com.classparser.bytecode.api;

import java.lang.instrument.Instrumentation;
import java.util.jar.Manifest;

/**
 * Interface provide basic method of java agent implementation
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public interface JavaAgent {

    String DEFAULT_AGENT_JAR_NAME = "agent.jar";

    /**
     * Gets instrumentation from premain or agentmain method
     * Execute initialize if java agent is not initialized
     *
     * @return {@link Instrumentation} instance
     */
    Instrumentation getInstrumentation();

    /**
     * Checks if agent is initialize
     *
     * @return boolean status
     */
    boolean isInitialized();

    /**
     * Obtains agent jar file name
     *
     * @return agent jar name
     */
    default String getAgentJarName() {
        return DEFAULT_AGENT_JAR_NAME;
    }

    /**
     * Obtains agent jar file dir location
     *
     * @return agent jar location
     */
    default String getAgentLocationPath() {
        return System.getProperty("user.dir");
    }

    /**
     * Obtains agent class implementation basic agent methods
     *
     * @return agent class
     */
    default Class<? extends JavaAgent> getAgentClass() {
        return getClass();
    }

    /**
     * Obtains manifest file name which will be uses for agent jar
     *
     * @return path to manifest
     */
     default Manifest getManifestFileName() {
         return new Manifest();
     }

    /**
     * Obtains classes which will be store to agent jar
     *
     * @return array of classes
     */
    default Class<?>[] getAgentJarClasses() {
        return new Class[]{getAgentClass()};
    }
}