package com.classparser.bytecode.utils;

import com.classparser.bytecode.api.ByteCodeCollector;
import com.classparser.bytecode.collector.ChainByteCodeCollector;
import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.exception.ParsingException;
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
            return new ByteCodeConstantPoolClassIterator(clazz);
        }
    }

    /**
     * Private exception uses for message if ReflectionConstantPool can't be used
     */
    private static class ConstantPoolNotSupportedException extends RuntimeException {
    }

    /**
     * Unsafe implementation uses {@link ConstantPool} for iterable by constant pool
     * This implementation is very fast for a little sets of classes
     */
    @SuppressWarnings("sunapi")
    private static class UnsafeReflectConstantPoolClassIterator implements Iterator<Class<?>> {

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

        /**
         * Utility class uses for reflect operation
         * as set field value or invoke method and wraps check exception to unchecked
         */
        public static class Reflection {

            /**
             * Load class by full class name
             *
             * @param className full class name
             * @return class object
             * @throws ParsingException if class is not found
             */
            public static Class<?> forName(String className) {
                try {
                    return Class.forName(className);
                } catch (ClassNotFoundException exception) {
                    throw new ParsingException("Can't load class by name: " + className, exception);
                }
            }

            /**
             * Unchecked wrapper for obtaining method instance from class
             *
             * @param clazz      any class
             * @param methodName method name
             * @param paramTypes type of method parameter
             * @return method instance
             * @throws ParsingException if method obtaining was obtains any errors
             */
            public static Method getMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
                try {
                    return clazz.getDeclaredMethod(methodName, paramTypes);
                } catch (NoSuchMethodException exception) {
                    throw new ParsingException("Can't find method for class " + clazz + "by name: " + methodName, exception);
                }
            }

            /**
             * Static method invoke with correctly accessors and throws runtime exceptions
             *
             * @param method any method
             * @param params method parameters
             * @return method return value
             */
            public static Object invokeStatic(Method method, Object... params) {
                return invoke(method, null, params);
            }

            /**
             * Method invoke with correctly accessors and throws runtime exceptions
             *
             * @param method any method
             * @param object any object
             * @param params method parameters
             * @return method return value
             * @throws ParsingException if invoke was interrupt with errors
             */
            public static Object invoke(Method method, Object object, Object... params) {
                try {
                    if (method.isAccessible()) {
                        return method.invoke(object, params);
                    } else {
                        method.setAccessible(true);
                        try {
                            return method.invoke(object, params);
                        } finally {
                            method.setAccessible(false);
                        }
                    }
                } catch (ReflectiveOperationException exception) {
                    throw new ParsingException("Can't perform invoke method operation for: " + method, exception);
                }
            }
        }
    }

    /**
     * More safe implementation of Constant Pool iterable uses byte code of class for
     * searching classes
     */
    private class ByteCodeConstantPoolClassIterator implements Iterator<Class<?>> {

        private final String[] constantPool;

        private final List<Integer> indexes;

        private final int size;

        private int index = 1;

        public ByteCodeConstantPoolClassIterator(Class<?> clazz) {
            try {
                DataInputStream stream = getByteCodeDataStream(clazz);
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
                throw new IllegalArgumentException("Invalid reading byte code of class!", exception);
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
         * Obtains byte code of class and transform it to data stream
         *
         * @param clazz any class
         * @return data stream with byte code of class
         * @throws IOException any problems occurred at creating data stream
         */
        private DataInputStream getByteCodeDataStream(Class<?> clazz) throws IOException {
            if (!clazz.isArray() && !clazz.isPrimitive()) {
                ByteCodeCollector chainByteCodeCollector = new ChainByteCodeCollector(configurationManager);
                byte[] byteCode = chainByteCodeCollector.getByteCode(clazz);
                if (byteCode != null) {
                    DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(byteCode));
                    dataInputStream.skipBytes(8);

                    return dataInputStream;
                }

                String className = ClassNameConverter.toJavaClassName(clazz);
                throw new IllegalArgumentException("Byte code for class: " + className + " is not found!");
            }

            throw new IllegalArgumentException("Array or primitive types have not constant pool!");
        }
    }
}