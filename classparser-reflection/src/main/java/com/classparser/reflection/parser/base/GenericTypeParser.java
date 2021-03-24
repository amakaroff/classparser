package com.classparser.reflection.parser.base;

import com.classparser.reflection.ParseContext;
import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.exception.ReflectionParserException;

import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class provides functionality by obtaining meta information about types and generics
 *
 * @author Aleksey Makarov
 * @author Valim Kiselev
 * @since 1.0.0
 */
public class GenericTypeParser {

    private final ClassNameParser classNameParser;

    private final ConfigurationManager configurationManager;

    private AnnotationParser annotationParser;

    public GenericTypeParser(ClassNameParser classNameParser, ConfigurationManager configurationManager) {
        this.classNameParser = classNameParser;
        this.configurationManager = configurationManager;
    }

    /**
     * Inner getter for annotation parser
     *
     * @return annotation parser
     * @throws ReflectionParserException if parser is not initialize
     */
    private AnnotationParser getAnnotationParser() {
        if (annotationParser != null) {
            return annotationParser;
        }

        throw new ReflectionParserException("<Annotation Parser> for <Generic Type Parser> is not initialized!");
    }

    /**
     * Setter for annotation parser
     *
     * @param annotationParser annotation parser
     */
    public void setAnnotationParser(AnnotationParser annotationParser) {
        this.annotationParser = annotationParser;
    }

    /**
     * Parse meta information about generic declarations and obtain it to {@link String}
     * For example: class, method or constructor
     * <code>
     * public {@literal <}T{@literal >} void method() {...}
     * </code>
     *
     * @param genericDeclaration any generic declaration object
     * @return string line with meta information about generics
     */
    public String parseGenerics(GenericDeclaration genericDeclaration, ParseContext context) {
        AnnotationParser annotationParser = getAnnotationParser();

        if (configurationManager.isDisplayGenericSignatures()) {
            List<String> generics = new ArrayList<>();
            TypeVariable<?>[] typeParameters = genericDeclaration.getTypeParameters();

            for (TypeVariable<?> parameter : typeParameters) {
                String annotations = annotationParser.parseAnnotationsAsInline(parameter, context);
                String boundTypes = String.join(" & ", parseBounds(parameter, context));
                String bounds = !boundTypes.isEmpty() ? " extends " + boundTypes : "";

                generics.add(getCorrectAnnotations(annotations) + parameter.getName() + bounds);
            }

            if (typeParameters.length > 0) {
                return "<" + String.join(", ", generics) + ">";
            }
        }

        return "";
    }

