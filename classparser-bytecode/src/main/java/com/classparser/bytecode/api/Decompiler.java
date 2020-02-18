package com.classparser.bytecode.api;

import com.classparser.bytecode.ByteCodeParser;
import com.classparser.bytecode.configuration.ConfigurationManager;
import com.classparser.bytecode.decompile.fernflower.FernflowerDecompiler;

import java.util.Collection;

/**
 * Interface provides methods for byte code decompilation
 * {@link ByteCodeParser} by default uses {@link FernflowerDecompiler}
 * Before decompilation will call method #setConfigurationManager
 * and #setConfiguration for setting parser configuration into decompiler
 *
 * @author Aleksei Makarov
 * @since 1.0.0
 */
public interface Decompiler {

    /**
     * Performs process of decompiling byte code and
     * return decompilation code
     *
     * @param byteCode - byte code of class
     * @return decompiling byte code
     */
    String decompile(byte[] byteCode);

    /**
     * Process of decompiling byte code with inner classes
     *
     * @param byteCode               - byte code of class
     * @param nestedClassesByteCodes - byte code of inner classes
     * @return decompiling byte code
     */
    String decompile(byte[] byteCode, Collection<byte[]> nestedClassesByteCodes);

    /**
     * Sets instance of {@link ConfigurationManager} into decompiler
     *
     * @param configurationManager current configuration manager
     */
    void setConfigurationManager(ConfigurationManager configurationManager);
}