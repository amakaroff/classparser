package com.classparser.reflection.parser.structure;

import com.classparser.reflection.ParseContext;
import com.classparser.reflection.configuration.ConfigurationManager;

import java.util.Set;
import java.util.TreeSet;

/**
 * Class provides functionality by storing information about all classes used in
 * based parsed class for collecting and build import section
 *
 * @author Aleksey Makarov
 * @since 1.0.0
 */
public class ImportParser {

    private static final String DEFAULT_JAVA_PACKAGE = "java.lang";

    private final ConfigurationManager configurationManager;

    public ImportParser(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    /**
     * Appends class for import section and checks can be this class added
     *
     * @param classForImport any class contains in meta information of based parsed class
     * @param context        context of parsing class process
     * @return true if class added to import section
     */
    public boolean tryAddToImport(Class<?> classForImport, ParseContext context) {
        classForImport = resolveClass(classForImport);

        if (!configurationManager.isDisplayImports() || isNeedFullName(classForImport, context)) {
            return false;
        } else {
            context.addImportClass(classForImport);
            return true;
        }
    }

    /**
     * Build import section for current parsed context
     *
     * @param context context of parsing class process
     * @return string line with information import classes for based parsed class
     */
    public String getImports(ParseContext context) {
        Set<String> imports = new TreeSet<>();
        String lineSeparator = configurationManager.getLineSeparator();

        for (Class<?> clazz : context.getImportClasses()) {
            if (isAppendToImports(clazz, context)) {
                imports.add("import " + clazz.getName() + ';' + lineSeparator);
            }
        }

        return imports.isEmpty() ? "" : String.join("", imports) + lineSeparator;
    }

    /**
     * Obtains the simple name of class
     *
     * @param clazz any class
     * @return class simple name
     */
    public String getSimpleName(Class<?> clazz) {
        String typeName = clazz.getSimpleName();
        if (typeName.isEmpty()) {
            typeName = clazz.getName();
            if (typeName.contains(".")) {
                typeName = typeName.substring(typeName.lastIndexOf('.') + 1);
            }
        }
        return typeName;
    }

    /**
     * Checks if for this class should be displayed full name
     *
     * @param classForImport any class
     * @param context        context of parsing class process
     * @return true if for class necessary full name displayed
     */
    private boolean isNeedFullName(Class<?> classForImport, ParseContext context) {
        Set<Class<?>> classes = context.getImportClasses();
        for (Class<?> clazz : classes) {
            if (areEqualBySimpleName(clazz, classForImport) && !areEqualByName(clazz, classForImport)) {
                return !classes.contains(classForImport);
            }
        }

        return false;
    }

    /**
     * Checking class for append to import section
     *
     * @param clazz   any class
     * @param context context of parsing class process
     * @return true if these class can be appended to import
     */
    private boolean isAppendToImports(Class<?> clazz, ParseContext context) {
        return !clazz.isPrimitive()
                && !DEFAULT_JAVA_PACKAGE.equals(getPackageName(clazz))
                && context.getBaseParsedClass().getPackage() != clazz.getPackage();
    }

    /**
     * Obtains package name
     *
     * @param clazz any class
     * @return name of package for class or empty string of class have not package
     */
    private String getPackageName(Class<?> clazz) {
        return clazz.getPackage() != null ? clazz.getPackage().getName() : "";
    }

    /**
     * Equal classes by full name
     *
     * @param source one class
     * @param target other class
     * @return true if full name of classes is equal
     */
    private boolean areEqualByName(Class<?> source, Class<?> target) {
        return source.getName().equals(target.getName());
    }

    /**
     * Equal classes by simple name
     *
     * @param source one class
     * @param target other class
     * @return true if simple name of classes is equal
     */
    private boolean areEqualBySimpleName(Class<?> source, Class<?> target) {
        return getSimpleName(source).equals(getSimpleName(target));
    }

    /**
     * Resolves class for obtaining correct import information about this class
     *
     * @param clazz any class
     * @return corrected to import class
     */
    private Class<?> resolveClass(Class<?> clazz) {
        if (clazz.isArray()) {
            clazz = resolveArray(clazz);
        }

        if (clazz.isMemberClass()) {
            clazz = resolveMemberClass(clazz);
        }

        return clazz;
    }

    /**
     * Obtains type of array
     *
     * @param clazz any array type
     * @return type of array
     */
    private Class<?> resolveArray(Class<?> clazz) {
        return clazz.isArray() ? resolveArray(clazz.getComponentType()) : clazz;
    }

    /**
     * Obtains top enclosing class
     *
     * @param clazz any class
     * @return top enclosing class
     */
    private Class<?> resolveMemberClass(Class<?> clazz) {
        return clazz.isMemberClass() ? resolveMemberClass(clazz.getEnclosingClass()) : clazz;
    }
}