package com.classparser.bytecode.decompile.cfr.configuration;

import com.classparser.bytecode.decompile.cfr.CFRDecompiler;
import com.classparser.configuration.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Builder configuration for {@link CFRDecompiler}
 */
public interface CFRConfiguration extends Configuration {

    List<String> INT_OPTIONS = Arrays.asList(
            "showops",
            "recpass",
            "renamesmallmembers",
            "aggressivesizethreshold"
    );

    List<String> STRING_OPTIONS = Collections.singletonList(
            "importfilter"
    );

    /**
     * Convert new Stringbuffer().add.add.add to string + string + string
     * See http://www.benf.org/other/cfr/stringbuilder-vs-concatenation.html
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration replaceStringConcatToStringBuffer(boolean flag);

    /**
     * Convert new StringBuilder().add.add.add to string + string + string
     * See http://www.benf.org/other/cfr/stringbuilder-vs-concatenation.html
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration replaceStringConcatToStringBuilder(boolean flag);

    /**
     * Convert usages of StringConcatFactor to string + string + string
     * See http://www.benf.org/other/cfr/java9stringconcat.html
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration replaceStringConcatFactorToStringConcatenation(boolean flag);

    /**
     * Re-sugar switch on enum
     * See http://www.benf.org/other/cfr/switch-on-enum.html
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration decompileSugarEnumInSwitch(boolean flag);

    /**
     * Re-sugar enums
     * See http://www.benf.org/other/cfr/how-are-enums-implemented.html
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration decompileSugarInEnums(boolean flag);

    /**
     * Re-sugar switch on String
     * See http://www.benf.org/other/cfr/java7switchonstring.html
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration decompileSugarStringInEnums(boolean flag);

    /**
     * Decompile preview features if class was compiled with (jdk --enable-preview).
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration decompileClassesWithPreviewFeatures(boolean flag);

    /**
     * Re-sugar switch expression.
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration decompileSwitchExpressions(boolean flag);

    /**
     * Re-sugar array based iteration.
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration decompileSugarInArrayIteration(boolean flag);

    /**
     * Reconstruct try-with-resources code construction
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration decompilerTryWithResourceConstruction(boolean flag);

    /**
     * Re-sugar collection based iteration
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration decompileSugarInCollectionIteration(boolean flag);

    /**
     * Re-build lambda functions
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration decompileLambdaFunctions(boolean flag);

    /**
     * Decompile inner classes
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration decompileInnerClasses(boolean flag);

    /**
     * When processing many files, skip inner classes,
     * as they will be processed as part of outer classes anyway.
     * If false, you will see inner classes as separate entities as well.
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration skipBatchInnerClasses(boolean flag);

    /**
     * Hide UTF8 characters - quote them instead of showing the raw characters
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration hideUTF8Characters(boolean flag);

    /**
     * Hide very long strings - useful if obfuscator have placed fake code in strings
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration hideVeryLongStrings(boolean flag);

    /**
     * Remove boilerplate functions - constructor boilerplate, lambda deserialization etc
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration removeBoilerplateFunctions(boolean flag);

    /**
     * Remove (where possible) implicit outer class references in inner classes
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration removeInnerClassesSynthetics(boolean flag);

    /**
     * Hide bridge methods
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration hideBridgeMethods(boolean flag);

    /**
     * Relink constant strings - if there is a local reference
     * to a string which matches a static final, use the static final.
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration relinkConstString(boolean flag);

    /**
     * Lift initialisation code common to all constructors into member initialisation
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration liftInitialisationToAllConstructors(boolean flag);

    /**
     * Remove pointless methods - default constructor etc
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration removeDeadMethods(boolean flag);

    /**
     * Hide generics where we've obviously got it wrong, and fallback to non-generic
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration removeBadGenerics(boolean flag);

    /**
     * Re-sugar assert calls
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration decompileSugarInAsserts(boolean flag);

    /**
     * Where possible, remove pointless boxing wrappers
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration decompileBoxing(boolean flag);

    /**
     * Show CFR version used in the header (handy to turn off when regression testing)
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration showCFRVersion(boolean flag);

    /**
     * Re-sugar finally statements
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration decompileSugarInFinally(boolean flag);

    /**
     * Remove support code for monitors - eg catch blocks just to exit a monitor
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration removeSupportCodeForMonitors(boolean flag);

    /**
     * Replace monitors with comments - useful if we're completely confused
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration replaceMonitorWithComments(boolean flag);

    /**
     * Be a bit more lenient in situations where we'd normally throw an exception
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration lenientSituationsWhereThrowException(boolean flag);

    /**
     * Dump class path for debugging purposes
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration dumpClassPathForDebuggingPurposes(boolean flag);

    /**
     * Output comments describing decompiler status, fallback flags etc
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration showDecompilerMessages(boolean flag);

    /**
     * Force basic block sorting.  Usually not necessary for code emitted directly from javac,
     * but required in the case of obfuscation (or dex2jar!).  Will be enabled in recovery.
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration forceBasicBlockSorting(boolean flag);

    /**
     * Allow for loops to aggressively roll mutations into update section,
     * even if they don't appear to be involved with the predicate
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration allowForLoopsToAggressivelyRollMutations(boolean flag);

    /**
     * Force extra aggressive top sort options
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration forceTopSortAggressive(boolean flag);

    /**
     * Force top sort not to pull try blocks
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration forceTopSortNoPull(boolean flag);

    /**
     * Pull results of deterministic jumps back through some constant assignments
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration forceCodePropagate(boolean flag);

    /**
     * Move return up to jump site
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration forceReturnIngifs(boolean flag);

    /**
     * Drop exception information (WARNING : changes semantics, dangerous!)
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration ignoreExceptionsAlways(boolean flag);

    /**
     * Drop exception information if completely stuck (WARNING : changes semantics, dangerous!)
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration ignoreExceptions(boolean flag);

    /**
     * Try to extend and merge exceptions more aggressively
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration forceExceptionPrune(boolean flag);

    /**
     * Remove nested exception handlers if they don't change semantics
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration removeNestedExceptionsHandlers(boolean flag);

    /**
     * Split lifetimes where analysis caused type clash
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration splitLifetimesAnalysisCausedType(boolean flag);

    /**
     * Recover type hints for iterators from first pass.
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration recoverTypeHintsForIterators(boolean flag);

    /**
     * Don't display state while decompiling
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration doNotDisplayStateWhile(boolean flag);

    /**
     * Allow more and more aggressive options to be set if decompilation fails
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration allowMoreAggressiveOptions(boolean flag);

    /**
     * Enable transformations to handle eclipse code better
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration enableEclipseTransformations(boolean flag);

    /**
     * Generate @Override annotations
     * (if method is seen to implement interface method, or override a base class method)
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration generateOverrideAnnotations(boolean flag);

    /**
     * Decorate methods with explicit types if not implied by arguments.
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration decorateMethodsWithExplicitTypes(boolean flag);

    /**
     * Allow transformations which correct errors, potentially at the cost of altering emitted code behaviour.
     * An example would be removing impossible (in java!) exception handling - if this has any effect,
     * a warning will be emitted.
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration allowTransformationsWhichCorrectErrors(boolean flag);

    /**
     * Allow code to be emitted which uses labelled blocks, (handling odd forward gotos)
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration allowCodeUsesLabelledBlocks(boolean flag);

    /**
     * Reverse java 1.4 class object construction
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration reverseOldJavaClassObjectConstruction(boolean flag);

    /**
     * Hide imports from java.lang.
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration hideDefaultImports(boolean flag);

    /**
     * Decompile specifically with recovery options from pass #X.
     * (really only useful for debugging)
     * <p>
     * Default value: 0
     *
     * @param debug debug indexes
     * @return builder instance
     */
    CFRConfiguration decompileSpecificallyWithRecoveryOptions(int debug);

