package com.classparser.reflection.parser.base;

import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.configuration.ReflectionParserManager;
import com.classparser.reflection.exception.ReflectionParserException;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class provides functionality by collecting annotation meta information
 * for any annotated class elements
 *
 * @author Aleksey Makarov
 * @author Vadim Kiselev
 * @since 1.0.0
 */
public class AnnotationParser {

    private static final String DEFAULT_ANNOTATION_METHOD = "value";

    private final IndentParser indentParser;

    private final ConfigurationManager configurationManager;

    private final ClassNameParser classNameParser;

    private final ModifierParser modifierParser;

    private ValueParser valueParser;

    public AnnotationParser(IndentParser indentParser, ReflectionParserManager manager,
                            ModifierParser modifierParser, ClassNameParser classNameParser) {
        this.indentParser = indentParser;
        this.configurationManager = manager.getConfigurationManager();
        this.modifierParser = modifierParser;
        this.classNameParser = classNameParser;
    }

    /**
     * Parses annotation meta information and collecting it to inline string
     * This method used for parameter or use type annotations
     * <code>
     * {
     * {@literal @}Annotation {@literal @}OtherAnnotation
     * }
     * </code>
     *
     * @param annotatedElement any annotated element
     * @return string line with parsed annotation meta information
     */
    public String parseAnnotationsAsInline(AnnotatedElement annotatedElement) {
        List<String> annotations = new ArrayList<>();

        if (annotatedElement != null) {
            String indent = indentParser.getIndent(annotatedElement);

            for (Annotation annotation : unrollAnnotations(annotatedElement.getDeclaredAnnotations())) {
                annotations.add(parseAnnotation(annotation, false));
            }

            return indent + String.join(" ", annotations);
        } else {
            throw new NullPointerException("Annotated element can't be a null!");
        }
    }

    /**
     * Parses annotation meta information and collecting it to inline string
     * <code>
     * {
     * {@literal @}Annotation
     * {@literal @}OtherAnnotation
     * }
     * </code>
     *
     * @param annotatedElement any annotated element
     * @return string line with parsed annotation meta information
     */
    public String parseAnnotationsAsBlock(AnnotatedElement annotatedElement) {
        return parseAnnotationsAsBlock(annotatedElement, false);
    }

    /**
     * Parses annotation meta information and collecting it to inline string
     * Uses in case if annotation located above any class
     *
     * @param annotatedElement any annotated element
     * @return string line with parsed annotation meta information
     */
    public String parseAnnotationsAsBlockAboveClass(AnnotatedElement annotatedElement) {
        return parseAnnotationsAsBlock(annotatedElement, true);
    }

    /**
     * Parse annotation meta information and collecting it to inline string
     * <code>
     * {
     * {@literal @}Annotation
     * {@literal @}OtherAnnotation
     * }
     * </code>
     *
     * @param annotatedElement any annotated element
     * @param isAboveClass     true if annotation located above class
     * @return string line with parsed annotation meta information
     */
    private String parseAnnotationsAsBlock(AnnotatedElement annotatedElement, boolean isAboveClass) {
        StringBuilder annotations = new StringBuilder();

        if (annotatedElement != null) {
            String indent = indentParser.getIndent(annotatedElement);
            String lineSeparator = configurationManager.getLineSeparator();

            for (Annotation annotation : unrollAnnotations(annotatedElement.getDeclaredAnnotations())) {
                annotations.append(indent).append(parseAnnotation(annotation, isAboveClass)).append(lineSeparator);
            }

            if (annotatedElement instanceof Method) {
                Method method = (Method) annotatedElement;
                if (isOverriddenMethod(method)) {
                    annotations.append(indent).append("@Override").append(lineSeparator);
                }
            }
        } else {
            throw new NullPointerException("Annotated element can't be a null!");
        }

        return annotations.toString();
    }

    /**
     * Parse annotation meta information and collecting it to {@link String}
     *
     * @param annotation any annotation
     * @return string meta information about annotation
     */
    public String parseAnnotation(Annotation annotation) {
        return parseAnnotation(annotation, false);
    }

    /**
     * Parse annotation meta information and collecting it to {@link String}
     *
     * @param annotation   any annotation
     * @param isAboveClass flag uses for mark if this annotation located above class
     * @return string meta information about annotation
     */
    private String parseAnnotation(Annotation annotation, boolean isAboveClass) {
        if (annotation != null) {
            String annotationName = classNameParser.parseAnnotationName(annotation.annotationType(), isAboveClass);
            String annotationArguments = parseAnnotationArguments(annotation);
            return '@' + annotationName + annotationArguments;
        }

        return "";
    }

