package com.classparser.bytecode.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * Proxy invocation handler uses for redirect any methods by bounded agent
 * <p>
 * Non public API
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
final class InstrumentationInvocationHandler implements InvocationHandler {

    private final DefaultJavaAgent defaultJavaAgent;

    private final Supplier<Instrumentation> instrumentationSupplier;

    private final ProxyChainClassTransformer proxyChainClassTransformer;

    public InstrumentationInvocationHandler(DefaultJavaAgent defaultJavaAgent,
                                            Supplier<Instrumentation> instrumentationSupplier,
                                            ProxyChainClassTransformer proxyChainClassTransformer) {
        this.defaultJavaAgent = defaultJavaAgent;
        this.instrumentationSupplier = instrumentationSupplier;
        this.proxyChainClassTransformer = proxyChainClassTransformer;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        switch (methodName) {
            case "retransformClasses":
                return retransformClasses(method, args);
            case "addTransformer":
                return addTransformer(args);
            case "removeTransformer":
                return removeTransformer(args);
            default:
                return invoke(method, args);
        }
    }

    /**
     * Simple call to instrumentation instance without proxy logic
     *
     * @param method instrumentation method
     * @param args   method call parameters
     * @return return value of method
     * @throws Exception if method throws any exceptions
     */
    private Object invoke(Method method, Object[] args) throws Exception {
        return method.invoke(instrumentationSupplier.get(), args);
    }

    /**
     * Proxy call to instrumentation instance and call class file transformers
     * for {@link #defaultJavaAgent} instance
     *
     * @param method instrumentation method
     * @param args   method call parameters
     * @return return value of method
     * @throws Exception if method throws any exceptions
     */
    private Object retransformClasses(Method method, Object[] args) throws Exception {
        defaultJavaAgent.startRetransform();
        Object returnValue = method.invoke(instrumentationSupplier.get(), args);
        defaultJavaAgent.finishRetransform();

        return returnValue;
    }

    /**
     * Proxy method adds transformer for {@link #proxyChainClassTransformer}
     *
     * @param args method call parameters
     * @return void type
     */
    private Void addTransformer(Object[] args) {
        ClassFileTransformer classFileTransformer = (ClassFileTransformer) args[0];
        boolean isRetransformEnable = (args.length == 2) && (boolean) args[1];
        ClassFileTransformerWrapper transformer = new ClassFileTransformerWrapper(classFileTransformer, isRetransformEnable);

        proxyChainClassTransformer.addTransformer(transformer);

        return null;
    }

    /**
     * Proxy method removes transformer from {@link #proxyChainClassTransformer}
     *
     * @param args method call parameters
     * @return void type
     */
    private boolean removeTransformer(Object[] args) {
        ClassFileTransformer classFileTransformer = (ClassFileTransformer) args[0];
        return proxyChainClassTransformer.removeTransformer(classFileTransformer);
    }
}