package com.classparser.reflection.parser.structure;

import com.classparser.reflection.ContentJoiner;
import com.classparser.reflection.ParseContext;
import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.exception.ReflectionParserException;
import com.classparser.reflection.parser.base.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class provides functionality for parsing fields meta information
 * Parsing includes annotation, generics and values for static fields
 *
 * @author Aleksey Makarov
 * @author Vadim Kiselev
 * @since 1.0.0
 */
public class FieldParser {

    private final AnnotationParser annotationParser;

    private final IndentParser indentParser;

    private final ModifierParser modifierParser;

    private final GenericTypeParser genericTypeParser;

    private final ClassNameParser classNameParser;

    private final ValueParser valueParser;

    private final ConfigurationManager configurationManager;

    public FieldParser(ConfigurationManager configurationManager) {
        this.annotationParser = new AnnotationParser(configurationManager);
        this.indentParser = new IndentParser(configurationManager);
        this.modifierParser = new ModifierParser(configurationManager);
        this.genericTypeParser = new GenericTypeParser(configurationManager);
        this.classNameParser = new ClassNameParser(configurationManager);
        this.valueParser = new ValueParser(configurationManager);
        this.configurationManager = configurationManager;
    }

    /**
     * Parse fields meta information for class
     *
     * @param clazz any class
     * @param context context of parsing class process
     * @return parsed fields
     */
    public String parseFields(Class<?> clazz, ParseContext context) {
        List<String> enumConstants = new ArrayList<>();
        List<String> staticFields = new ArrayList<>();
        List<String> instanceFields = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (isShouldBeDisplayed(field)) {
                String parsedField = parseField(field, context);

                if (field.isEnumConstant() && !configurationManager.isDisplayEnumAsClass()) {
                    enumConstants.add(parsedField);
                } else if (Modifier.isStatic(field.getModifiers())) {
                    staticFields.add(parsedField);
                } else {
                    instanceFields.add(parsedField);
                }
            }
        }

        staticFields.addAll(getStaticImplicitFields(clazz, context));
        instanceFields.addAll(getInstanceImplicitFields(clazz, context));

