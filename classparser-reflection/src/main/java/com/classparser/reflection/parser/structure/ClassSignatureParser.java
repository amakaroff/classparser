package com.classparser.reflection.parser.structure;

import com.classparser.reflection.ContentJoiner;
import com.classparser.reflection.ParseContext;
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

    public ClassSignatureParser(ConfigurationManager configurationManager) {
        this.annotationParser = new AnnotationParser(configurationManager);
        this.indentParser = new IndentParser(configurationManager);
        this.modifierParser = new ModifierParser(configurationManager);
        this.classNameParser = new ClassNameParser(configurationManager);
        this.classTypeParser = new ClassTypeParser();
        this.genericTypeParser = new GenericTypeParser(configurationManager);
        this.inheritanceParser = new InheritanceParser(configurationManager);
    }

    /**
     * Parses signature for class
     * Include annotations, modifiers, type, name, generics and inheritances
     *
     * @param clazz   any class
     * @param context context of parsing class process
     * @return parsed signature of class
     */
    public String getClassSignature(Class<?> clazz, ParseContext context) {
        String annotations = annotationParser.parseAnnotationsAsBlockAboveClass(clazz, context);
        String indent = indentParser.getIndent(clazz, context);
        String modifiers = modifierParser.parseModifiers(clazz);
        String name = classNameParser.parseTypeName(clazz, context);
        String classType = classTypeParser.parseClassType(clazz);
        String generics = genericTypeParser.parseGenerics(clazz, false, context);
        String inheritances = inheritanceParser.parseInheritances(clazz, context);
        String content = ContentJoiner.joinNotEmptyContentBySpace(modifiers, classType, name);
        String specialContent = ContentJoiner.joinNotEmptyContentBySpace(generics, inheritances);

        if (generics.isEmpty()) {
            return annotations + indent + ContentJoiner.joinNotEmptyContentBySpace(content, specialContent);
        } else {
            return annotations + indent + content + specialContent;
        }
    }
}
