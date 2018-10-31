package com.classparser.bytecode.decompile.fernflower.configuration;

import com.classparser.bytecode.decompile.fernflower.FernflowerDecompiler;
import com.classparser.bytecode.utils.ClassNameConverter;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IIdentifierRenamer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class provides java API special builder for creating
 * configuration map for {@link FernflowerDecompiler}
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public class FernflowerBuilderConfiguration {

    /**
     * Creates instance of builder
     *
     * @return {@link FernflowerConfiguration} instance
     */
    public static FernflowerConfiguration createBuilder() {
        return new Builder();
    }

    /**
     * Inner builder class implements {@link FernflowerConfiguration}
     */
    private static class Builder implements FernflowerConfiguration {

        private static final String ZERO = "0";

        private static final String ONE = "1";

        private final Map<String, Object> configuration;

        private Builder() {
            this.configuration = new ConcurrentHashMap<>();
        }

        @Override
        public Map<String, Object> getConfiguration() {
            return configuration;
        }

        @Override
        public FernflowerConfiguration displayBridgeMethods(boolean flag) {
            configuration.put("rbr", flag ? ZERO : ONE);
            return this;
        }

        @Override
        public FernflowerConfiguration displayMemberSyntheticClasses(boolean flag) {
            configuration.put("rsy", flag ? ZERO : ONE);
            return this;
        }

        @Override
        public FernflowerConfiguration decompileInnerClasses(boolean flag) {
            configuration.put("din", flag ? ONE : ZERO);
            return this;
        }

        @Override
        public FernflowerConfiguration collapseClassReferences(boolean flag) {
            configuration.put("dc4", flag ? ONE : ZERO);
            return this;
        }

        @Override
        public FernflowerConfiguration decompileAssertions(boolean flag) {
            configuration.put("das", flag ? ONE : ZERO);
            return this;
        }

        @Override
        public FernflowerConfiguration displayEmptySuperInvocation(boolean flag) {
            configuration.put("hes", flag ? ZERO : ONE);
            return this;
        }

        @Override
        public FernflowerConfiguration displayEmptyDefaultConstructor(boolean flag) {
            configuration.put("hdc", flag ? ZERO : ONE);
            return this;
        }

        @Override
        public FernflowerConfiguration decompileGenericSignatures(boolean flag) {
            configuration.put("dgs", flag ? ONE : ZERO);
            return this;
        }

        @Override
        public FernflowerConfiguration assumeReturnNotThrowingExceptions(boolean flag) {
            configuration.put("ner", flag ? ONE : ZERO);
            return this;
        }

        @Override
        public FernflowerConfiguration decompileEnumerations(boolean flag) {
            configuration.put("den", flag ? ZERO : ONE);
            return this;
        }

        @Override
        public FernflowerConfiguration removeGetClassInvocation(boolean flag) {
            configuration.put("rgn", flag ? ONE : ZERO);
            return this;
        }

        @Override
        public FernflowerConfiguration displayOutputNumericLiterals(boolean flag) {
            configuration.put("lit", flag ? ONE : ZERO);
            return this;
        }

        @Override
        public FernflowerConfiguration encodeNonASCIICharacters(boolean flag) {
            configuration.put("asc", flag ? ONE : ZERO);
            return this;
        }

        @Override
        public FernflowerConfiguration interpretInt1AsBooleanTrue(boolean flag) {
            configuration.put("bto", flag ? ONE : ZERO);
            return this;
        }

        @Override
        public FernflowerConfiguration allowForSetSyntheticAttribute(boolean flag) {
            configuration.put("nns", flag ? ZERO : ONE);
            return this;
        }

        @Override
        public FernflowerConfiguration considerNamelessTypes(boolean flag) {
            configuration.put("uto", flag ? ONE : ZERO);
            return this;
        }

        @Override
        public FernflowerConfiguration reconstructVariableNamesFromDebugInformation(boolean flag) {
            configuration.put("udv", flag ? ONE : ZERO);
            return this;
        }

        @Override
        public FernflowerConfiguration removeEmptyExceptionRanges(boolean flag) {
            configuration.put("rer", flag ? ONE : ZERO);
            return this;
        }

        @Override
        public FernflowerConfiguration deInlineFinallyStructures(boolean flag) {
            configuration.put("fdi", flag ? ONE : ZERO);
            return this;
        }

        @Override
        public FernflowerConfiguration setUpperLimitForDecompilation(int limit) {
            configuration.put("mpm", String.valueOf(limit));
            return this;
        }

        @Override
        public FernflowerConfiguration renameAmbiguousClassesAndClassElements(boolean flag) {
            configuration.put("ren", flag ? ONE : ZERO);
            return this;
        }

        @Override
        public FernflowerConfiguration setNewIIdentifierRenamer(Class<? extends IIdentifierRenamer> renamer) {
            configuration.put("urc", ClassNameConverter.toJavaClassName(renamer));
            return this;
        }

        @Override
        public FernflowerConfiguration checkNonNullAnnotation(boolean flag) {
            configuration.put("inn", flag ? ONE : ZERO);
            return this;
        }

        @Override
        public FernflowerConfiguration decompileLambdaExpressionsToAnonymousClasses(boolean flag) {
            configuration.put("lac", flag ? ONE : ZERO);
            return this;
        }

        @Override
        public FernflowerConfiguration defineLineSeparator(String character) {
            if (character.equals("\n")) {
                configuration.put("nls", ONE);
            } else if (character.equals("\r\n")) {
                configuration.put("nls", ZERO);
            }

            return this;
        }

        @Override
        public FernflowerConfiguration setCountIndentSpaces(int indent) {
            StringBuilder builder = new StringBuilder();

            for (int index = 0; index < indent; index++) {
                builder.append(" ");
            }

            configuration.put("ind", builder.toString());
            return this;
        }

        @Override
        public FernflowerConfiguration setLogLevel(IFernflowerLogger.Severity level) {
            configuration.put("log", level.name());
            return this;
        }
    }
}