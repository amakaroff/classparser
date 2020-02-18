package com.classparser.api;

import com.classparser.configuration.Configuration;
import com.classparser.exception.ParsingException;

/**
 * Interface provides functional by parsing of classes
 * Exists two implementations:
 * 1. {ReflectionParser} - based on reflection mechanism for parsing classes
 *    and collecting all meta information
 * 2. {ByteCodeParser} - based on byte code collecting mechanism for parsing and collecting
 *    full class code information used decompilers
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