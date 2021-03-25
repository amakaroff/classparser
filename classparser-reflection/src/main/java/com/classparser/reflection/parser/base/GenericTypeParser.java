package com.classparser.reflection.parser.base;

import com.classparser.reflection.ContentJoiner;
import com.classparser.reflection.ParseContext;
import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.exception.ReflectionParserException;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class provides functionality by obtaining meta information about types and generics
 *
 * @author Aleksey Makarov
 * @author Vadim Kiselev
 * @since 1.0.0
 */
public class GenericTypeParser {

    private final ClassNameParser classNameParser;

    private final ConfigurationManager configurationManager;

    private final AnnotationParser annotationParser;

    public GenericTypeParser(ConfigurationManager configurationManager) {
        this(new AnnotationParser(configurationManager), configurationManager);
    }

    public GenericTypeParser(AnnotationParser annotationParser, ConfigurationManager configurationManager) {
        this.classNameParser = new ClassNameParser(configurationManager);
        this.annotationParser = annotationParser;
        this.configurationManager = configurationManager;
    }

    /**
     * Parse meta information about generic declarations and obtain it to {@link String}
     * For example: class, method or constructor
     * <code>
     * public {@literal <}T{@literal >} void method() {...}
     * </code>
     *
     * @param genericDeclaration any generic declaration object
     * @param context            context of parsing class process
     * @return string line with meta information about generics
     */
    public String parseGenerics(GenericDeclaration genericDeclaration, boolean isInsideClass, ParseContext context) {
        if (configurationManager.isDisplayGenericSignatures()) {
            List<String> generics = new ArrayList<>();
            TypeVariable<?>[] typeParameters = genericDeclaration.getTypeParameters();

            for (TypeVariable<?> parameter : typeParameters) {
                String annotations = annotationParser.parseAnnotationsAsInline(parameter, isInsideClass, context);
                String boundTypes = String.join(" & ", parseBounds(parameter, isInsideClass, context));
                String bounds = !boundTypes.isEmpty() ? "extends " + boundTypes : "";

                generics.add(ContentJoiner.joinNotEmptyContentBySpace(annotations, parameter.getName(), bounds));
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
     * @param type    any type
     * @param context context of parsing class process
     * @return string line with meta information about type
     */
    public String parseType(Type type, ParseContext context) {
        return parseType(type, true, null, context);
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
     * @param context       context of parsing class process
     * @return string line with meta information about type
     */
    public String parseType(Type type, AnnotatedType annotatedType, ParseContext context) {
        return parseType(type, true, annotatedType, context);
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
     * @param isInsideClass is type contained inside class block
     * @param annotatedType annotation on this type
     * @param context       context of parsing class process
     * @return string line with meta information about type
     */
    public String parseType(Type type, boolean isInsideClass, AnnotatedType annotatedType, ParseContext context) {
        if (type instanceof Class) {
            Class<?> clazz = (Class<?>) type;

            return parseClassType(clazz, isInsideClass, annotatedType, context);
        } else if (type instanceof TypeVariable) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) type;

            return parseTypeVariable(typeVariable, isInsideClass, annotatedType, context);
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            return parseParametrizedType(parameterizedType, isInsideClass, annotatedType, context);
        } else if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            AnnotatedArrayType annotatedArrayType = (AnnotatedArrayType) annotatedType;

            return parseGenericArrayType(genericArrayType, isInsideClass, annotatedArrayType, context);
        }

        throw new ReflectionParserException("Unexpected code branch");
    }

    @SuppressWarnings("ConstantConditions")
    private String parseClassType(Class<?> clazz, boolean isInsideClass, AnnotatedType annotatedType, ParseContext context) {
        String boundType = "";
        String parsedAnnotations = "";

        if (clazz.isArray()) {
            AnnotatedArrayType annotatedArrayType = (AnnotatedArrayType) annotatedType;
            boundType = parseType(clazz.getComponentType(), annotatedArrayType, context);
            AnnotatedType annotatedForArrayType = getAnnotatedTypeForArray(clazz, annotatedArrayType);
            boundType += annotationParser.parseAnnotationsAsInline(annotatedForArrayType, isInsideClass, context) + "[]";
        } else {
            if (configurationManager.isDisplayAnnotationOnTypes() && annotatedType != null) {
                AnnotatedType resolvedAnnotatedType = getAnnotatedArrayType(annotatedType);
                parsedAnnotations = annotationParser.parseAnnotationsAsInline(resolvedAnnotatedType, isInsideClass, context);
            }

            if (classNameParser.isNeedNameForInnerClass(clazz, isInsideClass, context)) {
                // Have problems because of https://bugs.openjdk.java.net/browse/JDK-8146861
                // Fixed in Java 9
                AnnotatedType annotatedOwnType = null; // annotatedType.getAnnotatedOwnerType();
                String typeName = parseType(clazz.getDeclaringClass(), isInsideClass, annotatedOwnType, context);
                boundType = !typeName.isEmpty() ? typeName + "." + appendSpaceIfNotEmpty(parsedAnnotations) : "";
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

        return ContentJoiner.joinNotEmptyContentBySpace(parsedAnnotations, boundType);
    }

    private String parseTypeVariable(TypeVariable<?> typeVariable,
                                     boolean isInsideClass,
                                     AnnotatedType annotatedType,
                                     ParseContext context) {
        String parsedAnnotations = "";
        if (configurationManager.isDisplayAnnotationOnTypes() && annotatedType != null) {
            AnnotatedType annotatedArrayType = getAnnotatedArrayType(annotatedType);
            parsedAnnotations = annotationParser.parseAnnotationsAsInline(annotatedArrayType, isInsideClass, context);
        }
        return ContentJoiner.joinNotEmptyContentBySpace(parsedAnnotations, typeVariable.getName());
    }

    @SuppressWarnings("ConstantConditions")
    private String parseParametrizedType(ParameterizedType parameterizedType,
                                         boolean isInsideClass,
                                         AnnotatedType annotatedType,
                                         ParseContext context) {
        String boundType = "";
        String parsedAnnotations = "";

        if (configurationManager.isDisplayAnnotationOnTypes() && annotatedType != null) {
            // If type is inner nested class then "use type" annotations for parametrized type is invisible
            // https://stackoverflow.com/questions/39952812/why-annotation-on-generic-type-argument-is-not-visible-for-nested-type
            AnnotatedType resolvedAnnotatedType = getAnnotatedArrayType(annotatedType);
            parsedAnnotations = annotationParser.parseAnnotationsAsInline(resolvedAnnotatedType, isInsideClass, context);
        }

        if (classNameParser.isNeedNameForInnerClass((Class<?>) parameterizedType.getRawType(), isInsideClass, context)) {
            // Have problems because of https://bugs.openjdk.java.net/browse/JDK-8146861
            // Fixed in Java 9
            AnnotatedParameterizedType annotatedOwnerParametrizedType = null; // annotatedType.getAnnotatedOwnerType();
            String correctAnnotations = appendSpaceIfNotEmpty(parsedAnnotations);
            Type ownerType = parameterizedType.getOwnerType();
            boundType = parseType(ownerType, annotatedOwnerParametrizedType, context) + "." + correctAnnotations;
            parsedAnnotations = "";
        }

        String genericArguments = "";
        Class<?> clazz = (Class<?>) parameterizedType.getRawType();
        String parametrizedRawTypeName = classNameParser.parseTypeName(clazz, context);

        annotatedType = getAnnotatedArrayType(annotatedType);
        AnnotatedParameterizedType annotatedParameterizedType = (AnnotatedParameterizedType) annotatedType;

        List<String> innerGenericTypes = parseGenericArguments(parameterizedType,
                isInsideClass,
                annotatedParameterizedType,
                context);

        if (!innerGenericTypes.isEmpty()) {
            genericArguments = "<" + String.join(", ", innerGenericTypes) + ">";
        }
        boundType += parametrizedRawTypeName + genericArguments;

        return ContentJoiner.joinNotEmptyContentBySpace(parsedAnnotations, boundType);
    }

    private String parseGenericArrayType(GenericArrayType genericArrayType,
                                         boolean isInsideClass,
                                         AnnotatedArrayType annotatedArrayType,
                                         ParseContext context) {
        String boundType;

        boundType = parseType(genericArrayType.getGenericComponentType(), annotatedArrayType, context);

        AnnotatedType annotatedTypeForArray = getAnnotatedTypeForArray(genericArrayType, annotatedArrayType);
        boundType += annotationParser.parseAnnotationsAsInline(annotatedTypeForArray, isInsideClass, context) + "[]";

        return boundType;
    }

    /**
     * Collecting list bound from type variable
     *
     * @param parameter type variable
     * @param context   context of parsing class process
     * @return resolved list of string bound with meta information
     */
    private List<String> parseBounds(TypeVariable<?> parameter, boolean isInsideClass, ParseContext context) {
        List<String> bounds = new ArrayList<>();
        Type[] typeBounds = parameter.getBounds();
        AnnotatedType[] annotatedBounds = parameter.getAnnotatedBounds();

        for (int index = 0; index < typeBounds.length; index++) {
            Type typeBound = typeBounds[index];
            AnnotatedType annotatedBound = annotatedBounds[index];
            if (configurationManager.isDisplayDefaultInheritance() || typeBound != Object.class) {
                bounds.add(parseType(typeBound, isInsideClass, annotatedBound, context));
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
     * It's behaviour because of case:
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
        if (configurationManager.isDisplayAnnotationOnTypes()) {
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
     * @param context                    context of parsing class process
     * @return list of resolved parameterized types
     */
    private List<String> parseGenericArguments(ParameterizedType parameterizedType,
                                               boolean isInsideClass,
                                               AnnotatedParameterizedType annotatedParameterizedType,
                                               ParseContext context) {
        List<String> genericArguments = new ArrayList<>();

        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        AnnotatedType[] annotatedActualTypeArguments = ifNull(annotatedParameterizedType);

        for (int index = 0; index < actualTypeArguments.length; index++) {
            Type actualTypeArgument = actualTypeArguments[index];
            if (actualTypeArgument instanceof WildcardType) {
                WildcardType wildcardType = (WildcardType) actualTypeArgument;
                AnnotatedType annotatedType = ifEmpty(annotatedActualTypeArguments, index);

                String annotations = "";
                AnnotatedWildcardType annotatedWildcardType = (AnnotatedWildcardType) annotatedType;
                if (annotatedWildcardType != null) {
                    annotations = annotationParser.parseAnnotationsAsInline(annotatedWildcardType, isInsideClass, context);
                }
                String wildcard = ContentJoiner.joinNotEmptyContentBySpace(annotations, "?");

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
     * @param context        context of parsing class process
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
     * Obtains package name
     *
     * @param clazz any class
     * @return name of package for class or empty string of class have not package
     */
    private String getPackageName(Class<?> clazz) {
        return clazz.getPackage() != null ? clazz.getPackage().getName() : "";
    }

    /**
     * Appends space after annotation if it's not empty.
     *
     * @param annotations annotation string
     * @return annotation with space or empty line
     */
    private String appendSpaceIfNotEmpty(String annotations) {
        return !annotations.isEmpty() ? annotations + " " : "";
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
     * @param annotatedTypes annotations types array
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