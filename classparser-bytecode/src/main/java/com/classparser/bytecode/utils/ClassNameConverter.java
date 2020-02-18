package com.classparser.bytecode.utils;

import com.classparser.bytecode.exception.ByteCodeParserException;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;

/**
 * Class provides functionality by converting class name
 * to other representations
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public class ClassNameConverter {

    private static final int MAGIC = 0xCAFEBABE;

    private static final String CLASS_FILE_SUFFIX = ".class";

    /**
     * Obtains full java class name from class instance
     * Resolves name for anonymous class are defined by the Unsafe instance
     *
     * @param clazz any class
     * @return full name of class
     */
    public static String toJavaClassName(Class<?> clazz) {
        return toJavaClassName(clazz.getName());
    }

    /**
     * Obtains full java class name from any name class
     * Resolves name for anonymous class are defined by the Unsafe instance
     *
     * @param className class name
     * @return full name of class
     */
    public static String toJavaClassName(String className) {
        if (className.endsWith(CLASS_FILE_SUFFIX)) {
            className = className.substring(0, className.lastIndexOf(CLASS_FILE_SUFFIX));
        }

        className = className.replace('\\', '.').replace('/', '.');

        if (className.contains(".")) {
            String templateName = className.substring(className.lastIndexOf('.') + 1);
            if (Character.isDigit(templateName.charAt(0))) {
                className = className.substring(0, className.lastIndexOf('.'));
            }
        }


        return className;
    }

    /**
     * Obtains full java class name from byte code of class
     * Resolves name for anonymous class are defined Unsafe instance
     *
     * @param byteCode byte code of class
     * @return full name of class
     */
    public static String toJavaClassName(byte[] byteCode) {
        return toJavaClassName(getClassName(byteCode));
    }

    /**
     * Obtains java simple class name from any class name
     *
     * @param className class name
     * @return class simple name
     */
    public static String toJavaClassSimpleName(String className) {
        String fullName = toJavaClassName(className);
        return fullName.substring(fullName.lastIndexOf('.') + 1);
    }

    /**
     * Obtains java simple class name from class instance
     *
     * @param clazz any class
     * @return class simple name
     */
    public static String toJavaClassSimpleName(Class<?> clazz) {
        String simpleName = clazz.getSimpleName();
        if (simpleName.isEmpty() || simpleName.contains("/")) {
            return toJavaClassSimpleName(clazz.getName());
        }
        return simpleName;
    }

    /**
     * Creates java class jar file name from class name
     *
     * @param clazz any class
     * @return class jar file name
     */
    public static String toJarJavaClassName(Class<?> clazz) {
        return toJarJavaClassName(toJavaClassName(clazz));
    }

    /**
     * Creates java class jar file name from class name
     *
     * @param className java class name
     * @return class jar file name
     */
    public static String toJarJavaClassName(String className) {
        return toJavaClassName(className).replace('.', '/') + CLASS_FILE_SUFFIX;
    }

    /**
     * Creates java class file name from class name
     *
     * @param className java class name
     * @return class file name
     */
    public static String toFileJavaClassName(String className) {
        return toJavaClassName(className).replace('.', File.separatorChar) + CLASS_FILE_SUFFIX;
    }

    /**
     * Creates java class file name from class name
     *
     * @param clazz java class
     * @return class file name
     */
    public static String toFileJavaClassName(Class<?> clazz) {
        return toJavaClassName(clazz).replace('.', File.separatorChar) + CLASS_FILE_SUFFIX;
    }

    /**
     * Obtains java class name from byte code of class uses constant pool
     * Returns class name in normally form
     * <p>
     * com/company/ClassName
     * </p>
     *
     * @param byteCode byte code of class
     * @return class name
     */
    public static String getClassName(byte[] byteCode) {
        try (DataInputStream stream = new DataInputStream(new ByteArrayInputStream(byteCode))) {
            if (stream.readInt() == MAGIC) {
                stream.skipBytes(4);

                int sizeOfPool = stream.readUnsignedShort();
                Object[] constants = new Object[sizeOfPool];

                for (int i = 1; i < sizeOfPool; i++) {
                    switch (stream.readUnsignedByte()) {
                        case 1:
                            constants[i] = stream.readUTF();
                            break;
                        case 3: case 4: case 9: case 10: case 11: case 12: case 18:
                            stream.skipBytes(4);
                            break;
                        case 5: case 6:
                            stream.skipBytes(8);
                            i++;
                            break;
                        case 7:
                            constants[i] = stream.readUnsignedShort();
                            break;
                        case 8: case 16:
                            stream.skipBytes(2);
                            break;
                        case 15:
                            stream.skipBytes(3);
                            break;
                    }
                }

                stream.skipBytes(2);

                int referenceToClassStorageIndex = stream.readUnsignedShort();
                int indexOfClass = (int) constants[referenceToClassStorageIndex];
                Object className = constants[indexOfClass];
                if (className != null) {
                    return className.toString();
                }


                throw new ClassFormatError("Can't obtain class name from byte code!");
            }

            throw new ClassFormatError("Invalid java byte code of class");
        } catch (IOException exception) {
            throw new ByteCodeParserException("Constant pool is broken", exception);
        }
    }
}