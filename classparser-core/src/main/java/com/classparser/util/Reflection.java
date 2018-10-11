package com.classparser.util;

import com.classparser.exception.ParsingException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Utility class uses for reflect operation
 * as set field value or invoke method and wraps check exception to unchecked
 */
public class Reflection {

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
     * Unchecked wrapper for obtaining field instance from class
     *
     * @param clazz     any class
     * @param fieldName field name
     * @return field instance
     * @throws ParsingException if method obtaining was obtains any errors
     */
    public static Field getField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException exception) {
            throw new ParsingException("Can't find field for class " + clazz + "by name: " + fieldName, exception);
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

    /**
     * Obtains static field value uses correctly access
     *
     * @param field any field
     * @return field value
     */
    public static Object get(Field field) {
        return get(field, null);
    }

    /**
     * Obtains field value uses correctly access
     *
     * @param field  any field
     * @param object object instance
     * @return value of field
     * @throws ParsingException if obtaining process was interrupter with errors
     */
    public static Object get(Field field, Object object) {
        try {
            if (field.isAccessible()) {
                return field.get(object);
            } else {
                field.setAccessible(true);
                try {
                    return field.get(object);
                } finally {
                    field.setAccessible(false);
                }
            }
        } catch (ReflectiveOperationException exception) {
            throw new ParsingException("Can't perform get field value operation for: " + field, exception);
        }
    }

    /**
     * Set value to static field
     *
     * @param field any static field
     * @param value set value
     */
    public static void set(Field field, Object value) {
        if (Modifier.isStatic(field.getModifiers())) {
            set(field, null, value);
        }
    }

    /**
     * Performs set value to field
     *
     * @param field  any field
     * @param object any object
     * @param value  set value
     */
    public static void set(Field field, Object object, Object value) {
        try {
            if (field.isAccessible()) {
                field.set(object, value);
            } else {
                field.setAccessible(true);
                try {
                    if (Modifier.isFinal(field.getModifiers())) {
                        throw new IllegalAccessException("Can't change state of final field!");
                    } else {
                        field.set(object, value);

                    }
                } finally {
                    field.setAccessible(false);
                }
            }
        } catch (ReflectiveOperationException exception) {
            throw new ParsingException("Can't perform set field value operation for: " + field, exception);
        }
    }
}
