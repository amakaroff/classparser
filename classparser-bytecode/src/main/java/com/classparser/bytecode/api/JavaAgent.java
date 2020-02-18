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
    String getAgentJarName();

    /**
     * Obtains agent jar file dir location
     *
     * @return agent jar location
     */
    String getAgentLocationPath();

    /**
     * Obtains agent class implementation basic agent methods
     *
     * @return agent class
     */
    Class<? extends JavaAgent> getAgentClass();

    /**
     * Obtains manifest file name which will be uses for agent jar
     *
     * @return path to manifest
     */
    Manifest getManifestFileName();

    /**
     * Obtain classes which will be store to agent jar
     *
     * @return array of classes
     */
    Class<?>[] getAgentJarClasses();
}