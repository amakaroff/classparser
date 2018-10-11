package com.classparser.bytecode.decompile.fernflower.configuration;

import com.classparser.bytecode.decompile.fernflower.FernflowerDecompiler;
import com.classparser.configuration.Configuration;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IIdentifierRenamer;
import org.jetbrains.java.decompiler.modules.renamer.ConverterHelper;

/**
 * Interface for builder configuration for {@link FernflowerDecompiler}
 */
public interface FernflowerConfiguration extends Configuration {

    /**
     * Displays bridge methods
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    FernflowerConfiguration displayBridgeMethods(boolean flag);

    /**
     * Displays synthetic class members
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    FernflowerConfiguration displayMemberSyntheticClasses(boolean flag);

    /**
     * Decompiles inner classes
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    FernflowerConfiguration decompileInnerClasses(boolean flag);

    /**
     * Collapses 1.4 class references
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    FernflowerConfiguration collapseClassReferences(boolean flag);

    /**
     * Decompiles assertions
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    FernflowerConfiguration decompileAssertions(boolean flag);

    /**
     * Displays empty super invocation
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    FernflowerConfiguration displayEmptySuperInvocation(boolean flag);

    /**
     * Displays empty default constructor
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    FernflowerConfiguration displayEmptyDefaultConstructor(boolean flag);

    /**
     * Decompiles generic signatures
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    FernflowerConfiguration decompileGenericSignatures(boolean flag);

    /**
     * Assumes return not throwing exceptions
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    FernflowerConfiguration assumeReturnNotThrowingExceptions(boolean flag);

    /**
     * Decompiles enumerations to class
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    FernflowerConfiguration decompileEnumerations(boolean flag);

    /**
     * Removes getClass() invocation, when it is part of a qualified new statement
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    FernflowerConfiguration removeGetClassInvocation(boolean flag);

    /**
     * Displays output numeric literals "as-is"
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    FernflowerConfiguration displayOutputNumericLiterals(boolean flag);

    /**
     * Encodes non-ASCII characters in string and character literals as Unicode escapes
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    FernflowerConfiguration encodeNonASCIICharacters(boolean flag);

    /**
     * Interprets int 1 as boolean true (workaround to a compiler bug)
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    FernflowerConfiguration interpretInt1AsBooleanTrue(boolean flag);

    /**
     * Allows for set synthetic attribute (workaround to a compiler bug)
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    FernflowerConfiguration allowForSetSyntheticAttribute(boolean flag);

    /**
     * Considers nameless types as java.lang.Object (workaround to a compiler architecture flaw)
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    FernflowerConfiguration considerNamelessTypes(boolean flag);

    /**
     * Reconstructs variable names from debug information, if present
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    FernflowerConfiguration reconstructVariableNamesFromDebugInformation(boolean flag);

    /**
     * Removes empty exception ranges
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    FernflowerConfiguration removeEmptyExceptionRanges(boolean flag);

    /**
     * De-inlines finally structures
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    FernflowerConfiguration deInlineFinallyStructures(boolean flag);

    /**
     * Maximum allows processing time per decompiled method, in seconds. 0 means no upper limit
     * <p>
     * Default value: non limited
     *
     * @param limit limit in ms for decompile process
     * @return builder instance
     */
    FernflowerConfiguration setUpperLimitForDecompilation(int limit);

    /**
     * Renames ambiguous (resp. obfuscated) classes and class elements
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    FernflowerConfiguration renameAmbiguousClassesAndClassElements(boolean flag);

    /**
     * Full names of user-supplied class implementing IIdentifierRenamer.
     * It is used to determine which class identifiers should be renamed and provides
     * new identifier names (see "Renaming identifiers")
     * <p>
     * Default value:
     *
     * @param renamer deobfuscator instance
     * @return builder instance
     * @see ConverterHelper
     */
    FernflowerConfiguration setNewIIdentifierRenamer(Class<? extends IIdentifierRenamer> renamer);

    /**
     * Checks for IntelliJ IDEA-specific @NotNull annotation and remove inserted code if found
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    FernflowerConfiguration checkNonNullAnnotation(boolean flag);

    /**
     * Decompiles lambda expressions to anonymous classes
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    FernflowerConfiguration decompileLambdaExpressionsToAnonymousClasses(boolean flag);

    /**
     * Defines new line character to be used for output.
     * '\r\n' (Windows),
     * '\n' (Unix)
     * <p>
     * Default value: selected by OS
     *
     * @param character line separator
     * @return builder instance
     */
    FernflowerConfiguration defineLineSeparator(String character);

    /**
     * Indentations string
     * <p>
     * Default value: 4 spaces
     *
     * @param indent count indent spaces uses in decompile process
     * @return builder instance
     */
    FernflowerConfiguration setCountIndentSpaces(int indent);

    /**
     * Sets logging level, possible values are TRACE, INFO, WARN, ERROR
     * <p>
     * Default value: ERROR
     *
     * @param level log level uses in fernflower decompiler
     * @return builder instance
     */
    FernflowerConfiguration setLogLevel(IFernflowerLogger.Severity level);
}