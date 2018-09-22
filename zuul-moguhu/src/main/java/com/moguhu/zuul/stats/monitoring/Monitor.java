package com.moguhu.zuul.stats.monitoring;

/**
 * Interface to register a counter to monitor
 */
public interface Monitor {
    /**
     * Implement this to add this Counter to a Registry
     *
     * @param monitorObj
     */
    void register(NamedCount monitorObj);
}
