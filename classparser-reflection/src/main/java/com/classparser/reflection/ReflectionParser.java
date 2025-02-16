package com.classparser.reflection;

import com.classparser.api.ClassParser;
import com.classparser.configuration.Configuration;
import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.exception.ReflectionParserException;
import com.classparser.reflection.parser.structure.ClassContentParser;
import com.classparser.reflection.parser.structure.ClassSignatureParser;
import com.classparser.reflection.parser.structure.ImportParser;
import com.classparser.reflection.parser.structure.PackageParser;

/**
 * Implementation of {@link ClassParser} provides
 * functionality for parsing class by reflection mechanism
 * That parsing mechanism can parse all classes, including
 * array and primitive types.
 * <p>
 * Thread safe
 *
 * @author Aleksey Makarov
 * @author Vadim Kiselev
 * @since 1.0.0
 */
public class ReflectionParser implements ClassParser {

    private final ImportParser importParser;

    private final PackageParser packageParser;

    private final ClassContentParser classContentParser;

    private final ClassSignatureParser classSignatureParser;

    private final ConfigurationManager configurationManager;

    public ReflectionParser() {
        this(new ConfigurationManager());
    }

    public ReflectionParser(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
        this.importParser = new ImportParser(configurationManager);
        this.packageParser = new PackageParser(configurationManager);
        this.classSignatureParser = new ClassSignatureParser(configurationManager);
        this.classContentParser = new ClassContentParser(this, configurationManager);
    }

    @Override
    public String parseClass(Class<?> clazz) throws ReflectionParserException {
        return parseClass(clazz, new ParseContext(clazz));
    }

    public String parseClass(Class<?> clazz, ParseContext context) throws ReflectionParserException {
        if (clazz != null) {
            setUp(clazz, context);
            try {
                String packageName = packageParser.parsePackage(context);
                String imports = importParser.getImports(context);
                String classSignature = classSignatureParser.getClassSignature(context);
                String classBody = classContentParser.getClassContent(context);

                return packageName + imports + ContentJoiner.joinSpace(classSignature, classBody);
            } finally {
                tearDown(context);
            }
        }

        throw new ReflectionParserException("Parsed class can't be a null");
    }

    /**
     * Initializes current parser context
     *
     * @param clazz   class to be parsed
     * @param context context of parsing class process
     */
    private void setUp(Class<?> clazz, ParseContext context) {
        context.setCurrentParsedClass(clazz);
    }

    /**
     * Clears current parser context
     *
     * @param context context of parsing class process
     */
    private void tearDown(ParseContext context) {
        context.popCurrentClass();
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        configurationManager.reloadConfiguration(configuration);
    }
}