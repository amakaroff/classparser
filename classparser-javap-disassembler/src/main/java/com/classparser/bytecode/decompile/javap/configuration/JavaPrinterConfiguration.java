package com.classparser.bytecode.decompile.javap.configuration;

import com.classparser.bytecode.decompile.javap.JavaPrinterDisassembler;
import com.classparser.configuration.Configuration;
import com.sun.tools.javap.InstructionDetailWriter.Kind;

import java.lang.reflect.Modifier;

/**
 * Interface for builder configuration for {@link JavaPrinterDisassembler}
 */
public interface JavaPrinterConfiguration extends Configuration {

    String DISPLAY_DECOMPILE_CODE_KEY = "ddc";

    String DISPLAY_ATTRIBUTES_OF_CODE_KEY = "aoc";

    String DISPLAY_CODE_LINE_AND_LOCAL_VARIABLE_KEY = "cll";

    String DISPLAY_SYSTEM_INFORMATION_KEY = "dsi";

    String DISPLAY_VERBOSE_INFORMATION_KEY = "dvi";

    String DISPLAY_CONSTANTS_KEY = "dkk";

    String DISPLAY_MODIFIER_ACCESSOR_KEY = "dma";

    String INDENT_COUNT_SPACES_KEY = "ics";

    String DISPLAY_DESCRIPTORS_KEY = "dds";

    String APPEND_DISPLAY_DETAILS_KEY = "add";

    /**
     * Displays decompiled code for class
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    JavaPrinterConfiguration displayDecompiledCode(boolean flag);

    /**
     * Displays all attributes of code for methods
     *
     * @param flag true/false value
     * @return builder instance
     */
    JavaPrinterConfiguration displayAllAttributesOfCode(boolean flag);

    /**
     * Displays line of code and local variable table
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    JavaPrinterConfiguration displayCodeLineAndLocalVariableTable(boolean flag);

    /**
     * Displays system information about path to class file
     *
     * @param flag true/false value
     * @return builder instance
     */
    JavaPrinterConfiguration displaySystemInformation(boolean flag);

    /**
     * Displays full information about bytecode of class
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    JavaPrinterConfiguration displayVerboseInformation(boolean flag);

    /**
     * Displays constant value for static final fields
     *
     * @param flag true/false value
     * @return builder instance
     */
    JavaPrinterConfiguration displayConstants(boolean flag);

    /**
     * Appends access modifier to displayed access modifier set
     * <p>
     * Default value: {@link AccessModifier#PUBLIC}, {@link AccessModifier#PRIVATE},
     * {@link AccessModifier#PROTECTED}, {@link AccessModifier#PACKAGE}
     *
     * @param accessModifier any access modifier
     * @return builder instance
     */
    JavaPrinterConfiguration appendDisplayOnlyElementsWithAccessModifier(AccessModifier accessModifier);

    /**
     * Sets count of indent spaces for disassemble code
     * <p>
     * Default value: 4
     *1
     * @param count count of indent spaces
     * @return builder instance
     */
    JavaPrinterConfiguration setIndentSpaces(int count);

    /**
     * Displays descriptor for methods
     *
     * @param flag true/false value
     * @return builder instance
     */
    JavaPrinterConfiguration displayDescriptors(boolean flag);

    /**
     * Appends kind to displayed kind set
     * <p>
     * Default value: {@link Kind#TRY_BLOCKS},
     * {@link Kind#TYPE_ANNOS}
     *
     * @param kind any kind
     * @return builder instance
     */
    JavaPrinterConfiguration appendDisplayDetails(Kind kind);

    /**
     * Inner class uses for store information about
     */
    enum AccessModifier {

        PUBLIC(Modifier.PUBLIC, "public"),

        PRIVATE(Modifier.PRIVATE, "private"),

        PROTECTED(Modifier.PROTECTED, "protected"),

        PACKAGE(0, "");

        private final int modifier;

        private final String name;

        /**
         * Private constructor for create enum value
         *
         * @param modifier modifier mask
         * @param name     name of modifier
         */
        AccessModifier(int modifier, String name) {
            this.modifier = modifier;
            this.name = name;
        }

        /**
         * Getter for {@link #modifier}
         *
         * @return modifier mask
         */
        public int getModifier() {
            return modifier;
        }

        /**
         * Getter for {@link #name}
         *
         * @return name of modifier
         */
        public String getName() {
            return name;
        }
    }
}