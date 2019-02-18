package com.classparser.reflection.configuration.api;

/**
 * Module private interface for cleaning any parsers and run after parsing of base class
 *
 * @author Aleksey Makarov
 * @since 1.0.3
 */
public interface Clearance {

    void clear();
}