    /**
     * Parse and retrieve parameters value from annotation to {@link String}
     *
     * @param annotation any annotation
     * @return string line with annotation parameters information
     */
    private String parseAnnotationArguments(Annotation annotation) {
        List<String> arguments = new ArrayList<>();

        Set<Map.Entry<String, Object>> annotationParameters = getAnnotationMemberTypes(annotation).entrySet();
        for (Map.Entry<String, Object> entry : annotationParameters) {
            if (DEFAULT_ANNOTATION_METHOD.equals(entry.getKey()) && annotationParameters.size() == 1) {
                arguments.add(String.valueOf(entry.getValue()));
            } else {
                arguments.add(entry.getKey() + " = " + entry.getValue());
            }
        }

        if (!arguments.isEmpty()) {
            return '(' + String.join(", ", arguments) + ')';
        }

        return "";
    }

    /**
     * Collecting information about parameters for annotation object
     *
     * @param annotation any annotation
     * @return map with annotation parameter values
     */
    private Map<String, Object> getAnnotationMemberTypes(Annotation annotation) {
        Map<String, Object> map = new HashMap<>();

        Class<? extends Annotation> annotationTypeClass = annotation.annotationType();
        if (annotationTypeClass != null) {
            Method[] methods = annotationTypeClass.getDeclaredMethods();

            for (Method method : methods) {
                Object value = invokeMethod(method, annotation);
                if (value != null && !isDefaultValue(value, method.getDefaultValue())) {
                    map.put(method.getName(), getValueParser().getValue(value));
                }
            }
        }

        return map;
    }

    /**
     * Unrolls annotation if some annotation is repeatable
     *
     * @param declaredAnnotations any annotation array
     * @return correctly annotation array
     */
    private Annotation[] unrollAnnotations(Annotation[] declaredAnnotations) {
        List<Annotation> annotations = new ArrayList<>();

        for (Annotation declaredAnnotation : declaredAnnotations) {
            if (isRepeatableAnnotation(declaredAnnotation)) {
                annotations.addAll(retrieveRepeatableAnnotations(declaredAnnotation));
            } else {
                annotations.add(declaredAnnotation);
            }
        }

        return annotations.toArray(new Annotation[0]);
    }

