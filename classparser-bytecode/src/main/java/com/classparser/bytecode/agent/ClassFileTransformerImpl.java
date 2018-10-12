package com.classparser.bytecode.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * Decorator for {@link ClassFileTransformer} for store {@link #isCanRetransformClasses} value
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
class ClassFileTransformerImpl implements ClassFileTransformer {

    private final ClassFileTransformer classFileTransformer;

    private final boolean isCanRetransformClasses;

    public ClassFileTransformerImpl(ClassFileTransformer classFileTransformer, boolean isCanRetransformClasses) {
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
    public boolean isCanRetransformClasses() {
        return isCanRetransformClasses;
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
    public boolean equals(Object object) {
        if (object instanceof ClassFileTransformerImpl) {
            ClassFileTransformer classFileTransformer = ((ClassFileTransformerImpl) object).getClassFileTransformer();
            return this.classFileTransformer == classFileTransformer;
        }

        return false;
    }
}