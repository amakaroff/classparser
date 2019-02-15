package com.classparser.util;

import com.classparser.exception.ParsingException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Utility class uses for reflect operation
 * as set field value or invoke method and wraps check exception to unchecked
 *
 * This class will be removed in continue realises (for JDK 12), when
 * java give other way for working with constant pool
 */
@Deprecated
public class Reflection {

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