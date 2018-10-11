package com.classparser.bytecode.utils;

import com.classparser.bytecode.api.BytecodeCollector;
import com.classparser.bytecode.collector.ChainBytecodeCollector;
import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.util.Reflection;
import sun.reflect.ConstantPool;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Iterable provides functionality by iteration by constant pool of class
 * and collect any classes from it
 * <p>
 * Package private access
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
final class ConstantPoolClassIterable implements Iterable<Class<?>> {

    private final Class<?> clazz;

    private final ConfigurationManager configurationManager;

    public ConstantPoolClassIterable(Class<?> clazz, ConfigurationManager configurationManager) {
        this.clazz = clazz;
        this.configurationManager = configurationManager;
    }

    @Override
    public Iterator<Class<?>> iterator() {
        try {
            return new UnsafeReflectConstantPoolClassIterator(clazz);
        } catch (NoClassDefFoundError | ConstantPoolNotSupportedException error) {
            return new BytecodeConstantPoolClassIterator(clazz);
        }
    }

    /**
     * Private exception uses for message if ReflectionConstantPool can't be used
     */
    private class ConstantPoolNotSupportedException extends RuntimeException {
    }

    /**
     * Unsafe implementation uses {@link ConstantPool} for iterable by constant pool
     * This implementation is very fast for a little sets of classes
     */
    @SuppressWarnings("sunapi")
    private class UnsafeReflectConstantPoolClassIterator implements Iterator<Class<?>> {

        private final ConstantPool constantPool;

        private final int size;

        private int index = 1;

        public UnsafeReflectConstantPoolClassIterator(Class<?> clazz) {
            this.constantPool = getConstantPool(clazz);
            this.size = constantPool.getSize();
        }

        @Override
        public boolean hasNext() {
            return iterateToNextOrNull() != null;
        }

        @Override
        public Class<?> next() {
            if (hasNext()) {
                Class<?> clazz = iterateToNextOrNull();
                index++;
                return clazz;
            }

            throw new NoSuchElementException("No such the follow class constant!");
        }

        /**
         * Checks and move iterator to position where found next class in pool
         *
         * @return class or null if in constant pool no more exists classes
         */
        private Class<?> iterateToNextOrNull() {
            while (index < size) {
                try {
                    return constantPool.getClassAt(index);
                } catch (IllegalArgumentException | IllegalAccessError | NoClassDefFoundError ignore) {
                }
                index++;
            }

            return null;
        }

        /**
         * Obtains unsafe instance of constant pool
         *
         * @param clazz any class
         * @return reflect constant pool instance
         */
        private ConstantPool getConstantPool(Class<?> clazz) {
            if (!clazz.isArray() && !clazz.isPrimitive()) {
                if (isHotSpotJVM() || isOpenJDKVM()) {
                    return getHotSpotConstantPool(clazz);
                } else if (isJ9JVM()) {
                    return getJ9ConstantPool(clazz);
                } else {
                    throw new ConstantPoolNotSupportedException();
                }
            }

            throw new IllegalArgumentException("Array or primitive types have not constant pool!");
        }

        /**
         * Checks if current JVM is HotSpot
         *
         * @return true if JVM is hotspot
         */
        private boolean isHotSpotJVM() {
            return System.getProperty("java.vm.name").toLowerCase().contains("hotspot");
        }

        /**
         * Checks if current JVM is HotSpot
         *
         * @return true if JVM is hotspot
         */
        private boolean isOpenJDKVM() {
            return System.getProperty("java.vm.name").toLowerCase().contains("openjdk");
        }

        /**
         * Obtains ConstantPool instance uses reflection from HotSpot JDK
         *
         * @param clazz any class
         * @return constant pool instance
         */
        private ConstantPool getHotSpotConstantPool(Class<?> clazz) {
            Method constantPoolMethod = Reflection.getMethod(Class.class, "getConstantPool");
            constantPoolMethod.setAccessible(true);
            try {
                return (ConstantPool) Reflection.invoke(constantPoolMethod, clazz);
            } finally {
                constantPoolMethod.setAccessible(false);
            }
        }

