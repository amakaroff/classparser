package com.classparser.bytecode.api;

import com.classparser.bytecode.BytecodeParser;
import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.bytecode.decompile.fernflower.FernflowerDecompiler;

import java.util.Collection;

/**
 * Interface provides methods for bytecode decompilation
 * {@link BytecodeParser} by default uses {@link FernflowerDecompiler}
 * Before decompilation will call method #setConfigurationManager
 * and #setConfiguration for setting parser configuration into decompiler
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public interface Decompiler {

    /**
     * Performs process of decompiling bytecode and
     * return decompilation code
     *
     * @param bytecode - bytecode of class
     * @return decompiling bytecode
     */
    String decompile(byte[] bytecode);

    /**
     * Process of decompiling bytecode with inner classes
     *
     * @param bytecode - bytecode of class
     * @param classes  - bytecode of inner classes
     * @return decompiling bytecode
     */
    String decompile(byte[] bytecode, Collection<byte[]> classes);

    /**
     * Sets instance of {@link ConfigurationManager} into decompiler
     *
     * @param configurationManager current configuration manager
     */
    void setConfigurationManager(ConfigurationManager configurationManager);
}