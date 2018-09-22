package com.moguhu.zuul;

import java.io.File;


/**
 * Interface to generate Classes from source code
 * User: mcohen
 * Date: 5/30/13
 * Time: 11:35 AM
 */
public interface DynamicCodeCompiler {
    Class compile(String sCode, String sName) throws Exception;

    Class compile(File file) throws Exception;
}
