package com.classparser.bytecode.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Proxy class file transformer uses for redirect calls of
 * {@link Instrumentation#retransformClasses(Class[])} for different java agents
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
class ProxyChainClassTransformer implements ClassFileTransformer {

    private final DefaultJavaAgent defaultJavaAgent;

    private final Queue<ClassFileTransformerImpl> transformersQueue;

    public ProxyChainClassTransformer(DefaultJavaAgent defaultJavaAgent) {
        this.defaultJavaAgent = defaultJavaAgent;
        this.transformersQueue = new ConcurrentLinkedQueue<>();
    }

    /**
     * Add custom class file transformer
     *
     * @param classFileTransformer any class file transformer
     */
    public void addTransformer(ClassFileTransformerImpl classFileTransformer) {
        transformersQueue.add(classFileTransformer);
    }

    /**
     * Remove class file transformer
     *
     * @param classFileTransformer any class file transformer
     */
    public boolean removeTransformer(ClassFileTransformer classFileTransformer) {
        return transformersQueue.remove(new ClassFileTransformerImpl(classFileTransformer, false));
    }

    @Override
    public final byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                  ProtectionDomain protectionDomain, byte[] bytecode) throws IllegalClassFormatException {
        if (defaultJavaAgent.isCurrentAgentUsed()) {
            byte[] currentBytecode = bytecode;
            for (ClassFileTransformerImpl classFileTransformer : transformersQueue) {
                byte[] transformedBytecode = classFileTransformer.transform(loader, className,
                        classBeingRedefined, protectionDomain, currentBytecode);
                if (transformedBytecode != null && classFileTransformer.isRetransformClass()) {
                    currentBytecode = transformedBytecode;
                }
            }

            if (Arrays.equals(bytecode, currentBytecode)) {
                return null;
            } else {
                return currentBytecode;
            }
        }

        return null;
    }
}