    /**
     * Synonym for 'renamedupmembers' + 'renameillegalidents' + 'renameenummembers'
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration renameAll(boolean flag);

    /**
     * Rename ambiguous/duplicate fields.
     * Note - this WILL break reflection based access, so is not automatically enabled.
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration renameDuplicateFields(boolean flag);

    /**
     * Rename small members.
     * Note - this WILL break reflection based access, so is not automatically enabled.
     * <p>
     * Default value: 0
     *
     * @param rename rename mask
     * @return builder instance
     */
    CFRConfiguration renameSmallMembers(int rename);

    /**
     * Rename identifiers which are not valid java identifiers.
     * Note - this WILL break reflection based access, so is not automatically enabled.
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration renameInvalidIdentifiers(boolean flag);

    /**
     * Rename ENUM identifiers which do not match their 'expected' string names.
     * Note - this WILL break reflection based access, so is not automatically enabled.
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration renameEnumIdentifiers(boolean flag);

    /**
     * Opcode count at which to trigger aggressive reductions
     * <p>
     * Default value: 15000
     *
     * @param opcode opcode count
     * @return builder instance
     */
    CFRConfiguration countAtWhichToTriggerAggressiveReductions(int opcode);

    /**
     * Try to remove return from a static init
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration removeReturnFromStaticInit(boolean flag);

    /**
     * Use local variable name table if present
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration useLocalVariableTableIfExits(boolean flag);

    /**
     * Pull code into case statements aggressively
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration pullCodeIntoCaseStatements(boolean flag);

    /**
     * Elide things which aren't helpful in scala output (serialVersionUID, @ScalaSignature)
     * <p>
     * Default value: true
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration elideThingsInScalaOutput(boolean flag);

    /**
     * Be more aggressive about uncaching in order to reduce memory footprint.
     * <p>
     * Default value: false
     *
     * @param flag true/false value
     * @return builder instance
     */
    CFRConfiguration setLowMemoryMode(boolean flag);

    /**
     * Substring regex - import classes only when fqn matches this pattern.
     * (VNegate with !, eg !lang)
     * <p>
     * Default value: empty string
     *
     * @param regex the regular expression for filtration
     * @return builder instance
     */
    CFRConfiguration setImportFilter(String regex);
}