        if (clazz.isEnum() && !configurationManager.isDisplayEnumAsClass()) {
            return joinEnumFields(clazz, context, enumConstants, staticFields, instanceFields);
        } else {
            return joinClassFields(staticFields, instanceFields);
        }
    }

    /**
     * Parses the field meta information
     * Include types, name, annotation etc.
     *
     * @param field any field
     * @param context context of parsing class process
     * @return parsed field
     */
    public String parseField(Field field, ParseContext context) {
        if (field.isEnumConstant() && !configurationManager.isDisplayEnumAsClass()) {
            String annotations = annotationParser.parseAnnotationsAsBlock(field, context);
            String indent = indentParser.getIndent(field, context);

            return annotations + indent + field.getName();
        } else {
            String annotations = annotationParser.parseAnnotationsAsBlock(field, context);
            String indent = indentParser.getIndent(field, context);
            String modifiers = modifierParser.parseModifiers(field);
            String type = genericTypeParser.parseType(getType(field),
                    classNameParser.isInnerClassInStaticContext(field, field.getType()),
                    field.getAnnotatedType(),
                    context);
            String name = classNameParser.getMemberName(field);
            String value = valueParser.parseValue(field, context);

            return annotations + indent + ContentJoiner.joinNotEmptyContentBySpace(modifiers, type, name) + value + ';';
        }
    }

    private List<String> getInstanceImplicitFields(Class<?> declaredClass, ParseContext context) {
        List<String> instanceFields = new ArrayList<>();

        if (declaredClass.isArray()) {
            instanceFields.add(parseArrayLengthImplicitField(declaredClass, context));
        }

        if (declaredClass.equals(Throwable.class)) {
            instanceFields.add(parseThrowableImplicitField(declaredClass, context));
        }

        if (declaredClass.equals(Class.class)) {
            instanceFields.add(parseClassImplicitField(declaredClass, context));
        }

        if (declaredClass.equals(getConstantPoolClass())) {
            instanceFields.add(parseConstantPoolImplicitField(declaredClass, context));
        }

        if (declaredClass.equals(getUnsafeStaticFieldAccessorImplClass())) {
            instanceFields.add(parseUnsafeStaticFieldAccessorImplImplicitField(declaredClass, context));
        }

        return instanceFields;
    }

    private List<String> getStaticImplicitFields(Class<?> declaredClass, ParseContext context) {
        List<String> staticFields = new ArrayList<>();

        if (declaredClass.equals(System.class)) {
            staticFields.add(parseSystemImplicitField(declaredClass, context));
        }

        if (declaredClass.equals(getReflectionClass())) {
            staticFields.add(parseFirstReflectionImplicitField(declaredClass, context));
            staticFields.add(parseSecondReflectionImplicitField(declaredClass, context));
        }

        return staticFields;
    }

    private String joinEnumFields(Class<?> clazz,
                                  ParseContext context,
                                  List<String> enumConstants,
                                  List<String> staticFields,
                                  List<String> instanceFields) {
        String lineSeparator = configurationManager.getLineSeparator();
        String doubleLineSeparator = lineSeparator + lineSeparator;

        String parsedEnumConstants = "";
        if (!enumConstants.isEmpty()) {
            parsedEnumConstants = String.join("," + doubleLineSeparator, enumConstants) + ";";
        } else if (!staticFields.isEmpty() || !instanceFields.isEmpty()) {
            String indent = indentParser.getIndent(clazz, context) + configurationManager.getIndentSpaces();
            parsedEnumConstants = indent + ";";
        }

        String parsedStaticFields = String.join(doubleLineSeparator, staticFields);
        String parsedInstanceFields = String.join(doubleLineSeparator, instanceFields);

        String fields = ContentJoiner.joinNotEmpty(doubleLineSeparator, parsedEnumConstants, parsedStaticFields, parsedInstanceFields);

        if (fields.isEmpty()) {
            return "";
        } else {
            return fields + lineSeparator;
        }
    }

    private String joinClassFields(List<String> staticFields, List<String> instanceFields) {
        List<String> fields = new ArrayList<>();

        fields.addAll(staticFields);
        fields.addAll(instanceFields);

        return ContentJoiner.joinContent(fields, configurationManager.getLineSeparator());
    }

    /**
     * Checks if field displaying is necessary
     *
     * @param field any field
     * @return true if display is needed
     */
    private boolean isShouldBeDisplayed(Field field) {
        if (field.getDeclaringClass().isEnum()) {
            return configurationManager.isDisplaySyntheticEntities() && configurationManager.isDisplayEnumAsClass() ||
                    !field.isSynthetic();
        } else {
            return configurationManager.isDisplaySyntheticEntities() || !field.isSynthetic();
        }
    }


    /**
     * Obtains type of field in depend on {@link ConfigurationManager#isDisplayGenericSignatures()}
     *
     * @param field any field
     * @return usual or generic type of field
     */
    private Type getType(Field field) {
        if (configurationManager.isDisplayGenericSignatures()) {
            return field.getGenericType();
        } else {
            return field.getType();
        }
    }

    private String parseImplicitField(Class<?> clazz, ParseContext context, int modifiers, Type type, String name) {
        return configurationManager.getIndentSpaces() + indentParser.getIndent(clazz, context) +
                modifierParser.parseFieldModifiers(modifiers, clazz) + " " +
                genericTypeParser.parseType(type, context) + " " +
                name + ";";
    }

    /**
     * Construct "secret" array "length" field for array type
     *
     * @param clazz any array class
     * @return parsed "length" field
     */
    private String parseArrayLengthImplicitField(Class<?> clazz, ParseContext context) {
        int modifiers = Modifier.PUBLIC | Modifier.FINAL;
        return parseImplicitField(clazz, context, modifiers, int.class, "length");
    }

    private String parseThrowableImplicitField(Class<?> clazz, ParseContext context) {
        int modifiers = Modifier.PRIVATE | Modifier.TRANSIENT;
        return parseImplicitField(clazz, context, modifiers, Object.class, "backtrace");
    }

    private String parseClassImplicitField(Class<?> clazz, ParseContext context) {
        int modifiers = Modifier.PRIVATE | Modifier.FINAL;
        return parseImplicitField(clazz, context, modifiers, ClassLoader.class, "classLoader");
    }

    private String parseSystemImplicitField(Class<?> clazz, ParseContext context) {
        int modifiers = Modifier.PRIVATE | Modifier.STATIC & Modifier.VOLATILE;
        return parseImplicitField(clazz, context, modifiers, SecurityManager.class, "security");
    }

    private String parseFirstReflectionImplicitField(Class<?> clazz, ParseContext context) {
        int modifiers = Modifier.PRIVATE | Modifier.STATIC | Modifier.VOLATILE;
        return parseImplicitField(clazz, context, modifiers, getMapType(), "fieldFilterMap");
    }

    private String parseSecondReflectionImplicitField(Class<?> clazz, ParseContext context) {
        int modifiers = Modifier.PRIVATE | Modifier.STATIC | Modifier.VOLATILE;
        return parseImplicitField(clazz, context, modifiers, getMapType(), "methodFilterMap");
    }

    private String parseConstantPoolImplicitField(Class<?> clazz, ParseContext context) {
        int modifiers = Modifier.PRIVATE;
        return parseImplicitField(clazz, context, modifiers, Object.class, "constantPoolOop");
    }

    private String parseUnsafeStaticFieldAccessorImplImplicitField(Class<?> clazz, ParseContext context) {
        int modifiers = Modifier.PROTECTED | Modifier.FINAL;
        return parseImplicitField(clazz, context, modifiers, Object.class, "base");
    }

    private Class<?> getReflectionClass() {
        return classNameParser.forNameOrNull("sun.reflect.Reflection");
    }

    private Class<?> getConstantPoolClass() {
        return classNameParser.forNameOrNull("sun.reflect.ConstantPool");
    }

    private Class<?> getUnsafeStaticFieldAccessorImplClass() {
        return classNameParser.forNameOrNull("sun.reflect.UnsafeStaticFieldAccessorImpl");
    }

    private Type getMapType() {
        try {
            Field mapField = GenericFieldStorage.class.getField("mapField");
            return mapField.getGenericType();
        } catch (NoSuchFieldException exception) {
            throw new ReflectionParserException("Unexpected exception", exception);
        }
    }

    private static final class GenericFieldStorage {
        public Map<Class<?>, String[]> mapField;
    }
}