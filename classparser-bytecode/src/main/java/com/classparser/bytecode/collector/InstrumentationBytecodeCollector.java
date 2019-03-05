package com.classparser.bytecode.collector;

import com.classparser.bytecode.api.BytecodeCollector;
import com.classparser.bytecode.api.JavaAgent;
import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.bytecode.exception.ByteCodeParserException;
import com.classparser.bytecode.utils.ClassNameConverter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;

/**
 * Collector uses {@link Instrumentation} instance try obtain bytecode of class
 * This collector is unstable and can drop JVM because of error in instrument lib
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public class InstrumentationBytecodeCollector implements BytecodeCollector {

    private static final Object LOCK = new Object();

    private static volatile ClassFileTransformer classFileTransformer;

    private final ThreadLocal<byte[]> threadLocalBytecodeStorage;

    private volatile ConfigurationManager configurationManager;

    public InstrumentationBytecodeCollector() {
        this.threadLocalBytecodeStorage = new ThreadLocal<>();
    }

    @Override
    public byte[] getBytecode(Class<?> clazz) {
        if (clazz != null) {
            String className = ClassNameConverter.toJavaClassName(clazz);
            JavaAgent javaAgent = configurationManager.getAgent();

            Instrumentation instrumentation = javaAgent.getInstrumentation();
            try {
                if (instrumentation != null) {
                    if (instrumentation.isRetransformClassesSupported() && instrumentation.isModifiableClass(clazz)) {
                        initializeTransformer(javaAgent);
                        instrumentation.retransformClasses(clazz);
                    } else {
                        System.err.println("Class " + className + " is can't be transform.");
                        return null;
                    }
                } else {
                    throw new ByteCodeParserException("Instrumentation instance is not initialize!");
                }
            } catch (UnmodifiableClassException exception) {
                String errorMessage = "Class: \"" + className + "\" is can't transform";
                throw new IllegalArgumentException(errorMessage, exception);
            }

            return getBytesOfClass();
        } else {
            throw new NullPointerException("Class can't be a null!");
        }
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
        } else {
            throw new NullPointerException("Configuration Manager is can't be null!");
        }
    }

    /**
     * Initialize class transformer uses for store bytecode
     */
    private void initializeTransformer(JavaAgent agent) {
        if (classFileTransformer == null) {
            synchronized (LOCK) {
                if (classFileTransformer == null) {
                    ClassFileTransformer classFileTransformer = new BytecodeStoreClassFileTransformer();
                    Instrumentation instrumentation = agent.getInstrumentation();
                    instrumentation.addTransformer(classFileTransformer, true);
                    InstrumentationBytecodeCollector.classFileTransformer = classFileTransformer;
                }
            }
        }
    }

    /**
     * Obtains bytecode of class from storage map
     *
     * @return bytecode of this class or null if map value is absent
     */
    private byte[] getBytesOfClass() {
        byte[] bytecode = threadLocalBytecodeStorage.get();
        threadLocalBytecodeStorage.remove();

        return bytecode;
    }

    /**
     * Resolves java class name and append it to bytecode map
     *
     * @param bytecode bytecode of class
     */
    private void uploadByteCodeOfClassToHolder(byte[] bytecode) {
        threadLocalBytecodeStorage.set(bytecode);
    }

    /**
     * Simple class file transformer uses for store of re-transformed bytecode
     */
    private class BytecodeStoreClassFileTransformer implements ClassFileTransformer {

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                ProtectionDomain protectionDomain, byte[] bytecode) {
            uploadByteCodeOfClassToHolder(bytecode);
            return null;
        }
    }
}