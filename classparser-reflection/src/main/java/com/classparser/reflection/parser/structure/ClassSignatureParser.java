package com.classparser.reflection.parser.structure;

import com.classparser.reflection.configuration.ReflectionParserManager;
import com.classparser.reflection.parser.ClassTypeParser;
import com.classparser.reflection.parser.InheritanceParser;
import com.classparser.reflection.parser.base.*;

/**
 * Class provides functionality for parsing signature of class: modifiers, inheritance, generic and etc.
 *
 * @author Aleksey Makarov
 * @since 1.0.5
 */
public class ClassSignatureParser {

    private final AnnotationParser annotationParser;

    private final GenericTypeParser genericTypeParser;

    private final IndentParser indentParser;

    private final ModifierParser modifierParser;

    private final ClassTypeParser classTypeParser;

    private final ClassNameParser classNameParser;

    private final InheritanceParser inheritanceParser;

    private final ReflectionParserManager manager;

    public ClassSignatureParser(AnnotationParser annotationParser,
                                GenericTypeParser genericTypeParser,
                                IndentParser indentParser,
                                ModifierParser modifierParser,
                                ClassTypeParser classTypeParser,
                                ClassNameParser classNameParser,
                                InheritanceParser inheritanceParser,
                                ReflectionParserManager manager) {
        this.annotationParser = annotationParser;
        this.genericTypeParser = genericTypeParser;
        this.indentParser = indentParser;
        this.modifierParser = modifierParser;
        this.classTypeParser = classTypeParser;
        this.classNameParser = classNameParser;
        this.inheritanceParser = inheritanceParser;
        this.manager = manager;
    }

    /**
     * Parses signature for class
     * Include annotations, modifiers, type, name, generics and inheritances
     *
     * @param clazz any class
     * @return parsed signature of class
     */
    public String parseClassSignature(Class<?> clazz) {
        String annotations = annotationParser.parseAnnotationsAsBlockAboveClass(clazz);
        String indent = indentParser.getIndent(clazz);
        String modifiers = modifierParser.parseModifiers(clazz);
        String name = classNameParser.parseTypeName(clazz);
        String classType = classTypeParser.parseClassType(clazz);
        String generics = genericTypeParser.parseGenerics(clazz);
        String inheritances = inheritanceParser.parseInheritances(clazz);

        String content = manager.joinNotEmptyContentBySpace(modifiers, classType, name);
        String specialContent = manager.joinNotEmptyContentBySpace(generics, inheritances);

        if (generics.isEmpty()) {
            return annotations + indent + manager.joinNotEmptyContentBySpace(content, specialContent);
        } else {
            return annotations + indent + content + specialContent;
        }
    }
}
