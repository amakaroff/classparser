package com.classparser.reflection.configuration;

import com.classparser.configuration.Configuration;
import com.classparser.reflection.ReflectionParser;
import com.classparser.util.ConfigurationUtils;

import java.util.HashMap;
import java.util.Map;

import static com.classparser.reflection.configuration.api.ReflectionParserConfiguration.*;

/**
 * Basic configuration manager for {@link ReflectionParser}
 * Provides methods for checking current configuration on the parser
 * <p>
 * This class is thread safe
 * </p>
 *
 * @author Aleksey Makarov
 * @since 1.0.0
 */
public class ConfigurationManager {

    private final ConfigurationUtils utils;

    public ConfigurationManager() {
        this.utils = new ConfigurationUtils(new HashMap<>(), getDefaultConfiguration());
    }

    /**
     * Chooses line separator
     *
     * @return "'\n\r" if system windows or "\n" in other cases
     */
    private static String chooseSystemNewLineCharacter() {
        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            return "\n\r";
        } else {
            return "\n";
        }
    }

    /**
     * Obtain default configuration
     * <ul>
     *      <li>{@link #isDisplayAnnotationOnTypes()} - yes</li>
     *      <li>{@link #isDisplayInnerClasses()} - yes</li>
     *      <li>{@link #isDisplaySyntheticEntities()} - no</li>
     *      <li>{@link #isDisplayDefaultValueInAnnotation()} - no</li>
     *      <li>{@link #isDisplayFieldValue()} - yes</li>
     *      <li>{@link #isDisplayGenericSignatures()} - yes</li>
     *      <li>{@link #isDisplayVarArgs()} - yes</li>
     *      <li>{@link #isDisplayDefaultInheritance()} - false</li>
     *      <li>{@link #hideExhaustiveModifiers()} - true</li>
     *      <li>{@link #isDisplayStaticBlock()} - true</li>
     *      <li>{@link #isParseEnumAsClass()} - false</li>
     *      <li>{@link #isEnabledImports()} - yes</li>
     *      <li>{@link #getIndentSpaces()} - 4 spaces</li>
     *      <li>{@link #chooseSystemNewLineCharacter} - choice depend on the system</li>
     * </ul>
     *
     * @return default configuration
     */
    protected Map<String, Object> getDefaultConfiguration() {
        return ReflectionParserBuilderConfiguration
                .getBuilder()
                .displayAnnotationOnTypes(true)
                .displayInnerClasses(true)
                .displaySyntheticEntities(false)
                .displayDefaultValueInAnnotation(false)
                .displayValueForFields(true)
                .displayGenericSignatures(true)
                .displayVarArgs(true)
                .enableImportSection(true)
                .hideExhaustiveModifiers(true)
                .displayDefaultInheritance(false)
                .displayStaticBlock(true)
                .parseEnumsAsClass(false)
                .setCountIndentSpaces(4)
                .defineLineSeparator(chooseSystemNewLineCharacter())
                .getConfiguration();
    }

    /**
     * Reloads current configuration
     *
     * @param configuration new reflection parser configuration instance
     */
    public void reloadConfiguration(Configuration configuration) {
        this.utils.reloadConfiguration(configuration);
    }

    /**
     * Checks if annotation on types displaying is necessary
     * <code>
     * {@literal @}AnnotationOnType MyType a;
     * </code>
     *
     * @return true is display needed
     */
    public boolean isDisplayAnnotationOnTypes() {
        return utils.getConfigOption(ANNOTATION_TYPE_DISPLAY_KEY, Boolean.class);
    }

    /**
     * Check if inner and nested classes displaying is necessary
     *
     * @return true if display is needed
     */
    public boolean isDisplayInnerClasses() {
        return utils.getConfigOption(INNER_CLASSES_DISPLAY_KEY, Boolean.class);
    }

    /**
     * Checks if synthetic entities displaying is necessary
     * For example synthetic, bridge or implicit
     *
     * @return true if need display synthetic entities
     */
    public boolean isDisplaySyntheticEntities() {
        return utils.getConfigOption(SYNTHETIC_ENTITIES_DISPLAY_KEY, Boolean.class);
    }

    /**
     * Checks if default value in annotation display is necessary
     *
     * @return true if display is needed
     */
    public boolean isDisplayDefaultValueInAnnotation() {
        return utils.getConfigOption(DISPLAY_DEFAULT_VALUE_IN_ANNOTATIONS_KEY, Boolean.class);
    }

    /**
     * Checks if generic signatures for types displaying is necessary
     *
     * @return true if display is needed
     */
    public boolean isDisplayGenericSignatures() {
        return utils.getConfigOption(DISPLAY_GENERIC_SIGNATURES_KEY, Boolean.class);
    }

    /**
     * Checks if var args for arguments displaying is necessary
     *
     * @return true if display is needed
     */
    public boolean isDisplayVarArgs() {
        return utils.getConfigOption(DISPLAY_VAR_ARGS_KEY, Boolean.class);
    }

    /**
     * Checks if value for static fields displaying is necessary
     *
     * @return true if display is needed
     */
    public boolean isDisplayFieldValue() {
        return utils.getConfigOption(DISPLAY_VALUE_IN_STATIC_FIELDS_KEY, Boolean.class);
    }

    /**
     * Checks if import section in parsed code display is necessary
     *
     * @return true if import section is enabled
     */
    public boolean isEnabledImports() {
        return utils.getConfigOption(DISPLAY_IMPORT_SECTION_KEY, Boolean.class);
    }

    /**
     * Checks default inheritance displaying is necessary
     * <code>
     * class MyClass extends Object {
     * ...
     * }
     * </code>
     * or
     * <code>
     * enum MyEnum extends Enum{@literal <}MyEnum{@literal >} {
     * ...
     * }
     * </code>
     *
     * @return true if display is needed
     */
    public boolean isDisplayDefaultInheritance() {
        return utils.getConfigOption(DISPLAY_DEFAULT_INHERITANCE_KEY, Boolean.class);
    }

    /**
     * Checks if exhaustive modifiers hiding is necessary, for example
     * it's abstract and public for methods in interface
     *
     * @return true if should hide exhaustive modifiers
     */
    public boolean hideExhaustiveModifiers() {
        return utils.getConfigOption(HIDE_EXHAUSTIVE_MODIFIERS_KEY, Boolean.class);
    }

    /**
     * Checks is static initializer block in classes should be displayed
     * That options use special reflection hacks for display it and
     * may be disabled in future versions
     *
     * @return true if static block should be shown
     */
    public boolean isDisplayStaticBlock() {
        return utils.getConfigOption(DISPLAY_STATIC_BLOCK, Boolean.class);
    }

    /**
     * Checks is parser should parse enum type as simple java class
     * or display special enum information
     *
     * @return true if enum should be parsed as class
     */
    public boolean isParseEnumAsClass() {
        return utils.getConfigOption(PARSE_ENUM_AS_CLASS, Boolean.class);
    }

    /**
     * Obtains count of indent spaces
     *
     * @return string includes count of spaces
     */
    public String getIndentSpaces() {
        return utils.getConfigOption(COUNT_INDENT_SPACES_KEY, String.class);
    }

    /**
     * Obtains symbol for line separator
     *
     * @return line separator symbol
     */
    public String getLineSeparator() {
        return utils.getConfigOption(LINE_SEPARATOR_KEY, String.class);
    }
}