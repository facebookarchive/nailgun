package com.martiansoftware.nailgun;

/** Allows providing a instance (non-static) main method.
 *  Potentially helpful for users of JVM languages other than Java.
 * 
 *  Implementors of this interface MUST provide a public, no-args constructor. */
public interface NonStaticNail {
    
    public void nailMain(String[] args);
    
}