    /**
     * Checking this annotation is an {@link Repeatable} annotation
     *
     * @param annotation any annotation
     * @return true if this annotation is an {@link Repeatable} annotation
     */
    private boolean isRepeatableAnnotation(Annotation annotation) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        Method valueMethod = retrieveValueMethodFromAnnotation(annotationType);
        if (valueMethod != null) {
            Class<?> returnType = valueMethod.getReturnType();

            if (returnType.isArray()) {
                Class<?> componentType = returnType.getComponentType();
                Repeatable repeatable = componentType.getAnnotation(Repeatable.class);
                return repeatable != null && annotationType.equals(repeatable.value());
            }
        }
        return false;
    }

    /**
     * Retrieve repeatable annotations from annotation container
     *
     * @param annotation annotation container
     * @return list of retrieve annotations
     */
    private List<Annotation> retrieveRepeatableAnnotations(Annotation annotation) {
        List<Annotation> annotations = new ArrayList<>();

        Class<? extends Annotation> annotationType = annotation.annotationType();
        Method valueMethod = retrieveValueMethodFromAnnotation(annotationType);
        if (valueMethod != null) {
            Annotation[] retrievedAnnotations = (Annotation[]) invokeMethod(valueMethod, annotation);
            if (retrievedAnnotations != null) {
                annotations.addAll(Arrays.asList(retrievedAnnotations));
            }
        }

        return annotations;
    }

    /**
     * Checks annotation method on access and call it
     *
     * @param method   method in annotation
     * @param instance annotation instance
     * @return method return value
     */
    private Object invokeMethod(Method method, Object instance) {
        try {
            if (method.isAccessible()) {
                return method.invoke(instance);
            }
        } catch (ReflectiveOperationException exception) {
            System.err.println("Can't access to method " + method + " from AnnotationParser!");
        }

        return null;
    }

    /**
     * Obtaining method with name "value" from annotation
     *
     * @param annotationType any class of annotation
     * @return method or null if method with that name is not found
     */
    private Method retrieveValueMethodFromAnnotation(Class<? extends Annotation> annotationType) {
        for (Method method : annotationType.getDeclaredMethods()) {
            if (DEFAULT_ANNOTATION_METHOD.equals(method.getName())) {
                return method;
            }
        }

        return null;
    }

    /**
     * Checking value is default value
     *
     * @param value        value
     * @param defaultValue default value
     * @return true if value equal default value
     */
    private boolean isDefaultValue(Object value, Object defaultValue) {
        if (!configurationManager.isDisplayDefaultValueInAnnotation()) {
            if (!value.getClass().isArray()) {
                return value.equals(defaultValue);
            } else {
                Object[] arrayValue = getValueParser().toObjectArray(value);
                Object[] arrayDefaultValue = getValueParser().toObjectArray(defaultValue);
                return Arrays.deepEquals(arrayValue, arrayDefaultValue);
            }
        }

        return false;
    }

    /**
     * Get value parser or throw exception if it not initialize
     *
     * @return instance of value parser
     */
    private ValueParser getValueParser() {
        if (valueParser != null) {
            return valueParser;
        }

        throw new ReflectionParserException("<Value Parser> for <Annotation Parser> is not initialized!");
    }

    /**
     * Setter for value parser
     *
     * @param valueParser value parser
     */
    public void setValueParser(ValueParser valueParser) {
        this.valueParser = valueParser;
    }

    /**
     * Checks method if it is overridden
     *
     * @param method any method
     * @return true if method is overridden
     */
    private boolean isOverriddenMethod(Method method) {
        if (isCanOverridden(method)) {
            Class<?> declaringClass = method.getDeclaringClass();
            return isSuperClassMethodOverridden(declaringClass, method) ||
                    isInterfaceMethodOverridden(declaringClass, method);
        }

        return false;
    }

    /**
     * Checks if method is overridden for any superclass
     *
     * @param declaringClass declaring class for method
     * @param method         any method
     * @return true if method is overridden for any superclass
     */
    private boolean isSuperClassMethodOverridden(Class<?> declaringClass, Method method) {
        Class<?> superclass = declaringClass.getSuperclass();

        if (superclass != null) {
            for (Method superClassMethod : superclass.getDeclaredMethods()) {
                if (isCanOverridden(superClassMethod) && isMethodOverriddenEquals(method, superClassMethod)) {
                    return true;
                }
            }

            return isSuperClassMethodOverridden(superclass, method);
        }

        return false;
    }

    /**
     * Checks if method is can be overridden
     *
     * @param method any method
     * @return true if method can be overridden
     */
    private boolean isCanOverridden(Method method) {
        return !Modifier.isPrivate(method.getModifiers()) || !Modifier.isStatic(method.getModifiers());
    }

    /**
     * Checks if method is overridden for any interface
     *
     * @param declaringClass declaring class or interface for method
     * @param method         any method
     * @return true if method is overridden for any interface
     */
    private boolean isInterfaceMethodOverridden(Class<?> declaringClass, Method method) {
        Class<?>[] interfaces = declaringClass.getInterfaces();

        if (interfaces != null) {
            for (Class<?> interfaceOfClass : interfaces) {
                for (Method interfaceMethod : interfaceOfClass.getDeclaredMethods()) {
                    if (isMethodOverriddenEquals(method, interfaceMethod)) {
                        return true;
                    }
                }

                if (isInterfaceMethodOverridden(interfaceOfClass, method)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if source method is an overridden for target method
     *
     * @param source source method
     * @param target target method
     * @return true if method source is an overridden for target method
     */
    private boolean isMethodOverriddenEquals(Method source, Method target) {
        int targetModifiers = target.getModifiers();
        if (source.getName().equals(target.getName()) && !Modifier.isPrivate(targetModifiers)) {
            Class<?>[] sourceMethodParametersTypes = source.getParameterTypes();
            Class<?>[] targetMethodParameterTypes = target.getParameterTypes();
            if (Arrays.equals(sourceMethodParametersTypes, targetMethodParameterTypes)) {
                if (modifierParser.isPackagePrivate(targetModifiers)) {
                    Class<?> sourceDeclaringClass = source.getDeclaringClass();
                    Class<?> targetDeclaringClass = target.getDeclaringClass();
                    return sourceDeclaringClass.getPackage() == targetDeclaringClass.getPackage();
                }

                return true;
            }
        }

        return false;
    }
}