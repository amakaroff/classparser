package com.classparser.bytecode.agent;

import com.classparser.bytecode.api.JavaAgent;
import com.classparser.bytecode.assembly.AgentAssembler;
import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.bytecode.utils.ClassNameConverter;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Proxy;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Default implementation of {@link JavaAgent} uses dynamically attach to jvm
 * by default and allow obtains {@link Instrumentation} instance
 * <p>
 * Agent class is self initialize and have proxy access to instrumentation instance
 * allows create many agents, uses it for different transformers
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public final class DefaultJavaAgent implements JavaAgent {

    private static final String AGENT_JAR_NAME = "agent.jar";

    private static final String TEMP_DIR_KEY = "java.io.tmpdir";

    private static Instrumentation instrumentation;

    private final ThreadLocal<Boolean> retransformIndicator;

    private final AgentAssembler agentAssembler;

    private final Object lock;

    private final Instrumentation proxyInstrumentation;

    private final ProxyChainClassTransformer proxyClassTransformer;

    private volatile boolean isInitialized;

    public DefaultJavaAgent(ConfigurationManager configurationManager) {
        this(new AgentAssembler(configurationManager));
    }

    public DefaultJavaAgent(AgentAssembler agentAssembler) {
        this.agentAssembler = agentAssembler;
        this.lock = new Object();
        this.retransformIndicator = new ThreadLocal<>();
        this.proxyClassTransformer = new ProxyChainClassTransformer(this);
        this.proxyInstrumentation = createProxyInstrumentation();
        this.isInitialized = false;
    }

    /**
     * Premain method which was call before main program method
     *
     * @param args       enter java agent arguments
     * @param instrument instrumentation instance
     */
    public static void premain(String args, Instrumentation instrument) {
        agentmain(args, instrument);
    }

    /**
     * Agent main method was call in java agent attach process time
     *
     * @param args       enter java agent arguments
     * @param instrument instrumentation instance
     */
    public static void agentmain(String args, Instrumentation instrument) {
        instrumentation = instrument;
    }

    @Override
    public Instrumentation getInstrumentation() {
        ensureInitialize();
        return proxyInstrumentation;
    }

    /**
     * Method for initialize java agent
     * If agent already init, do nothing
     */
    private void ensureInitialize() {
        if (!isInitialized()) {
            synchronized (lock) {
                if (!isInitialized()) {
                    if (instrumentation == null) {
                        agentAssembler.assembly(this);
                    }

                    instrumentation.addTransformer(proxyClassTransformer, true);
                    isInitialized = true;
                }
            }
        }
    }

    @Override
    public boolean isInitialized() {
        return instrumentation != null && isInitialized;
    }

    @Override
    public String getAgentJarName() {
        return AGENT_JAR_NAME;
    }

    @Override
    public String getAgentLocationPath() {
        return System.getProperty(TEMP_DIR_KEY);
    }

    @Override
    public Class<? extends JavaAgent> getAgentClass() {
        return getClass();
    }

    @Override
    public Manifest getManifestFileName() {
        Manifest manifest = new Manifest();
        String agentClassName = ClassNameConverter.toJavaClassName(getAgentClass());
        Attributes attributes = manifest.getMainAttributes();

        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.put(new Attributes.Name("Can-Retransform-Classes"), "true");
        attributes.put(new Attributes.Name("Can-Redefine-Classes"), "true");
        attributes.put(new Attributes.Name("Agent-Class"), agentClassName);
        attributes.put(new Attributes.Name("Premain-Class"), agentClassName);

        return manifest;
    }

    @Override
    public Class<?>[] getAgentJarClasses() {
        return new Class<?>[]{JavaAgent.class, getClass()};
    }

    /**
     * Create proxy instrumentation instance for catch any calls and redirect it to selected agents
     *
     * @return instrumentation proxy instance
     */
    private Instrumentation createProxyInstrumentation() {
        Object proxyInstrumentation = Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class[]{Instrumentation.class},
                new InstrumentationInvocationHandler(this, () -> instrumentation, proxyClassTransformer)
        );

        return (Instrumentation) proxyInstrumentation;
    }

    //Package private section uses for proxy instrumentation access

    /**
     * Checks if for retransform operation uses this agent
     *
     * @return true if uses this agent
     */
    boolean isCurrentAgentUsed() {
        Boolean value = retransformIndicator.get();
        return value != null && value;
    }

    /**
     * Starts operation by retransform class for this agent
     */
    void startRetransform() {
        retransformIndicator.set(true);
    }

    /**
     * Ends operation by retransform class for this agent
     */
    void finishRetransform() {
        retransformIndicator.set(false);
    }
}