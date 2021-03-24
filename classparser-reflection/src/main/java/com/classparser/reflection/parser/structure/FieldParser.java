package com.classparser.reflection.parser.structure;

import com.classparser.reflection.ParseContext;
import com.classparser.reflection.ContentJoiner;
import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.parser.base.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Class provides functionality for parsing fields meta information
 * Parsing includes annotation, generics and values for static fields
 *
 * @author Aleksey Makarov
 * @author Vadim Kiselev
 * @since 1.0.0
 */
public class FieldParser {

    private final ConfigurationManager configurationManager;

    private final AnnotationParser annotationParser;

    private final IndentParser indentParser;

    private final ModifierParser modifierParser;

    private final GenericTypeParser genericTypeParser;

    private final ClassNameParser classNameParser;

    private final ValueParser valueParser;

    public FieldParser(ConfigurationManager configurationManager,
                       AnnotationParser annotationParser,
                       IndentParser indentParser,
                       ModifierParser modifierParser,
                       GenericTypeParser genericTypeParser,
                       ClassNameParser classNameParser,
                       ValueParser valueParser) {
        this.configurationManager = configurationManager;
        this.annotationParser = annotationParser;
        this.indentParser = indentParser;
        this.modifierParser = modifierParser;
        this.genericTypeParser = genericTypeParser;
        this.classNameParser = classNameParser;
        this.valueParser = valueParser;
    }

    /**
     * Parse fields meta information for class
     *
     * @param clazz any class
     * @return parsed fields
     */
    public String parseFields(Class<?> clazz, ParseContext context) {
        List<String> enumConstants = new ArrayList<>();
        List<String> staticFields = new ArrayList<>();
        List<String> instanceFields = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (isShouldBeDisplayed(field)) {
                String parsedField = parseField(field, context);

                if (!configurationManager.isParseEnumAsClass() && field.isEnumConstant()) {
                    enumConstants.add(parsedField);
                } else if (Modifier.isStatic(field.getModifiers())) {
                    staticFields.add(parsedField);
                } else {
                    instanceFields.add(parsedField);
                }

            }
        }

        if (clazz.isArray()) {
            instanceFields.add(parseArrayLengthField(clazz, context));
        }

        if (clazz.equals(Throwable.class)) {
            instanceFields.add(parseSpecialThrowableField(clazz, context));
        }

        if (clazz.isEnum() && !configurationManager.isParseEnumAsClass()) {
            String lineSeparator = configurationManager.getLineSeparator();
            String doubleLineSeparator = lineSeparator + lineSeparator;

            String parsedEnumConstants = "";
            if (!enumConstants.isEmpty()) {
                parsedEnumConstants = String.join("," + doubleLineSeparator, enumConstants) + ";" + lineSeparator;
            }

            String parsedStaticFields = "";
            if (!staticFields.isEmpty()) {
                parsedStaticFields = lineSeparator + String.join(doubleLineSeparator, staticFields);
            }

            String parsedInstanceFields = "";
            if (!staticFields.isEmpty()) {
                parsedInstanceFields = lineSeparator + String.join(doubleLineSeparator, instanceFields) + lineSeparator;
            }

            return parsedEnumConstants + parsedStaticFields + parsedInstanceFields;
        } else {
            staticFields.addAll(instanceFields);
            return ContentJoiner.joinContent(staticFields, configurationManager.getLineSeparator());
        }
    }

    /**
     * Checks if field displaying is necessary
     *
     * @param field any field
     * @return true if display is needed
     */
    private boolean isShouldBeDisplayed(Field field) {
        if (field.getDeclaringClass().isEnum()) {
            return configurationManager.isDisplaySyntheticEntities() && configurationManager.isParseEnumAsClass() ||
                    !field.isSynthetic();
        } else {
            return configurationManager.isDisplaySyntheticEntities() || !field.isSynthetic();
        }
    }

    /**
     * Parses the field meta information
     * Include types, name, annotation etc.
     *
     * @param field any field
     * @return parsed field
     */
    public String parseField(Field field, ParseContext context) {
        Class<?> declaringClass = field.getDeclaringClass();

        if (declaringClass.isEnum() && field.isEnumConstant() && !configurationManager.isParseEnumAsClass()) {
            String annotations = annotationParser.parseAnnotationsAsBlock(field, context);
            String indent = indentParser.getIndent(field, context);

            return annotations + indent + field.getName();
        } else {
            String annotations = annotationParser.parseAnnotationsAsBlock(field, context);
            String indent = indentParser.getIndent(field, context);
            String modifiers = modifierParser.parseModifiers(field);
            String type = genericTypeParser.parseType(getType(field), field.getAnnotatedType(), context);
            String name = classNameParser.getMemberName(field);
            String value = valueParser.getValue(field, context);

            return annotations + indent + ContentJoiner.joinNotEmptyContentBySpace(modifiers, type, name) + value + ';';
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

    /**
     * Construct "secret" array "length" field for array type
     *
     * @param clazz any array class
     * @return parsed "length" field
     */
    private String parseArrayLengthField(Class<?> clazz, ParseContext context) {
        String indent = configurationManager.getIndentSpaces() + indentParser.getIndent(clazz, context);
        String modifiers = "public final";
        String type = "int";
        String name = "length";

        return indent + modifiers + " " + type + " " + name + ";";
    }

    private String parseSpecialThrowableField(Class<?> clazz, ParseContext context) {
        String indent = configurationManager.getIndentSpaces() + indentParser.getIndent(clazz, context);
        String modifiers = "private transient";
        String type = "Object";
        String name = "backtrace";

        return indent + modifiers + " " + type + " " + name + ";";
    }
}