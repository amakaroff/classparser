package com.classparser.reflection.parser.imports;

import com.classparser.reflection.configuration.ConfigurationManager;
import com.classparser.reflection.configuration.ReflectionParserManager;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Class provides functionality by storing information about all classes used in
 * based parsed class for collecting and build import section
 * This class depend on context {@link ReflectionParserManager} of parsing
 *
 * @author Aleksey Makarov
 * @since 1.0.0
 */
public final class ImportParser {

    private static final String DEFAULT_JAVA_PACKAGE = "java.lang";

    private final ThreadLocal<Set<Class<?>>> threadLocalClassesForImport;

    private final ReflectionParserManager manager;

    private final ConfigurationManager configurationManager;

    public ImportParser(ReflectionParserManager manager) {
        this.manager = manager;
        this.configurationManager = manager.getConfigurationManager();
        this.threadLocalClassesForImport = new ThreadLocal<>();
        this.threadLocalClassesForImport.set(getImportClasses());
    }

    /**
     * Obtain {@link Set} for store imported classes
     *
     * @return import classes storage
     */
    private Set<Class<?>> getImportClasses() {
        Set<Class<?>> classes = threadLocalClassesForImport.get();
        if (classes == null) {
            classes = new HashSet<>();
            threadLocalClassesForImport.set(classes);
        }

        return classes;
    }

    /**
     * Initialize import parser
     */
    public void initBeforeParsing() {
        if (manager.getBaseParsedClass() == manager.getCurrentParsedClass()) {
            getImportClasses().addAll(getAllInnerAndNestedClasses(manager.getBaseParsedClass()));
        }
    }

    /**
     * Loads all inner and nested classes to set for any class
     *
     * @param clazz any class
     * @return set of all inner and nested classes
     */
    private Set<Class<?>> getAllInnerAndNestedClasses(Class<?> clazz) {
        Set<Class<?>> classes = new HashSet<>();

        classes.add(clazz);
        for (Class<?> innerClass : clazz.getDeclaredClasses()) {
            classes.addAll(getAllInnerAndNestedClasses(innerClass));
        }

        return classes;
    }

    /**
     * Appends class for import section and checks can be this class added
     *
     * @param classForImport any class contains in meta information of based parsed class
     * @return true if class added to import section
     */
    public boolean addToImportSection(Class<?> classForImport) {
        if (manager.getBaseParsedClass() != null) {
            classForImport = resolveClass(classForImport);

            if (!configurationManager.isEnabledImports() || isNeedFullName(classForImport)) {
                return false;
            } else {
                getImportClasses().add(classForImport);
                return true;
            }
        }

        return false;
    }

    /**
     * Build import section for current parsed context
     *
     * @return string line with information import classes for based parsed class
     */
    public String getImports() {
        Set<String> imports = new TreeSet<>();
        String lineSeparator = configurationManager.getLineSeparator();

        for (Class<?> clazz : getImportClasses()) {
            if (isAppendToImports(clazz)) {
                imports.add("import " + clazz.getName() + ';' + lineSeparator);
            }
        }

        return imports.isEmpty() ? "" : String.join("", imports) + lineSeparator;
    }

    /**
     * Clear current import context
     */
    public void tearDownAfterParsing() {
        this.threadLocalClassesForImport.remove();
    }

    /**
     * Checks if for this class should be displayed full name
     *
     * @param classForImport any class
     * @return true if for class necessary full name displayed
     */
    private boolean isNeedFullName(Class<?> classForImport) {
        Set<Class<?>> classes = getImportClasses();
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
     * @param clazz any class
     * @return true if this class can be append to import
     */
    private boolean isAppendToImports(Class<?> clazz) {
        return !clazz.isPrimitive()
                && !DEFAULT_JAVA_PACKAGE.equals(getPackageName(clazz))
                && manager.getBaseParsedClass().getPackage() != clazz.getPackage();
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
     * Equals classes by full name
     *
     * @param source one class
     * @param target other class
     * @return true if full name of classes is equal
     */
    private boolean areEqualByName(Class<?> source, Class<?> target) {
        return source.getName().equals(target.getName());
    }

    /**
     * Equals classes by simple name
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

    /**
     * Obtains simple name of class
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
}