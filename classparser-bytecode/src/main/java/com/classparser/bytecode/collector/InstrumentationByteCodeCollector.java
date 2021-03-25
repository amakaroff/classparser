package com.classparser.bytecode.collector;

import com.classparser.bytecode.api.ByteCodeCollector;
import com.classparser.bytecode.api.JavaAgent;
import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.bytecode.exception.classes.IllegalClassException;
import com.classparser.bytecode.utils.ClassNameConverter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Collector uses {@link Instrumentation} instance try obtain byte code of class
 * This collector is unstable on java 8 and can drop JVM because of error in instrument lib
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public class InstrumentationByteCodeCollector implements ByteCodeCollector {

    private static final Lock LOCK = new ReentrantLock();

    private static volatile ClassFileTransformer classFileTransformer;

    private static Map<String, byte[]> byteCodeStorage;

    private ConfigurationManager configurationManager;

    @Override
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public byte[] getByteCode(Class<?> clazz) {
        if (clazz != null) {
            synchronized (clazz) {
                String className = ClassNameConverter.toJavaClassName(clazz);
                Instrumentation instrumentation = configurationManager.getAgent().getInstrumentation();
                try {
                    if (instrumentation != null) {
                        if (instrumentation.isRetransformClassesSupported() && instrumentation.isModifiableClass(clazz)) {
                            initializeTransformer(configurationManager.getAgent());
                            instrumentation.retransformClasses(clazz);
                        } else {
                            System.err.println("Class " + className + " is can't be transform.");
                        }
                    } else {
                        System.err.println("Instrumentation instance is not initialize!");
                    }
                } catch (UnmodifiableClassException exception) {
                    String errorMessage = "Class: \"" + className + "\" is can't transform";
                    throw new IllegalClassException(errorMessage, exception, clazz);
                }

                return getBytesOfClass(clazz);
            }
        }

        return null;
    }

    @Override
    public int getOrder() {
        return 1000;
    }

    @Override
    public boolean isEnabled() {
        return configurationManager.isEnableInstrumentationByteCodeCollector();
    }

    @Override
    public void setConfigurationManager(ConfigurationManager configurationManager) {
        if (configurationManager != null) {
            this.configurationManager = configurationManager;
        }
    }

    /**
     * Initialize class transformer uses for store byte code
     */
    private void initializeTransformer(JavaAgent agent) {
        if (classFileTransformer == null) {
            LOCK.lock();
            try {
                if (classFileTransformer == null) {
                    classFileTransformer = new ByteCodeStoreClassFileTransformer();
                    byteCodeStorage = new ConcurrentHashMap<>();
                    Instrumentation instrumentation = agent.getInstrumentation();
                    instrumentation.addTransformer(classFileTransformer, true);
                }
            } finally {
                LOCK.unlock();
            }
        }
    }

    /**
     * Obtains byte code of class from storage map
     *
     * @param clazz any class
     * @return byte code of this class or null if map value is absent
     */
    private byte[] getBytesOfClass(Class<?> clazz) {
        String className = ClassNameConverter.toJavaClassName(clazz);
        return byteCodeStorage.remove(className);
    }

    /**
     * Resolves java class name and append it to byte code map
     *
     * @param className java class name
     * @param byteCode  byte code of class
     */
    private void uploadByteCodeOfClassToHolder(String className, byte[] byteCode) {
        String javaBasedClassName = ClassNameConverter.toJavaClassName(className);
        byteCodeStorage.put(javaBasedClassName, byteCode);
    }

    /**
     * Simple class file transformer uses for store of re-transformed byte code
     */
    private class ByteCodeStoreClassFileTransformer implements ClassFileTransformer {

        @Override
        public byte[] transform(ClassLoader loader, 
                                String className, 
                                Class<?> classBeingRedefined,
                                ProtectionDomain protectionDomain,
                                byte[] byteCode) {
            uploadByteCodeOfClassToHolder(className, byteCode);
            return null;
        }
    }
}