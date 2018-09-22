package com.moguhu.zuul.stats.monitoring;

/**
 * Interface for a named counter
 */
public interface NamedCount {

    String getName();

    long getCount();

}