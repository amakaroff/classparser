package com.classparser.bytecode.assembly.build;

import com.classparser.bytecode.api.BytecodeCollector;
import com.classparser.bytecode.collector.ClassFileBytecodeCollector;
import com.classparser.bytecode.exception.ByteCodeParserException;
import com.classparser.bytecode.utils.ClassNameConverter;
import com.classparser.exception.file.FileCreatingException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Class uses builder pattern for creating jar with agent class
 * for attaching to JVM
 * <p>
 * Stores agent jar to java temp directory
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public final class AgentBuilder {

    private static final String DEFAULT_AGENT_JAR_FILE_NAME = "agent.jar";

    public static AgentJarBuilder createBuilder() {
        return new Builder();
    }

    /**
     * Inner builder implemented functionality by agent jar creating
     */
    private static class Builder implements AgentJarBuilder {

        private static final String JAR_SUFFIX = ".jar";

        private static final String DEFAULT_AGENT_JAR_NAME = "agent.jar";

        private final Collection<Class<?>> attachedClasses;

        private String agentName;

        private String agentDirLocation;

        private Class<?> agentClass;

        private Manifest manifest;

        private Builder() {
            this.agentName = DEFAULT_AGENT_JAR_NAME;
            this.agentDirLocation = "";
            this.attachedClasses = new HashSet<>();
        }

        @Override
        public AgentJarBuilder addAgentName(String agentName) {
            if (agentName != null) {
                this.agentName = appendJarSuffixIfNeeded(agentName);
            }

            return this;
        }

        @Override
        public AgentJarBuilder addAgentDirLocation(String dirLocation) {
            if (dirLocation != null) {
                this.agentDirLocation = dirLocation;
            }

            return this;
        }

        @Override
        public AgentJarBuilder addClasses(Class<?>... attachedClasses) {
            if (attachedClasses != null) {
                this.attachedClasses.addAll(Arrays.asList(attachedClasses));
            }

            return this;
        }

        @Override
        public AgentJarBuilder addManifest(Manifest manifest) {
            if (manifest != null) {
                this.manifest = manifest;
            }

            return this;
        }

        @Override
        public AgentJarBuilder addAgentClass(Class<?> agentClass) {
            if (!isAgentClass(agentClass)) {
                String className = ClassNameConverter.toJavaClassName(agentClass);
                throw new IllegalArgumentException("Class \"" + className + "\" is can't be an agent class");
            }

            this.agentClass = agentClass;
            return this;
        }

        @Override
        public String build() {
            findAgentClass();
            if (agentClass == null) {
                throw new NullPointerException("Java agent class can't be null");
            }

            if (agentName == null) {
                agentName = DEFAULT_AGENT_JAR_FILE_NAME;
            }

            String agentPath = agentDirLocation + agentName;

            this.attachedClasses.add(agentClass);
            try (JarOutputStream jarStream = new JarOutputStream(new FileOutputStream(agentPath), getManifest())) {
                BytecodeCollector reader = new ClassFileBytecodeCollector();

                for (Class<?> attachedClass : attachedClasses) {
                    if (attachedClass != null) {
                        jarStream.putNextEntry(new JarEntry(ClassNameConverter.toJarJavaClassName(attachedClass)));

                        byte[] bytecode = reader.getBytecode(attachedClass);
                        if (bytecode == null) {
                            String className = ClassNameConverter.toJavaClassName(attachedClass);
                            String exceptionMessage = "Can't find bytecode of class \"" + className + "\"";
                            throw new ByteCodeParserException(exceptionMessage);
                        }

                        jarStream.write(bytecode);
                        jarStream.flush();
                        jarStream.closeEntry();
                    }
                }

                jarStream.finish();
            } catch (IOException exception) {
                throw new FileCreatingException("Java agent jar is can't be created", exception, agentPath);
            }

            return agentPath;
        }

        /**
         * Tryings find agent class in all attach classes and set it to {@link Builder#agentClass}
         */
        private void findAgentClass() {
            if (this.agentClass == null) {
                for (Class<?> attachedClass : this.attachedClasses) {
                    if (isAgentClass(attachedClass)) {
                        this.agentClass = attachedClass;
                        return;
                    }
                }
            }
        }

        /**
         * Checks if class exists special agent signature method
         * <code>
         * void agentmain(String args, Instrumentation instrumentation)
         * </code>
         * or
         * <code>
         * void premain(String args, Instrumentation instrumentation)
         * </code>
         *
         * @param clazz any class
         * @return true if class exists special agent method
         */
        private boolean isAgentClass(Class<?> clazz) {
            if (clazz != null) {
                Method agentmain = getAgentMethod(clazz, "agentmain");
                Method premain = getAgentMethod(clazz, "premain");

                if (agentmain == null && premain == null) {
                    return false;
                }

                Method agentMethod = agentmain != null ? agentmain : premain;

                int modifiers = agentMethod.getModifiers();
                return Modifier.isStatic(modifiers) && agentMethod.getReturnType().equals(Void.TYPE);
            }

            return false;
        }

        /**
         * Tryings obtain agent method by name
         *
         * @param clazz      any class
         * @param methodName name of agent method
         * @return agent method or null if it's not found
         */
        private Method getAgentMethod(Class<?> clazz, String methodName) {
            try {
                return clazz.getDeclaredMethod(methodName, String.class, Instrumentation.class);
            } catch (NoSuchMethodException exception) {
                return null;
            }
        }

        /**
         * Tryings and append any suffix if it's needed
         *
         * @param value any value
         * @return value exists suffix
         */
        private String appendJarSuffixIfNeeded(String value) {
            if (!value.endsWith(JAR_SUFFIX)) {
                return value + JAR_SUFFIX;
            }

            return value;
        }

        /**
         * Finds or create manifest entity for storing in jar
         *
         * @return {@link Manifest} instance
         */
        private Manifest getManifest() {
            return manifest;
        }
    }
}