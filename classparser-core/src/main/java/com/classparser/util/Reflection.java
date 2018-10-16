package com.classparser.util;

import com.classparser.exception.ParsingException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
    public static Object getStatic(Field field) {
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
}