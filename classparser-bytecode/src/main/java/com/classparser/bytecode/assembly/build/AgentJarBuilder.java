package com.classparser.bytecode.assembly.build;

import java.util.jar.Manifest;

/**
 * Interface for creating java agent jar files
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public interface AgentJarBuilder {

    /**
     * Setter for the name of jar file
     *
     * @param agentName full name of agent jar file
     * @return builder instance
     */
    AgentJarBuilder addAgentName(String agentName);

    /**
     * Setter for dir location of agent jar file
     *
     * @param dirLocation dir location
     * @return builder instance
     */
    AgentJarBuilder addAgentDirLocation(String dirLocation);

    /**
     * Appends other classes to jar
     *
     * @param attachedClasses classes will be pack to agent jar
     * @return builder instance
     */
    AgentJarBuilder addClasses(Class<?>... attachedClasses);

    /**
     * Appends manifest to jar file
     *
     * @param manifest manifest instance
     * @return builder instance
     */
    AgentJarBuilder addManifest(Manifest manifest);

    /**
     * Adds java agent class
     * <p>
     * Agent class need contains method
     * with signature
     * static void agentmain(String args, Instrumentation instrumentation)
     * or
     * static void premain(String args, Instrumentation instrumentation)
     * </p>
     *
     * @param agentClass agent class
     * @return builder instance
     */
    AgentJarBuilder setAgentClass(Class<?> agentClass);

    /**
     * Process of building java agent jar
     *
     * @return path to agent jar file
     */
    String build();
}