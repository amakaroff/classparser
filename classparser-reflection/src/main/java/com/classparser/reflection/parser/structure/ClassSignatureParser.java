package com.classparser.reflection.parser.structure;

import com.classparser.reflection.ContentJoiner;
import com.classparser.reflection.ParseContext;
import com.classparser.reflection.Phase;
import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.parser.ClassTypeParser;
import com.classparser.reflection.parser.InheritanceParser;
import com.classparser.reflection.parser.base.*;

public class ClassSignatureParser {

    private final AnnotationParser annotationParser;

    private final IndentParser indentParser;

    private final ModifierParser modifierParser;

    private final ClassNameParser classNameParser;

    private final ClassTypeParser classTypeParser;

    private final GenericTypeParser genericTypeParser;

    private final InheritanceParser inheritanceParser;

    private final ConfigurationManager manager;

    public ClassSignatureParser(ConfigurationManager configurationManager) {
        this.annotationParser = new AnnotationParser(configurationManager);
        this.indentParser = new IndentParser(configurationManager);
        this.modifierParser = new ModifierParser(configurationManager);
        this.classNameParser = new ClassNameParser(configurationManager);
        this.classTypeParser = new ClassTypeParser();
        this.genericTypeParser = new GenericTypeParser(configurationManager);
        this.inheritanceParser = new InheritanceParser(configurationManager);
        this.manager = configurationManager;
    }

    /**
     * Parses signature for class
     * Include annotations, modifiers, type, name, generics and inheritances
     *
     * @param context context of parsing class process
     * @return parsed signature of class
     */
    public String getClassSignature(ParseContext context) {
        context.setPhase(Phase.SIGNATURE);
        try {
            Class<?> currentParsedClass = context.getCurrentParsedClass();
            if (manager.isDisplayPackageInfoAsClass() || !classNameParser.isPackageInfo(currentParsedClass)) {
                String annotations = annotationParser.parseAnnotationsAsBlock(currentParsedClass, context);
                String indent = indentParser.getIndent(currentParsedClass, context);
                String modifiers = modifierParser.parseModifiers(currentParsedClass);
                String name = classNameParser.parseTypeName(currentParsedClass, context);
                String classType = classTypeParser.parseClassType(context);
                String generics = genericTypeParser.parseGenerics(currentParsedClass, context);
                String inheritances = inheritanceParser.parseInheritances(context);
                String content = ContentJoiner.joinSpace(modifiers, classType, name);
                String specialContent = ContentJoiner.joinSpace(generics, inheritances);

                if (generics.isEmpty()) {
                    return annotations + indent + ContentJoiner.joinSpace(content, specialContent);
                } else {
                    return annotations + indent + content + specialContent;
                }
            } else {
                return "";
            }
        } finally {
            context.clearPhase();
        }
    }
}
