package com.classparser.reflection.parser.structure;

import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.configuration.ReflectionParserManager;
import com.classparser.reflection.parser.base.ClassNameParser;
import com.classparser.reflection.parser.base.AnnotationParser;
import com.classparser.reflection.parser.base.GenericTypeParser;
import com.classparser.reflection.parser.base.IndentParser;
import com.classparser.reflection.parser.base.ModifierParser;
import com.classparser.reflection.parser.base.ValueParser;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Class provides functionality for parsing fields meta information
 * Parsing includes annotation, generics and values for static fields
 *
 * @author Aleksey Makarov
 * @author Valim Kiselev
 * @since 1.0.0
 */
public class FieldParser {

    private final ReflectionParserManager manager;

    private final ConfigurationManager configurationManager;

    private final AnnotationParser annotationParser;

    private final IndentParser indentParser;

    private final ModifierParser modifierParser;

    private final GenericTypeParser genericTypeParser;

    private final ClassNameParser classNameParser;

    private final ValueParser valueParser;

    public FieldParser(ReflectionParserManager manager, AnnotationParser annotationParser, IndentParser indentParser,
                       ModifierParser modifierParser, GenericTypeParser genericTypeParser,
                       ClassNameParser classNameParser, ValueParser valueParser) {
        this.manager = manager;
        this.configurationManager = manager.getConfigurationManager();
        this.annotationParser = annotationParser;
        this.indentParser = indentParser;
        this.modifierParser = modifierParser;
        this.genericTypeParser = genericTypeParser;
        this.classNameParser = classNameParser;
        this.valueParser = valueParser;
    }

    /**
     * Parses fields meta information for class
     *
     * @param clazz any class
     * @return parsed fields
     */
    public String parseFields(Class<?> clazz) {
        List<String> fields = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (isShouldBeDisplayed(field)) {
                fields.add(parseField(field));
            }
        }

        if (clazz.isArray()) {
            fields.add(parseArrayLengthField(clazz));
        }

        return manager.joinContentByLineSeparator(fields);
    }

    /**
     * Checks if field displaying is necessary
     *
     * @param field any field
     * @return true if display is needed
     */
    private boolean isShouldBeDisplayed(Field field) {
        return configurationManager.isDisplaySyntheticEntities() || !field.isSynthetic();
    }

    /**
     * Parses field meta information
     * Include types, name, annotation and etc.
     *
     * @param field any field
     * @return parsed field
     */
    public String parseField(Field field) {
        String annotations = annotationParser.parseAnnotationsAsBlock(field);
        String indent = indentParser.getIndent(field);
        String modifiers = modifierParser.parseModifiers(field);
        String type = genericTypeParser.parseType(getType(field), field.getAnnotatedType());
        String name = classNameParser.getMemberName(field);
        String value = valueParser.getValue(field);

        return annotations + indent + manager.joinNotEmptyContentBySpace(modifiers, type, name) + value + ';';
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
    private String parseArrayLengthField(Class<?> clazz) {
        String indent = configurationManager.getIndentSpaces() + indentParser.getIndent(clazz);
        String modifiers = "public final";
        String type = "int";
        String name = "length";

        return indent + modifiers + " " + type + " " + name + ";";
    }
}