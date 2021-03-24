package com.classparser.reflection.configuration.api;

import com.classparser.configuration.Configuration;
import com.classparser.reflection.ReflectionParser;

import java.lang.annotation.ElementType;

/**
 * Builder interface provides configuration API for class: {@link ReflectionParser}
 *
 * @author Aleksey Makarov
 * @since 1.0.0
 */
public interface ReflectionParserConfiguration extends Configuration {

    String ANNOTATION_TYPE_DISPLAY_KEY = "sat";

    String INNER_CLASSES_DISPLAY_KEY = "sic";

    String SYNTHETIC_ENTITIES_DISPLAY_KEY = "njm";

    String DISPLAY_DEFAULT_VALUE_IN_ANNOTATIONS_KEY = "dva";

    String DISPLAY_GENERIC_SIGNATURES_KEY = "sgs";

    String DISPLAY_VAR_ARGS_KEY = "sva";

    String DISPLAY_VALUE_IN_STATIC_FIELDS_KEY = "dvf";

    String DISPLAY_IMPORT_SECTION_KEY = "dim";

    String DISPLAY_DEFAULT_INHERITANCE_KEY = "doi";

    String HIDE_EXHAUSTIVE_MODIFIERS_KEY = "hem";

    String COUNT_INDENT_SPACES_KEY = "cis";

    String LINE_SEPARATOR_KEY = "nlc";

    String DISPLAY_STATIC_BLOCK = "dsb";

    String PARSE_ENUM_AS_CLASS = "pec";

    /**
     * Set displaying annotation on types like
     * example {@link ElementType#TYPE_USE} or {@link ElementType#TYPE_PARAMETER}
     * <p>
     * Default value: true
     * </p>
     *
     * @param flag true/false value
     * @return builder instance
     */
    ReflectionParserConfiguration displayAnnotationOnTypes(boolean flag);

    /**
     * Set displaying inner classes
     * <p>
     * Default value: true
     * </p>
     *
     * @param flag true/false value
     * @return builder instance
     */
    ReflectionParserConfiguration displayInnerClasses(boolean flag);

    /**
     * Set visible non java modifiers, as
     * synthetic, implicit and bridge
     * <p>
     * Default value: false
     * </p>
     *
     * @param flag true/false value
     * @return builder instance
     */
    ReflectionParserConfiguration displaySyntheticEntities(boolean flag);

    /**
     * Set display default value in annotations
     * <p>
     * Default value: false
     * </p>
     *
     * @param flag true/false value
     * @return builder instance
     */
    ReflectionParserConfiguration displayDefaultValueInAnnotation(boolean flag);

    /**
     * Set display generic signatures
     * <p>
     * Default value: true
     * </p>
     *
     * @param flag true/false value
     * @return builder instance
     */
    ReflectionParserConfiguration displayGenericSignatures(boolean flag);

    /**
     * Set display variate arguments
     * <p>
     * Default value: true
     * </p>
     *
     * @param flag true/false value
     * @return builder instance
     */
    ReflectionParserConfiguration displayVarArgs(boolean flag);

    /**
     * Set display value for fields
     * <p>
     * Default value: true
     * </p>
     *
     * @param flag true/false value
     * @return builder instance
     */
    ReflectionParserConfiguration displayValueForFields(boolean flag);

    /**
     * Set enable parsing import section for class
     * <p>
     * Default value: true
     * </p>
     *
     * @param flag true/false value
     * @return builder instance
     */
    ReflectionParserConfiguration enableImportSection(boolean flag);

    /**
     * Set display default inheritance from object class
     * <p>
     * Default value: false
     * </p>
     *
     * @param flag true/false value
     * @return builder instance
     */
    ReflectionParserConfiguration displayDefaultInheritance(boolean flag);

    /**
     * Set hide the exhaustive modifiers,
     * for example: public and abstract for interfaces
     * <p>
     * Default value: false
     * </p>
     *
     * @param flag true/false value
     * @return builder instance
     */
    ReflectionParserConfiguration hideExhaustiveModifiers(boolean flag);

    /**
     * Set count of indent for parser structure of class
     * <p>
     * Default value: 4 spaces
     * </p>
     *
     * @param count count of spaces
     * @return builder instance
     */
    ReflectionParserConfiguration setCountIndentSpaces(int count);

    /**
     * define new line character to be used for output.
     * '\r\n' (Windows),
     * '\n' (Unix)
     * <p>
     * Default value: selected by OS
     * </p>
     *
     * @param character line separator
     * @return builder instance
     */
    ReflectionParserConfiguration defineLineSeparator(String character);

    /**
     * Set to enable showing static init block in classes
     * <p>
     * Default value: true
     * </p>
     *
     * @param flag true if block should be shown
     * @return builder instance
     */
    ReflectionParserConfiguration displayStaticBlock(boolean flag);


    /**
     * Set to parse enum type as simple java class
     * <p>
     * Default value: false
     * </p>
     *
     * @param flag true if enum should be parsed as class
     * @return builder instance
     */
    ReflectionParserConfiguration parseEnumsAsClass(boolean flag);
}