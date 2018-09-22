package com.moguhu.zuul.groovy;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Filters only .groovy files
 *
 * @author Mikey Cohen
 *         Date: 5/30/13
 *         Time: 11:47 AM
 */
public class GroovyFileFilter implements FilenameFilter {

    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith(".groovy");
    }

}