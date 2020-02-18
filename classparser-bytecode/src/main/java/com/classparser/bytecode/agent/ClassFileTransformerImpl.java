package com.classparser.bytecode.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * Decorator for {@link ClassFileTransformer} for store {@link #isRetransformClass} value
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
class ClassFileTransformerImpl implements ClassFileTransformer {

    private final ClassFileTransformer classFileTransformer;

    private final boolean isRetransformClass;

    public ClassFileTransformerImpl(ClassFileTransformer classFileTransformer, boolean isRetransformClass) {
        this.classFileTransformer = classFileTransformer;
        this.isRetransformClass = isRetransformClass;
    }

    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] byteCode) throws IllegalClassFormatException {
        return classFileTransformer.transform(loader, className, classBeingRedefined, protectionDomain, byteCode);
    }

    /**
     * Getter for field {@link #isRetransformClass}
     *
     * @return true if this transformer can retransform classes
     */
    public boolean isRetransformClass() {
        return isRetransformClass;
    }

    /**
     * Getter for field {@link #classFileTransformer}
     *
     * @return class file transformer instance
     */
    public ClassFileTransformer getClassFileTransformer() {
        return classFileTransformer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClassFileTransformerImpl) {
            ClassFileTransformer classFileTransformer = ((ClassFileTransformerImpl) obj).getClassFileTransformer();
            return this.classFileTransformer == classFileTransformer;
        }

        return false;
    }
}