package com.classparser.reflection.parser.structure.executeble;

import com.classparser.reflection.ParseContext;
import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.parser.base.GenericTypeParser;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Class provides functionality for parsing meta information
 * about throws exceptions for {@link Executable} objects
 *
 * @author Aleksey Makarov
 * @author Valim Kiselev
 * @since 1.0.0
 */
public class ExceptionParser {

    private final GenericTypeParser genericTypeParser;

    private final ConfigurationManager configurationManager;

    public ExceptionParser(GenericTypeParser genericTypeParser, ConfigurationManager configurationManager) {
        this.genericTypeParser = genericTypeParser;
        this.configurationManager = configurationManager;
    }

    /**
     * Parses the throws exceptions meta information and collects it to {@link String}
     * For example:
     * <code>
     * throws Exception, RuntimeException
     * </code>
     * This method returns the follow string: " throws Exception, RuntimeException"
     *
     * @param executable any executable ({@link Method}, {@link Constructor})
     * @return parsed exceptions or empty string if throws exceptions is not exists
     */
    public String parseExceptions(Executable executable, ParseContext context) {
        String exceptions = "";

        AnnotatedType[] annotatedExceptionTypes = executable.getAnnotatedExceptionTypes();
        Type[] exceptionTypes = getExceptionTypes(executable);

        List<String> exceptionTypesList = new ArrayList<>();
        for (int index = 0; index < exceptionTypes.length; index++) {
            exceptionTypesList.add(genericTypeParser.parseType(exceptionTypes[index],
                    annotatedExceptionTypes[index],
                    context));
        }

        if (!exceptionTypesList.isEmpty()) {
            exceptions += " throws " + String.join(", ", exceptionTypesList);
        }

        return exceptions;
    }

    /**
     * Obtains exception types from executable object in depend on
     * {@link ConfigurationManager#isDisplayGenericSignatures()}
     *
     * @param executable any executable
     * @return array of throwing exceptions
     */
    private Type[] getExceptionTypes(Executable executable) {
        if (configurationManager.isDisplayGenericSignatures()) {
            return executable.getGenericExceptionTypes();
        } else {
            return executable.getExceptionTypes();
        }
    }
}