        /**
         * Checks if current JVM is J9
         *
         * @return true if JVM is J9
         */
        private boolean isJ9JVM() {
            return System.getProperty("java.vm.name").toLowerCase().contains("j9");
        }

        /**
         * Obtains ConstantPool instance uses reflection from J9 JDK
         *
         * @param clazz any class
         * @return constant pool instance
         */
        private ConstantPool getJ9ConstantPool(Class<?> clazz) {
            Class<?> access = Reflection.forName("java.lang.Access");
            Method constantPoolMethod = Reflection.getMethod(access, "getConstantPool", Object.class);
            return (ConstantPool) Reflection.invokeStatic(constantPoolMethod, clazz);
        }
    }

    /**
     * More safe implementation of Constant Pool iterable uses bytecode of class for
     * searching classes
     */
    private class BytecodeConstantPoolClassIterator implements Iterator<Class<?>> {

        private final String[] constantPool;

        private final List<Integer> indexes;

        private final int size;

        private int index = 1;

        public BytecodeConstantPoolClassIterator(Class<?> clazz) {
            try {
                DataInputStream stream = getBytecodeDataStream(clazz);
                int constantPoolSize = stream.readUnsignedShort();
                constantPool = new String[constantPoolSize];
                indexes = new ArrayList<>();
                for (int i = 1; i < constantPoolSize; ++i) {
                    switch (stream.readUnsignedByte()) {
                        case 1:
                            constantPool[i] = stream.readUTF();
                            break;
                        case 2:
                        case 13:
                        case 14:
                        case 17:
                        default:
                            break;
                        case 3:
                        case 4:
                        case 9:
                        case 10:
                        case 11:
                        case 12:
                        case 18:
                            stream.skipBytes(4);
                            break;
                        case 5:
                        case 6:
                            stream.skipBytes(8);
                            ++i;
                            break;
                        case 7:
                            indexes.add(stream.readUnsignedShort());
                            break;
                        case 8:
                        case 16:
                            stream.skipBytes(2);
                            break;
                        case 15:
                            stream.skipBytes(3);
                            break;
                    }
                }

                size = indexes.size();
            } catch (IOException exception) {
                throw new IllegalArgumentException("Invalid reading bytecode of class!", exception);
            }
        }

        @Override
        public boolean hasNext() {
            return iterateToNextNotNull() != null;
        }

        /**
         * Checks and move iterator to position where found next class in pool
         *
         * @return class or null if in constant pool no more exists classes
         */
        public Class<?> iterateToNextNotNull() {
            while (index < size) {
                Integer integer = indexes.get(index);
                String className = constantPool[integer].replace('/', '.');
                try {
                    return Class.forName(className, false, getClass().getClassLoader());
                } catch (ClassNotFoundException ignore) {
                    index++;
                }
            }

            return null;
        }

        @Override
        public Class<?> next() {
            if (hasNext()) {
                Class<?> clazz = iterateToNextNotNull();
                index++;
                if (clazz != null) {
                    return null;
                }
            }

            throw new NoSuchElementException("No such the follow class constant!");
        }

        /**
         * Obtains bytecode of class and transform it to data stream
         *
         * @param clazz any class
         * @return data stream with bytecode of class
         * @throws IOException any problems occurred at creating data stream
         */
        private DataInputStream getBytecodeDataStream(Class<?> clazz) throws IOException {
            if (!clazz.isArray() && !clazz.isPrimitive()) {
                BytecodeCollector chainByteCodeCollector = new ChainBytecodeCollector(configurationManager);
                byte[] bytecode = chainByteCodeCollector.getBytecode(clazz);
                if (bytecode != null) {
                    DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(bytecode));
                    dataInputStream.skipBytes(8);

                    return dataInputStream;
                }

                String className = ClassNameConverter.toJavaClassName(clazz);
                throw new IllegalArgumentException("Bytecode for class: " + className + " is not found!");
            }

            throw new IllegalArgumentException("Array or primitive types have not constant pool!");
        }
    }
}