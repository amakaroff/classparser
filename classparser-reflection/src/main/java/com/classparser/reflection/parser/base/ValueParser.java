package com.classparser.reflection.parser.base;

import com.classparser.reflection.ParseContext;
import com.classparser.reflection.configuration.ConfigurationManager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Class provides functionality by parsing value for any objects
 *
 * @author Aleksey Makarov
 * @since 1.0.0
 */
public class ValueParser {

    private static final Object EMPTY = new Object();

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    private final GenericTypeParser genericTypeParser;

    private final AnnotationParser annotationParser;

    private final ConfigurationManager configurationManager;

    public ValueParser(GenericTypeParser genericTypeParser,
                       AnnotationParser annotationParser,
                       ConfigurationManager configurationManager) {
        this.genericTypeParser = genericTypeParser;
        this.annotationParser = annotationParser;
        this.configurationManager = configurationManager;
    }

    /**
     * Try obtain and parse value for any object
     * This method supported: numbers, chars, strings, annotations, classes, enums, arrays
     * And also static fields and default methods in annotations
     *
     * @param object any object
     * @return parsed value
     */
    public String getValue(Object object, ParseContext context) {
        if (isField(object, context)) {
            return getFieldValue((Field) object, context);
        }

        if (isAnnotationMethod(object)) {
            return getDefaultAnnotationValue((Method) object, context);
        }

        if (object != null) {
            Class<?> clazz = object.getClass();
            if (clazz.isArray()) {
                List<String> listValues = new ArrayList<>();
                boolean isAllElementsEmpty = true;
                for (Object listValue : toObjectArray(object)) {
                    String value = getValue(listValue, context);
                    if (value != null && !value.isEmpty()) {
                        isAllElementsEmpty = false;
                    }

                    listValues.add(value);
                }


                if (listValues.isEmpty() || isAllElementsEmpty) {
                    return "";
                } else {
                    return '{' + String.join(", ", listValues) + '}';
                }
            }

            if (isEnum(clazz)) {
                if (!clazz.isEnum()) {
                    clazz = clazz.getSuperclass();
                }
                return genericTypeParser.parseType(clazz, context) + "." + object;
            } else if (object instanceof String) {
                return "\"" + escapeString((String) object) + "\"";
            } else if (object instanceof Character) {
                return "'" + escapeCharacter((Character) object) + "'";
            } else if (object instanceof Number || object instanceof Boolean) {
                return object.toString() + getLiteral(object);
            } else if (object instanceof Class) {
                return genericTypeParser.parseType((Class<?>) object, context) + ".class";
            } else if (object instanceof Annotation) {
                if (context.getBaseParsedClass().isAnnotation()) {
                    Annotation annotation = (Annotation) object;
                    Class<? extends Annotation> annotationType = annotation.annotationType();
                    if (annotationType != null && annotationType.isAnnotation()) {
                        return annotationParser.parseAnnotation(annotation, context);
                    }
                }
            }

            return "";

        }

        return null;
    }

    /**
     * Escapes special characters in string
     *
     * @param value any line
     * @return escaped string
     */
    private String escapeString(String value) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            stringBuilder.append(escapeCharacter(value.charAt(i)));
        }

        return stringBuilder.toString();
    }

    /**
     * Escapes special characters
     *
     * @param character any character
     * @return escaped character
     */
    private String escapeCharacter(char character) {
        switch (character) {
            case '\n':
                return "\\n";
            case '\r':
                return "\\r";
            case '\t':
                return "\\t";
            case '\f':
                return "\\f";
            case '\b':
                return "\\b";
            default:
                return String.valueOf(character);
        }
    }

    /**
     * Convert array object to really array
     *
     * @param object any object
     * @return array or empty array if object is not array
     */
    public Object[] toObjectArray(Object object) {
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
     * Obtains literal for number value
     *
     * @param object any number
     * @return literal for number value
     */
    private String getLiteral(Object object) {
        if (object instanceof Long) {
            return "L";
        } else if (object instanceof Float) {
            float floatValue = (Float) object;
            if (!Float.isInfinite(floatValue) && !Float.isNaN(floatValue)) {
                return "f";
            }
        } else if (object instanceof Double) {
            double doubleValue = (Double) object;
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
    private String getDefaultAnnotationValue(Method method, ParseContext context) {
        String defaultAnnotationValue = "";

        String defaultValue = getValue(method.getDefaultValue(), context);
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
    private String getFieldValue(Field field, ParseContext context) {
        if (isDisplayField(field)) {
            Object value = getStaticValue(field);
            if (value != EMPTY && !isField(value, context)) {
                String fieldValue = getValue(value, context);
                if (!"".equals(fieldValue)) {
                    return " = " + fieldValue;
                }
            }
        }

        return "";
    }

    /**
     * Checks if value displayed is necessary
     *
     * @param field any field
     * @return true if value of field should be displayed
     */
    private boolean isDisplayField(Field field) {
        return configurationManager.isDisplayFieldValue() &&
                Modifier.isStatic(field.getModifiers()) &&
                !isEnumConstant(field);
    }

    /**
     * Obtains static accessible field value
     *
     * @param field any field
     * @return static field value
     */
    private Object getStaticValue(Field field) {
        if (field.isAccessible()) {
            try {
                return field.get(null);
            } catch (ReflectiveOperationException exception) {
                return EMPTY;
            }
        }

        return EMPTY;
    }

    /**
     * Checks field for accessory to current parsed class
     *
     * @param object any field
     * @return true if field accessory to current parsed class
     */
    private boolean isField(Object object, ParseContext context) {
        return object instanceof Field && context.isCurrentParsedClass(((Field) object).getDeclaringClass());
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