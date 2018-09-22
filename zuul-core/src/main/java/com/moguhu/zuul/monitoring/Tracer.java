package com.moguhu.zuul.monitoring;

/**
 * Time based monitoring metric.
 *
 * @author mhawthorne
 */
public interface Tracer {

    /**
     * Stops and Logs a time based tracer
     */
    void stopAndLog();

    /**
     * Sets the name for the time based tracer
     *
     * @param name a <code>String</code> value
     */
    void setName(String name);

}
