package com.classparser.bytecode.assembly;

import com.classparser.bytecode.api.JavaAgent;
import com.classparser.bytecode.assembly.attach.AgentAttacher;
import com.classparser.bytecode.assembly.build.AgentBuilder;
import com.classparser.bytecode.configuration.ConfigurationManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class provide functionality by creating agent jar and dynamic attach this jar to current JVM
 *
 * @author Aleksei Makarov
 * @author Vadim Kiselev
 * @since 1.0.0
 */
public class AgentAssembler {

    private final AgentAttacher agentAttacher;

    private final ConfigurationManager configurationManager;

    public AgentAssembler(ConfigurationManager configurationManager) {
        this(new AgentAttacher(configurationManager), configurationManager);
    }

    public AgentAssembler(AgentAttacher agentAttacher, ConfigurationManager configurationManager) {
        this.agentAttacher = agentAttacher;
        this.configurationManager = configurationManager;
    }

    /**
     * Start process by assembly agent to JVM
     * Method perform create agent jar by methods:
     * {@link JavaAgent#getAgentClass()}
     * {@link JavaAgent#getAgentJarClasses()}
     * {@link JavaAgent#getAgentJarName()}
     * {@link JavaAgent#getManifestFileName()}
     * and dynamically attach this jar to JVM
     * <p>
     * If agent already init, then method will be skipped
     *
     * @param agent java agent instance
     */
    public void assembly(JavaAgent agent) {
        if (!agent.isInitialized()) {
            if (configurationManager.isCacheAgentJar()) {
                cachedAssembly(agent);
            } else {
                nonCachedAssembly(agent);
            }
        }
    }

    /**
     * Performs assembly java agent uses cache agent jar
     *
     * @param agent java agent instance
     */
    protected void cachedAssembly(JavaAgent agent) {
        String agentPath = agent.getAgentLocationPath() + agent.getAgentJarName();
        Path agentJarPath = Paths.get(agentPath);

        if (Files.notExists(agentJarPath)) {
            agentPath = createAgent(agent);
        }

        agentAttacher.attach(agentPath);
    }

    /**
     * Performs assembly java agent and removes jar after attach processes
     *
     * @param agent java agent instance
     */
    protected void nonCachedAssembly(JavaAgent agent) {
        String agentPath = agent.getAgentLocationPath() + agent.getAgentJarName();
        Path agentJarPath = Paths.get(agentPath);

        if (Files.exists(agentJarPath)) {
            removeAgentJar(agentJarPath);
        }

        agentAttacher.attach(createAgent(agent));
        removeAgentJar(agentJarPath);
    }

    /**
     * Remove agent jar by path
     *
     * @param agentJarPath path to agent jar
     */
    private void removeAgentJar(Path agentJarPath) {
        try {
            Files.deleteIfExists(agentJarPath);
        } catch (IOException exception) {
            System.err.println("Problems occurred with removing agent jar file: " + agentJarPath.toString());
            exception.printStackTrace();
        }
    }

    /**
     * Process of building agent jar file
     *
     * @return absolute path to path to agent jar
     */
    private String createAgent(JavaAgent agent) {
        return AgentBuilder.createBuilder()
                .addAgentName(agent.getAgentJarName())
                .addAgentDirLocation(agent.getAgentLocationPath())
                .addAgentClass(agent.getAgentClass())
                .addManifest(agent.getManifestFileName())
                .addClasses(agent.getAgentJarClasses())
                .build();
    }
}