    /**
     * Resolving meta information about type and collecting it to string
     * For example:
     * <code>
     * List{@literal <}String{@literal >}
     * MyClass{@literal <}? extends Number{@literal >}
     * MyType{@literal <}{@literal @}Annotation MyOtherType{@literal >}
     * </code>
     *
     * @param type          any type
     * @param annotatedType annotation on this type
     * @return string line with meta information about type
     */
    public String parseType(Type type, AnnotatedType annotatedType, ParseContext context) {
        AnnotationParser annotationParser = getAnnotationParser();
        String annotations = "";
        String boundType = "";

        if (configurationManager.isDisplayAnnotationOnTypes() && annotatedType != null && !isArray(type)) {
            // If type is inner nested class then "use type" annotations for parametrized type is invisible
            // https://stackoverflow.com/questions/39952812/why-annotation-on-generic-type-argument-is-not-visible-for-nested-type
            annotations = annotationParser.parseAnnotationsAsInline(getAnnotatedArrayType(annotatedType), context);
        }

        if (type instanceof Class) {
            Class<?> clazz = (Class<?>) type;
            return parseClassType(clazz, annotatedType, annotations, context);
        } else if (type instanceof TypeVariable) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) type;
            return parseTypeVariable(typeVariable);
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return parseParametrizedType(parameterizedType, annotatedType, annotations, context);
        } else if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            AnnotatedArrayType annotatedArrayType = (AnnotatedArrayType) annotatedType;

            return parseGenericArrayType(genericArrayType, annotatedArrayType, annotations, context);
        }

        return getCorrectAnnotations(annotations) + boundType;
    }

    private String parseClassType(Class<?> clazz, AnnotatedType annotatedType, String parsedAnnotations, ParseContext context) {
        AnnotationParser annotationParser = getAnnotationParser();
        String boundType = "";

        if (clazz.isArray()) {
            AnnotatedArrayType annotatedArrayType = (AnnotatedArrayType) annotatedType;
            boundType = parseType(clazz.getComponentType(), annotatedArrayType, context);
            AnnotatedType annotatedForArrayType = getAnnotatedTypeForArray(clazz, annotatedArrayType);
            boundType += annotationParser.parseAnnotationsAsInline(annotatedForArrayType, context) + "[]";
        } else {
            if (isNeedNameForInnerClass(clazz, context)) {
                String typeName = parseType(clazz.getDeclaringClass(), null, context);
                boundType = !typeName.isEmpty() ? typeName + "." + getCorrectAnnotations(parsedAnnotations) : "";
                parsedAnnotations = "";
            }

            boundType += classNameParser.parseTypeName(clazz, context);
            if (isAnnotationOnClassWithFullName(clazz, boundType, parsedAnnotations)) {
                String packageName = getPackageName(clazz);
                String simpleName = classNameParser.getSimpleName(clazz);
                boundType = packageName + "." + parsedAnnotations + " " + simpleName;
                parsedAnnotations = "";
            }
        }

        return getCorrectAnnotations(parsedAnnotations) + boundType;
    }

    private String parseTypeVariable(TypeVariable<?> typeVariable) {
        return typeVariable.getName();
    }

    @SuppressWarnings("ConstantConditions")
    private String parseParametrizedType(ParameterizedType parameterizedType,
                                         AnnotatedType annotatedType,
                                         String parsedAnnotations,
                                         ParseContext context) {
        String boundType = "";

        if (isNeedNameForInnerClass((Class<?>) parameterizedType.getRawType(), context)) {
            // Have problems because of https://bugs.openjdk.java.net/browse/JDK-8146861
            // Fixed in Java 9
            AnnotatedParameterizedType annotatedOwnerParametrizedType = null;
            String correctAnnotations = getCorrectAnnotations(parsedAnnotations);
            Type ownerType = parameterizedType.getOwnerType();
            boundType = parseType(ownerType, annotatedOwnerParametrizedType, context) + "." + correctAnnotations;
            parsedAnnotations = "";
        }

        String genericArguments = "";
        Class<?> clazz = (Class<?>) parameterizedType.getRawType();
        String parametrizedRawTypeName = classNameParser.parseTypeName(clazz, context);
        annotatedType = getAnnotatedArrayType(annotatedType);
        AnnotatedParameterizedType annotatedParameterizedType = (AnnotatedParameterizedType) annotatedType;

        List<String> innerGenericTypes = parseGenericArguments(parameterizedType, annotatedParameterizedType, context);
        if (!innerGenericTypes.isEmpty()) {
            genericArguments = "<" + String.join(", ", innerGenericTypes) + ">";
        }
        boundType += parametrizedRawTypeName + genericArguments;

        return getCorrectAnnotations(parsedAnnotations) + boundType;
    }

    private String parseGenericArrayType(GenericArrayType genericArrayType,
                                         AnnotatedArrayType annotatedArrayType,
                                         String parsedAnnotations,
                                         ParseContext context) {
        AnnotationParser annotationParser = getAnnotationParser();
        String boundType;

        boundType = parseType(genericArrayType.getGenericComponentType(), annotatedArrayType, context);

        AnnotatedType annotatedTypeForArray = getAnnotatedTypeForArray(genericArrayType, annotatedArrayType);
        boundType += annotationParser.parseAnnotationsAsInline(annotatedTypeForArray, context) + "[]";

        return getCorrectAnnotations(parsedAnnotations) + boundType;
    }

    /**
     * Resolving meta information about type and collecting it to string
     * This method used if absent information about annotations on type
     * For example:
     * <code>
     * List{@literal <}String{@literal >}
     * MyClass{@literal <}? extends Number{@literal >}
     * </code>
     *
     * @param type any type
     * @return string line with meta information about type
     */
    public String parseType(Type type, ParseContext context) {
        return parseType(type, null, context);
    }

    /**
     * Collecting list bound from type variable
     *
     * @param parameter type variable
     * @return resolved list of string bound with meta information
     */
    private List<String> parseBounds(TypeVariable<?> parameter, ParseContext context) {
        AnnotationParser annotationParser = getAnnotationParser();
        List<String> bounds = new ArrayList<>();
        Type[] typeBounds = parameter.getBounds();
        AnnotatedType[] annotatedBounds = parameter.getAnnotatedBounds();

        for (int index = 0; index < typeBounds.length; index++) {
            String annotations = annotationParser.parseAnnotationsAsInline(annotatedBounds[index], context);
            Type typeBound = typeBounds[index];
            if (configurationManager.isDisplayDefaultInheritance() || typeBound != Object.class) {
                String boundType = parseType(typeBound, context);
                if (!boundType.isEmpty()) {
                    bounds.add(getCorrectAnnotations(annotations) + boundType);
                }
            }
        }
        return bounds;
    }

    /**
     * Resolve annotated types for array
     *
     * @param array         generic array
     * @param annotatedType annotated types for array
     * @return resolved annotated type
     */
    private AnnotatedType getAnnotatedTypeForArray(Class<?> array, AnnotatedArrayType annotatedType) {
        if (configurationManager.isDisplayAnnotationOnTypes() && annotatedType != null) {
            int dimensionIndex = 0;
            while (array.getComponentType().isArray()) {
                array = array.getComponentType();
                dimensionIndex++;
            }

            return getAnnotatedArrayType(annotatedType, dimensionIndex);
        }

        return null;
    }

    /**
     * Resolve annotated types for generic array
     * Its behaviour because of case:
     * <code>
     * {@literal @}Annotation int{@literal @}Annotation[]{@literal @}Annotation3[] a;
     * </code>
     *
     * @param array         generic array
     * @param annotatedType annotated types for genetic array
     * @return resolved annotated type
     */
    private AnnotatedType getAnnotatedTypeForArray(GenericArrayType array, AnnotatedArrayType annotatedType) {
        if (configurationManager.isDisplayAnnotationOnTypes() && annotatedType != null) {
            int dimensionIndex = 0;
            while (array.getGenericComponentType() instanceof GenericArrayType) {
                array = (GenericArrayType) array.getGenericComponentType();
                dimensionIndex++;
            }

            return getAnnotatedArrayType(annotatedType, dimensionIndex);
        }

        return null;
    }

    /**
     * Retrieve annotated type from array type
     *
     * @param annotatedType any annotated array type
     * @param countIncludes index of position recursive includes
     * @return annotated type
     */
    private AnnotatedType getAnnotatedArrayType(AnnotatedArrayType annotatedType, int countIncludes) {
        for (int index = 0; index < countIncludes; index++) {
            annotatedType = (AnnotatedArrayType) annotatedType.getAnnotatedGenericComponentType();
        }

        return annotatedType;
    }

    /**
     * Check on array annotated type and obtaining type of generic array
     *
     * @param annotatedType any annotated type
     * @return annotated array type
     */
    private AnnotatedType getAnnotatedArrayType(AnnotatedType annotatedType) {
        if (!configurationManager.isDisplayAnnotationOnTypes()) {
            if (annotatedType instanceof AnnotatedArrayType) {
                AnnotatedArrayType annotatedArrayType = (AnnotatedArrayType) annotatedType;
                while (annotatedArrayType.getAnnotatedGenericComponentType() instanceof AnnotatedArrayType) {
                    AnnotatedType annotatedGenericComponentType = annotatedArrayType.getAnnotatedGenericComponentType();
                    annotatedArrayType = (AnnotatedArrayType) annotatedGenericComponentType;
                }
                annotatedType = annotatedArrayType.getAnnotatedGenericComponentType();
            }

            return annotatedType;
        }

        return null;
    }

    /**
     * Obtain list with resolved strings meta information for parameterized type
     *
     * @param parameterizedType          any parameterized type
     * @param annotatedParameterizedType annotated parameterized type
     * @return list of resolved parameterized types
     */
    private List<String> parseGenericArguments(ParameterizedType parameterizedType,
                                               AnnotatedParameterizedType annotatedParameterizedType,
                                               ParseContext context) {
        AnnotationParser annotationParser = getAnnotationParser();
        List<String> genericArguments = new ArrayList<>();

        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        AnnotatedType[] annotatedActualTypeArguments = ifNull(annotatedParameterizedType);

        for (int index = 0; index < actualTypeArguments.length; index++) {
            Type actualTypeArgument = actualTypeArguments[index];
            if (actualTypeArgument instanceof WildcardType) {
                WildcardType wildcardType = (WildcardType) actualTypeArgument;
                AnnotatedType annotatedType = ifEmpty(annotatedActualTypeArguments, index);

                AnnotatedWildcardType annotatedWildcardType = (AnnotatedWildcardType) annotatedType;
                String annotations = annotationParser.parseAnnotationsAsInline(annotatedWildcardType, context);
                String wildcard = getCorrectAnnotations(annotations) + "?";

                AnnotatedType[] upper = ifNullUpper(annotatedWildcardType);
                AnnotatedType[] lower = ifNullLower(annotatedWildcardType);

                wildcard += parseWildCardsBound(wildcardType.getUpperBounds(), "extends", upper, context);
                wildcard += parseWildCardsBound(wildcardType.getLowerBounds(), "super", lower, context);

                genericArguments.add(wildcard);
            } else {
                AnnotatedType annotatedType = ifEmpty(annotatedActualTypeArguments, index);
                genericArguments.add(parseType(actualTypeArguments[index], annotatedType, context));
            }
        }

        return genericArguments;
    }

    /**
     * Obtain meta information about wild cards bounds
     *
     * @param types          wild card bounds
     * @param boundCase      type of bound
     * @param annotatedTypes annotation for bounds
     * @return string with meta information about wild card bounds
     */
    private String parseWildCardsBound(Type[] types,
                                       String boundCase,
                                       AnnotatedType[] annotatedTypes,
                                       ParseContext context) {
        String wildcard = "";

        if (types.length != 0) {
            List<String> bounds = new ArrayList<>();
            for (int index = 0; index < types.length; index++) {
                Type type = types[index];
                if (configurationManager.isDisplayDefaultInheritance() || type != Object.class) {
                    bounds.add(parseType(type, ifEmpty(annotatedTypes, index), context));
                }
            }

            if (!bounds.isEmpty()) {
                wildcard += " " + boundCase + " " + String.join(" & ", bounds);
            }
        }

        return wildcard;
    }

    /**
     * Check is a needed name for inner class
     *
     * @param innerClass any class
     * @return true if name needed for inner class
     */
    private boolean isNeedNameForInnerClass(Class<?> innerClass, ParseContext context) {
        Class<?> parsedClass = context.getBaseParsedClass();
        return innerClass.isMemberClass()
                && (!getTopClass(innerClass).equals(getTopClass(parsedClass))
                || !isInVisibilityZone(innerClass, context));
    }

    /**
     * Checking is class exists in visibility zone for current parsed class
     * For example:
     *    Top class
     * 1 /         \ 2
     *  /\         /\
     * 3 4        5 6
     * <p>Class 3 in visibility zone for class 1 and not requires full name</p>
     * <p>
     * Class 4 and 5 in not visibility zone and if we used class 4 in 5 then we should
     * add enclosing class name
     * </p>
     *
     * @param innerClass any class
     * @return true if class in visibility zone for current parsed class
     */
    private boolean isInVisibilityZone(Class<?> innerClass, ParseContext context) {
        Class<?> currentClass = context.getCurrentParsedClass();
        while (currentClass != null) {
            List<Class<?>> innerClasses = Arrays.asList(currentClass.getDeclaredClasses());
            if (innerClasses.contains(innerClass)) {
                return true;
            }

            currentClass = currentClass.getDeclaringClass();
        }

        return false;
    }

    /**
     * Obtains package name
     *
     * @param clazz any class
     * @return name of package for class or empty string of class have not package
     */
    private String getPackageName(Class<?> clazz) {
        return clazz.getPackage() != null ? clazz.getPackage().getName() : "";
    }

    /**
     * Retrievals top declaring class
     *
     * @param innerClass any class
     * @return top declaring class
     */
    private Class<?> getTopClass(Class<?> innerClass) {
        return innerClass.getDeclaringClass() != null ? getTopClass(innerClass.getDeclaringClass()) : innerClass;
    }

    /**
     * Appends space after annotation if it's not empty.
     *
     * @param annotations annotation string
     * @return annotation with space or empty line
     */
    private String getCorrectAnnotations(String annotations) {
        return !annotations.isEmpty() ? annotations + " " : "";
    }

    /**
     * Check type is array
     *
     * @param type any type
     * @return true if type is array (generic array)
     */
    private boolean isArray(Type type) {
        return type instanceof Class && ((Class<?>) type).isArray() || type instanceof GenericArrayType;
    }

    /**
     * Checks is exists annotation type for class with full name
     * For example:
     * <code>
     * com.classparser.@AnnotationType MyClass
     * </code>
     *
     * @param clazz       any class
     * @param className   name of this class
     * @param annotations annotation for this class
     * @return true if case describe above is exists
     */
    private boolean isAnnotationOnClassWithFullName(Class<?> clazz, String className, String annotations) {
        return !clazz.isMemberClass() && className.contains(".") && !annotations.isEmpty();
    }

    /**
     * Checks and try get actual annotated type arguments for parameterized type
     *
     * @param type parameterized type
     * @return array of annotated type or null if parameterized type is null
     */
    private AnnotatedType[] ifNull(AnnotatedParameterizedType type) {
        return type != null ? type.getAnnotatedActualTypeArguments() : null;
    }

    /**
     * Checks and try get annotation for index
     *
     * @param annotatedTypes annotation types array
     * @param index          index for array
     * @return annotation by index or null if array is null or empty
     */
    private AnnotatedType ifEmpty(AnnotatedType[] annotatedTypes, int index) {
        return annotatedTypes != null && annotatedTypes.length > 0 ? annotatedTypes[index] : null;
    }

    /**
     * Checks and try get annotated upper bounds for wild cards
     *
     * @param type any type
     * @return array of annotated upper bounds for wild cards or null if type is null
     */
    private AnnotatedType[] ifNullUpper(AnnotatedWildcardType type) {
        return type != null ? type.getAnnotatedUpperBounds() : null;
    }

    /**
     * Checks and try get annotated lower bounds for wild cards
     *
     * @param type any type
     * @return array of annotated lower bounds for wild cards or null if type is null
     */
    private AnnotatedType[] ifNullLower(AnnotatedWildcardType type) {
        return type != null ? type.getAnnotatedLowerBounds() : null;
    }
}