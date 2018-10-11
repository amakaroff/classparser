package com.classparser.reflection.parser.base;

import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.configuration.ReflectionParserManager;
import com.classparser.util.Reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Class provides functionality by parsing value for any objects
 * Depending on context {@link ReflectionParserManager} of parsing
 *
 * @author Aleksey Makarov
 * @since 1.0.0
 */
public class ValueParser {

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    private final GenericTypeParser genericTypeParser;

    private final ReflectionParserManager manager;

    private final AnnotationParser annotationParser;

    private final ConfigurationManager configurationManager;

    public ValueParser(GenericTypeParser genericTypeParser, AnnotationParser annotationParser,
                       ReflectionParserManager manager) {
        this.genericTypeParser = genericTypeParser;
        this.annotationParser = annotationParser;
        this.manager = manager;
        this.configurationManager = manager.getConfigurationManager();
    }

    /**
     * Try obtain and parse value for any object
     * This method supported: numbers, chars, strings, annotations, classes, enums, arrays
     * And also static fields and default methods in annotations
     *
     * @param object any object
     * @return parsed value
     */
    public String getValue(Object object) {
        if (isField(object)) {
            return getFieldValue((Field) object);
        }

        if (isAnnotationMethod(object)) {
            return getDefaultAnnotationValue((Method) object);
        }

        if (object != null) {
            Class<?> clazz = object.getClass();
            if (clazz.isArray()) {
                List<String> listValues = new ArrayList<>();
                for (Object listValue : getArrayValues(object)) {
                    if (!isObjectValue(listValue)) {
                        listValues.add(getValue(listValue));
                    }
                }

                String values = String.join(", ", listValues);
                if (values.isEmpty()) {
                    return values;
                } else {
                    return '{' + values + '}';
                }
            }

            if (isEnum(clazz)) {
                if (!clazz.isEnum()) {
                    clazz = clazz.getSuperclass();
                }
                return genericTypeParser.parseType(clazz) + "." + object;
            } else if (object instanceof String) {
                return "\"" + object + "\"";
            } else if (object instanceof Character) {
                return "\'" + object + "\'";
            } else if (object instanceof Number || object instanceof Boolean) {
                return object.toString() + getLiteral(object);
            } else if (object instanceof Class) {
                return genericTypeParser.parseType((Class) object) + ".class";
            } else if (object instanceof Annotation) {
                Class<? extends Annotation> annotationType = ((Annotation) object).annotationType();
                if (annotationType != null && annotationType.isAnnotation()) {
                    return annotationParser.parseAnnotation((Annotation) object);
                }
            }

            return "";

        }

        return null;
    }

    /**
     * Convert array object to really array
     *
     * @param object any object
     * @return array or empty array if object is not array
     */
    public Object[] getArrayValues(Object object) {
        if (object != null && object.getClass().isArray()) {
            int length = Array.getLength(object);
            Object[] objects = new Object[length];

            for (int i = 0; i < length; i++) {
                objects[i] = Array.get(object, i);
            }

            return objects;
        }

        return EMPTY_OBJECT_ARRAY;
    }

    /**
     * Checks if object is non string value
     *
     * @param object any object
     * @return true if this not string value
     */
    private boolean isObjectValue(Object object) {
        return object != null && !(object instanceof String) && object.toString().isEmpty();
    }

    /**
     * Obtains literal for number value
     *
     * @param object any number
     * @return literal for number value
     */
    private String getLiteral(Object object) {
        if (object instanceof Long) {
            return "L";
        } else if (object instanceof Float) {
            Float floatValue = (Float) object;
            if (!Float.isInfinite(floatValue) && !Float.isNaN(floatValue)) {
                return "f";
            }
        } else if (object instanceof Double) {
            Double doubleValue = (Double) object;
            if (!Double.isInfinite(doubleValue) && !Double.isNaN(doubleValue)) {
                return "d";
            }
        }

        return "";
    }

    /**
     * Obtains default value for annotation method
     *
     * @param method any method
     * @return default value for method
     */
    private String getDefaultAnnotationValue(Method method) {
        String defaultAnnotationValue = "";

        String defaultValue = getValue(method.getDefaultValue());
        if (defaultValue != null) {
            defaultAnnotationValue += " default " + defaultValue;
        }

        return defaultAnnotationValue;
    }

    /**
     * Obtains value from static field
     *
     * @param field any field
     * @return value from static field or empty string if value can't be obtained
     */
    private String getFieldValue(Field field) {
        if (configurationManager.isDisplayFieldValue() &&
                Modifier.isStatic(field.getModifiers()) &&
                !isEnumConstant(field)) {
            Object value = Reflection.get(field);
            if (!isField(value)) {
                String fieldValue = getValue(value);
                if (!"".equals(fieldValue)) {
                    return " = " + fieldValue;
                }
            }
        }

        return "";
    }

    /**
     * Checks field for accessory to current parsed class
     *
     * @param object any field
     * @return true if field accessory to current parsed class
     */
    private boolean isField(Object object) {
        return object instanceof Field && ((Field) object).getDeclaringClass() == manager.getCurrentParsedClass();
    }

    /**
     * Checks method for accessory to annotation
     *
     * @param object any method
     * @return true if method is accessory to annotation
     */
    private boolean isAnnotationMethod(Object object) {
        return object instanceof Method && ((Method) object).getDeclaringClass().isAnnotation();
    }

    /**
     * Checks if field is enum constant
     *
     * @param field any field
     * @return true if field is enum constant
     */
    private boolean isEnumConstant(Field field) {
        return field.isEnumConstant() && field.getDeclaringClass().isEnum();
    }

    /**
     * Checks class is Enum or generated enum constant classes
     *
     * @param clazz any class
     * @return true if class is enum
     */
    private boolean isEnum(Class<?> clazz) {
        if (clazz != null) {
            if (clazz.isEnum()) {
                return true;
            } else {
                return isEnum(clazz.getSuperclass());
            }
        }

        return false;
    }
}