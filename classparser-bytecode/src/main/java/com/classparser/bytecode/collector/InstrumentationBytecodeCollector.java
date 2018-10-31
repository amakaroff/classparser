package com.classparser.bytecode.collector;

import com.classparser.bytecode.api.BytecodeCollector;
import com.classparser.bytecode.api.JavaAgent;
import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.bytecode.utils.ClassNameConverter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collector uses {@link Instrumentation} instance try obtain bytecode of class
 * This collector is unstable and can drop JVM because of error in instrument lib
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public class InstrumentationBytecodeCollector implements BytecodeCollector {

    private static volatile ClassFileTransformer classFileTransformer;

    private static Map<String, byte[]> bytecodeStorage;

    private ConfigurationManager configurationManager;

    @Override
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public byte[] getBytecode(Class<?> clazz) {
        if (isEnable()) {
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
                        throw new IllegalArgumentException(errorMessage, exception);
                    }

                    return getBytesOfClass(clazz);
                }
            }
        }

        return null;
    }

    @Override
    public int getOrder() {
        return 1000;
    }

    @Override
    public boolean isEnable() {
        ConfigurationManager configurationManager = this.configurationManager;
        return configurationManager != null && configurationManager.isEnableInstrumentationBytecodeCollector();
    }

    @Override
    public void setConfigurationManager(ConfigurationManager configurationManager) {
        if (configurationManager != null) {
            this.configurationManager = configurationManager;
        }
    }

    /**
     * Initialize class transformer uses for store bytecode
     */
    private void initializeTransformer(JavaAgent agent) {
        if (classFileTransformer == null) {
            synchronized (InstrumentationBytecodeCollector.class) {
                if (classFileTransformer == null) {
                    classFileTransformer = new BytecodeStoreClassFileTransformer();
                    bytecodeStorage = new ConcurrentHashMap<>();
                    Instrumentation instrumentation = agent.getInstrumentation();
                    instrumentation.addTransformer(classFileTransformer, true);
                }
            }
        }
    }

    /**
     * Obtains bytecode of class from storage map
     *
     * @param clazz any class
     * @return bytecode of this class or null if map value is absent
     */
    private byte[] getBytesOfClass(Class<?> clazz) {
        String className = ClassNameConverter.toJavaClassName(clazz);
        byte[] bytecode = bytecodeStorage.get(className);
        bytecodeStorage.remove(className);

        return bytecode;
    }

    /**
     * Resolves java class name and append it to bytecode map
     *
     * @param className java class name
     * @param bytecode  bytecode of class
     */
    private void uploadByteCodeOfClassToHolder(String className, byte[] bytecode) {
        String javaBasedClassName = ClassNameConverter.toJavaClassName(className);
        bytecodeStorage.put(javaBasedClassName, bytecode);
    }

    /**
     * Simple class file transformer uses for store of re-transformed bytecode
     */
    private class BytecodeStoreClassFileTransformer implements ClassFileTransformer {

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                ProtectionDomain protectionDomain, byte[] bytecode) {
            uploadByteCodeOfClassToHolder(className, bytecode);
            return null;
        }
    }
}