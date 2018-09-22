package com.moguhu.zuul;

/**
 * Interface to implement for registering a callback for each time a filter
 */
public interface FilterUsageNotifier {

    void notify(ZuulFilter filter, ExecutionStatus status);

}
