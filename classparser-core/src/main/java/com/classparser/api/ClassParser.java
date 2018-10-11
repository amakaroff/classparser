package com.classparser.api;

import com.classparser.configuration.Configuration;
import com.classparser.exception.ParsingException;

/**
 * Interface provides functional by parsing of classes
 * Exists two implementations:
 * 1. {ReflectionParser} - parsing classes by reflection mechanism
 *    and collecting all meta information
 * 2. {BytecodeParser} - parsing of bytecode and collecting
 *    full class code information
 *
 * @author Aleksei Makarov
 */
public interface ClassParser {

    /**
     * Get meta information of class
     *
     * @param clazz class for which getting meta-info
     * @return decompiled code of class
     * @throws ParsingException if process of parsing was interrupted with any error
     */
    String parseClass(Class<?> clazz) throws ParsingException;

    /**
     * Set configuration object
     *
     * @param configuration configuration object
     */
    void setConfiguration(Configuration configuration);
}