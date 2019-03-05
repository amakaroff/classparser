package com.classparser.bytecode.agent;

import com.classparser.bytecode.api.JavaAgent;
import com.classparser.bytecode.assembly.AgentAssembler;
import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.bytecode.utils.ClassNameConverter;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.function.Supplier;
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

    private static final String TEMP_DIR_KEY = "java.io.tmpdir";

    private static final Object GLOBAL_LOCK = new Object();

    private static Instrumentation instrumentation;

    private final Object localLock;

    private final ThreadLocal<Boolean> retransformIndicator;

    private final AgentAssembler agentAssembler;

    private final Instrumentation proxyInstrumentation;

    private final ProxyChainClassTransformer proxyTransformer;

    private volatile boolean isInitialized;

    public DefaultJavaAgent(ConfigurationManager configurationManager) {
        this(new AgentAssembler(configurationManager));
    }

    public DefaultJavaAgent(AgentAssembler agentAssembler) {
        this.agentAssembler = agentAssembler;
        this.localLock = new Object();
        this.retransformIndicator = ThreadLocal.withInitial(() -> Boolean.FALSE);
        this.proxyTransformer = new ProxyChainClassTransformer(this);
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
        if (!isGlobalInitialized()) {
            synchronized (GLOBAL_LOCK) {
                if (!isGlobalInitialized()) {
                    if (instrumentation == null) {
                        agentAssembler.assembly(this);
                    }
                }
            }
        }

        if (!isLocalInitialized()) {
            synchronized (localLock) {
                if (!isLocalInitialized()) {
                    instrumentation.addTransformer(proxyTransformer, true);
                    isInitialized = true;
                }
            }
        }
    }

    /**
     * Checks if instrumentation instance is not loaded
     *
     * @return true if instrumentation instance had exists
     */
    private boolean isGlobalInitialized() {
        return instrumentation != null;
    }

    /**
     * Checks if local agent instance on initialize
     *
     * @return true if agent instance already initialized
     */
    private boolean isLocalInitialized() {
        return isInitialized;
    }

    @Override
    public boolean isInitialized() {
        return isGlobalInitialized() && isLocalInitialized();
    }

    @Override
    public String getAgentLocationPath() {
        return System.getProperty(TEMP_DIR_KEY);
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
        return new Class<?>[]{JavaAgent.class, getAgentClass()};
    }

    /**
     * Create proxy instrumentation instance for catch any calls and redirect it to selected agents
     *
     * @return instrumentation proxy instance
     */
    private Instrumentation createProxyInstrumentation() {
        Supplier<Instrumentation> supplier = () -> instrumentation;
        InvocationHandler handler = new InstrumentationInvocationHandler(this, supplier, proxyTransformer);

        ClassLoader classLoader = getClass().getClassLoader();
        Class[] classes = {Instrumentation.class};

        return (Instrumentation) Proxy.newProxyInstance(classLoader, classes, handler);
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