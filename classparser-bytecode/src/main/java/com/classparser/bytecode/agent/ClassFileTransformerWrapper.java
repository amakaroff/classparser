package com.classparser.bytecode.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * Decorator for {@link ClassFileTransformer} for store {@link #isCanRetransformClasses} value
 * <p>
 * Non public API
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
final class ClassFileTransformerWrapper implements ClassFileTransformer {

    private final ClassFileTransformer classFileTransformer;

    private final boolean isCanRetransformClasses;

    ClassFileTransformerWrapper(ClassFileTransformer classFileTransformer, boolean isCanRetransformClasses) {
        this.classFileTransformer = classFileTransformer;
        this.isCanRetransformClasses = isCanRetransformClasses;
    }

    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] bytecode) throws IllegalClassFormatException {
        return classFileTransformer.transform(loader, className, classBeingRedefined, protectionDomain, bytecode);
    }

    /**
     * Getter for field {@link #isCanRetransformClasses}
     *
     * @return true if this transformer can retransform classes
     */
    boolean isCanRetransformClasses() {
        return isCanRetransformClasses;
    }

    @Override
    public int hashCode() {
        return classFileTransformer.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object != null && object.getClass() == getClass()) {
            ClassFileTransformerWrapper transformerWrapper = (ClassFileTransformerWrapper) object;
            return this.classFileTransformer == transformerWrapper.classFileTransformer;
        }

        return false;
    }
}