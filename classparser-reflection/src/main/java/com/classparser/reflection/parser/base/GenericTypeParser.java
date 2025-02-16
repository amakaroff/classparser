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
    public String parseGenerics(GenericDeclaration genericDeclaration, ParseContext context) {
        if (configurationManager.isDisplayGenericSignatures()) {
            List<String> generics = new ArrayList<>();

            for (TypeVariable<?> parameter : genericDeclaration.getTypeParameters()) {
                String annotations = annotationParser.parseAnnotationsAsInline(parameter, context);
                String boundTypes = String.join(" & ", parseBounds(parameter, context));
                String bounds = !boundTypes.isEmpty() ? "extends " + boundTypes : "";

                generics.add(ContentJoiner.joinSpace(annotations, parameter.getName(), bounds));
            }

            return ContentJoiner.joinGenerics(generics);
        }

        return "";
    }

    /**
     * @see #parseType(Type, AnnotatedType, ParseContext)
     */
    public String parseType(Type type, ParseContext context) {
        return parseType(type, null, context);
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
        if (!configurationManager.isDisplayAnnotationOnTypes()) {
            annotatedType = null;
        }

        if (type instanceof Class<?> clazz) {
            if (clazz.isArray()) {
                AnnotatedArrayType annotatedArrayType = (AnnotatedArrayType) annotatedType;
                return parseArrayType(clazz, annotatedArrayType, context);
            } else {
                return parseClassType(clazz, annotatedType, context);
            }
        } else if (type instanceof TypeVariable<?> typeVariable) {
            return parseTypeVariable(typeVariable, annotatedType, context);
        } else if (type instanceof ParameterizedType parameterizedType) {
            return parseParametrizedType(parameterizedType, annotatedType, context);
        } else if (type instanceof GenericArrayType genericArrayType) {
            AnnotatedArrayType annotatedArrayType = (AnnotatedArrayType) annotatedType;
            return parseGenericArrayType(genericArrayType, annotatedArrayType, context);
        } else if (type instanceof WildcardType wildcardType) {
            AnnotatedWildcardType annotatedWildcardType = (AnnotatedWildcardType) resolveAnnotatedType(annotatedType);
            return parseWildCard(wildcardType, annotatedWildcardType, context);
        }

        throw new ReflectionParserException("Unexpected code branch");
    }

    private String parseClassType(Class<?> clazz, AnnotatedType annotatedType, ParseContext context) {
        return parseClassLink(clazz, clazz.getDeclaringClass(), annotatedType, context);
    }

    private String parseArrayType(Class<?> arrayType, AnnotatedArrayType annotatedType, ParseContext context) {
        return parseArrayLink(arrayType, annotatedType, context);
    }

    private String parseTypeVariable(TypeVariable<?> typeVariable, AnnotatedType annotatedType, ParseContext context) {
        AnnotatedType resolvedAnnotatedType = resolveAnnotatedType(annotatedType);
        String parsedAnnotations = annotationParser.parseAnnotationsAsInline(resolvedAnnotatedType, context);

        return ContentJoiner.joinSpace(parsedAnnotations, typeVariable.getName());
    }

    private String parseParametrizedType(ParameterizedType type, AnnotatedType annotatedType, ParseContext context) {
        Class<?> clazz = (Class<?>) type.getRawType();
        AnnotatedParameterizedType resolvedAnnotatedType = (AnnotatedParameterizedType) resolveAnnotatedType(annotatedType);

        String boundType = parseClassLink(clazz, type.getOwnerType(), annotatedType, context);
        List<String> innerGenericTypes = parseGenericArguments(type, resolvedAnnotatedType, context);

        return boundType + ContentJoiner.joinGenerics(innerGenericTypes);
    }

    private String parseGenericArrayType(GenericArrayType type, AnnotatedArrayType annotatedType, ParseContext context) {
        return parseArrayLink(type, annotatedType, context);
    }

    private String parseWildCard(WildcardType type, AnnotatedWildcardType annotatedType, ParseContext context) {
        AnnotatedType[] upper = ifNullUpper(annotatedType);
        AnnotatedType[] lower = ifNullLower(annotatedType);

        String annotations = annotationParser.parseAnnotationsAsInline(annotatedType, context);

        String upperBounds = parseWildCardsBound(type.getUpperBounds(), "extends", upper, context);
        String lowerBounds = parseWildCardsBound(type.getLowerBounds(), "super", lower, context);

        return ContentJoiner.joinSpace(annotations, "?", upperBounds, lowerBounds);
    }

    @SuppressWarnings("ConstantConditions")
    private String parseClassLink(Class<?> clazz, Type owner, AnnotatedType annotatedType, ParseContext context) {
        AnnotatedType resolvedAnnotatedType = resolveAnnotatedType(annotatedType);
        // If type is inner nested class then "use type" annotations for parametrized type is invisible
        // https://stackoverflow.com/questions/39952812/why-annotation-on-generic-type-argument-is-not-visible-for-nested-type
        String parsedAnnotations = annotationParser.parseAnnotationsAsInline(resolvedAnnotatedType, context);

        String classTypeName = classNameParser.parseTypeName(clazz, context);
        if (classNameParser.isNeedNameForInnerClass(clazz, context)) {
            AnnotatedType annotatedOwnerParametrizedType = null;
            if (annotatedType != null) {
                annotatedOwnerParametrizedType = annotatedType.getAnnotatedOwnerType();
            }

            String ownerType = parseType(owner, annotatedOwnerParametrizedType, context);

            return ownerType + "." + ContentJoiner.joinSpace(parsedAnnotations, classTypeName);
        } else if (classNameParser.isRequireFullName(clazz, context)) {
            String packageName = classNameParser.getPackageName(clazz);
            String simpleName = classNameParser.getSimpleName(clazz);

            String classLinkName = ContentJoiner.joinSpace(parsedAnnotations, simpleName);
            if (packageName.isEmpty()) {
                return classLinkName;
            } else {
                return packageName + "." + classLinkName;
            }
        } else {
            return ContentJoiner.joinSpace(parsedAnnotations, classTypeName);
        }
    }

    private String parseArrayLink(Type arrayType, AnnotatedArrayType annotatedArrayType, ParseContext context) {
        String boundType = parseType(getComponentType(arrayType), annotatedArrayType, context);

        AnnotatedType annotatedTypeForArray = getAnnotatedTypeForArray(arrayType, annotatedArrayType);
        String annotations = annotationParser.parseAnnotationsAsInline(annotatedTypeForArray, context);

        return boundType + annotations + "[]";
    }

    /**
     * Collecting list bound from type variable
     *
     * @param parameter type variable
     * @param context   context of parsing class process
     * @return resolved list of string bound with meta information
     */
    private List<String> parseBounds(TypeVariable<?> parameter, ParseContext context) {
        List<String> bounds = new ArrayList<>();
        Type[] typeBounds = parameter.getBounds();
        AnnotatedType[] annotatedBounds = parameter.getAnnotatedBounds();

        for (int index = 0; index < typeBounds.length; index++) {
            Type typeBound = typeBounds[index];
            AnnotatedType annotatedBound = annotatedBounds[index];
            if (configurationManager.isDisplayDefaultInheritance() || typeBound != Object.class) {
                bounds.add(parseType(typeBound, annotatedBound, context));
            }
        }

        return bounds;
    }

    private Type getComponentType(Type type) {
        if (type instanceof GenericArrayType genericArrayType) {
            return genericArrayType.getGenericComponentType();
        } else if (isClassArray(type)) {
            Class<?> arrayType = (Class<?>) type;
            return arrayType.getComponentType();
        } else {
            return type;
        }
    }

    private boolean isClassArray(Type type) {
        return type instanceof Class<?> && ((Class<?>) type).isArray();
    }

    private boolean isArrayType(Type type) {
        return type instanceof GenericArrayType || isClassArray(type);
    }

    /**
     * Resolve annotated types for array
     * It's behaviour because of case:
     * <code>
     * {@literal @}Annotation int{@literal @}Annotation[]{@literal @}Annotation3[] a;
     * </code>
     *
     * @param array         array type
     * @param annotatedType annotated types for genetic array
     * @return resolved annotated type
     */
    private AnnotatedType getAnnotatedTypeForArray(Type array, AnnotatedArrayType annotatedType) {
        if (annotatedType != null) {
            int dimensionIndex = 0;
            while (isArrayType(getComponentType(array))) {
                array = getComponentType(array);
                dimensionIndex++;
            }

            return getArrayAnnotatedType(annotatedType, dimensionIndex);
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
    private AnnotatedType getArrayAnnotatedType(AnnotatedArrayType annotatedType, int countIncludes) {
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
    private AnnotatedType resolveAnnotatedType(AnnotatedType annotatedType) {
        if (annotatedType instanceof AnnotatedArrayType annotatedArrayType) {
            while (annotatedArrayType.getAnnotatedGenericComponentType() instanceof AnnotatedArrayType) {
                AnnotatedType annotatedGenericComponentType = annotatedArrayType.getAnnotatedGenericComponentType();
                annotatedArrayType = (AnnotatedArrayType) annotatedGenericComponentType;
            }
            annotatedType = annotatedArrayType.getAnnotatedGenericComponentType();
        }

        return annotatedType;
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
                                               AnnotatedParameterizedType annotatedParameterizedType,
                                               ParseContext context) {
        List<String> genericArguments = new ArrayList<>();

        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        AnnotatedType[] annotatedActualTypeArguments = ifNull(annotatedParameterizedType);

        for (int index = 0; index < actualTypeArguments.length; index++) {
            Type actualTypeArgument = actualTypeArguments[index];
            AnnotatedType annotatedType = ifEmpty(annotatedActualTypeArguments, index);
            genericArguments.add(parseType(actualTypeArgument, annotatedType, context));
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
    private String parseWildCardsBound(Type[] types, String boundCase, AnnotatedType[] annotatedTypes, ParseContext context) {
        if (types.length != 0) {
            List<String> bounds = new ArrayList<>();
            for (int index = 0; index < types.length; index++) {
                Type type = types[index];
                AnnotatedType annotatedType = ifEmpty(annotatedTypes, index);
                if (configurationManager.isDisplayDefaultInheritance() || type != Object.class) {
                    bounds.add(parseType(type, annotatedType, context));
                }
            }

            if (!bounds.isEmpty()) {
                return ContentJoiner.joinSpace(boundCase, String.join(" & ", bounds));
            }
        }

        return "";
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