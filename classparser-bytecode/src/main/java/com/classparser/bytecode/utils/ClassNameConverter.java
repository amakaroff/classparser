package com.classparser.bytecode.utils;

import com.classparser.bytecode.exception.ByteCodeParserException;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

    private static final Map<String, String> PRIMITIVE_NAMES = new HashMap<String, String>() {{
        put("B", "byte");
        put("S", "short");
        put("I", "int");
        put("F", "float");
        put("C", "char");
        put("D", "double");
        put("J", "long");
    }};

    /**
     * Obtains full java class name from class instance
     * Resolves name for anonymous class are defined Unsafe instance
     *
     * @param clazz any class
     * @return full name of class
     */
    public static String toJavaClassName(Class<?> clazz) {
        return toJavaClassName(clazz.getName());
    }

    /**
     * Obtains full java class name from any name class
     * Resolves name for anonymous class are defined Unsafe instance
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
     * Obtains full java class name from bytecode of class
     * Resolves name for anonymous class are defined Unsafe instance
     *
     * @param bytecode bytecode of class
     * @return full name of class
     */
    public static String toJavaClassName(byte[] bytecode) {
        return toJavaClassName(getClassName(bytecode));
    }

    /**
     * Obtains java simple class name from any class name
     *
     * @param className class name
     * @return class simple name
     */
    public static String toJavaClassSimpleName(String className) {
        String fullName = toJavaClassName(className);

        String simpleName;
        if (className.startsWith("[")) {
            int countBlocks = className.lastIndexOf('[') + 1;
            if (className.endsWith(";")) {
                simpleName = className.substring(countBlocks + 1, className.length() - 1);
                if (simpleName.contains(".")) {
                    simpleName = simpleName.substring(simpleName.lastIndexOf('.') + 1);
                }
            } else {
                simpleName = PRIMITIVE_NAMES.get(className.substring(countBlocks));
            }

            return simpleName + createBlocks(countBlocks);
        }

        return fullName.substring(fullName.lastIndexOf('.') + 1);
    }

    /**
     * Create array blocks by input count
     *
     * @param count count of blocks
     * @return string of array blocks
     */
    private static String createBlocks(int count) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < count; i++) {
            stringBuilder.append("[]");
        }

        return stringBuilder.toString();
    }

    /**
     * Obtains java simple class name from class instance
     *
     * @param clazz any class
     * @return class simple name
     */
    public static String toJavaClassSimpleName(Class<?> clazz) {
        return toJavaClassSimpleName(clazz.getName());
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
        return toFileJavaClassName(toJavaClassName(clazz));
    }

    /**
     * Obtains java class name from bytecode of class uses constant pool
     * Returns class name in normally form
     * <p>
     * com/company/ClassName
     * </p>
     *
     * @param bytecode bytecode of class
     * @return class name
     */
    public static String getClassName(byte[] bytecode) {
        try (DataInputStream stream = new DataInputStream(new ByteArrayInputStream(bytecode))) {
            if (stream.readInt() == MAGIC) {
                stream.skipBytes(4);

                int sizeOfPool = stream.readUnsignedShort();
                Object[] constants = new Object[sizeOfPool];

                for (int i = 1; i < sizeOfPool; i++) {
                    switch (stream.readUnsignedByte()) {
                        case 1:
                            constants[i] = stream.readUTF();
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
                            i++;
                            break;
                        case 7:
                            constants[i] = stream.readUnsignedShort();
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

                stream.skipBytes(2);

                int referenceToClassStorageIndex = stream.readUnsignedShort();
                int indexOfClass = (int) constants[referenceToClassStorageIndex];
                Object className = constants[indexOfClass];
                if (className != null) {
                    return className.toString();
                }

                throw new ByteCodeParserException("Can't obtain class name from bytecode!");
            }

            throw new ByteCodeParserException("Invalid java bytecode of class");
        } catch (IOException exception) {
            throw new ByteCodeParserException("Constant pool is broken", exception);
        }
    }
}