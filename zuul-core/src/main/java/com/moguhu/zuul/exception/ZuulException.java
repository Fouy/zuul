package com.moguhu.zuul.exception;

import com.moguhu.zuul.monitoring.CounterFactory;

/**
 * All handled exceptions in Zuul are ZuulExceptions
 *
 * @author Mikey Cohen
 *         Date: 10/20/11
 *         Time: 4:33 PM
 */
public class ZuulException extends Exception {
    public int nStatusCode;
    public String errorCause;

    /**
     * Source Throwable, message, status code and info about the cause
     *
     * @param throwable
     * @param sMessage
     * @param nStatusCode
     * @param errorCause
     */
    public ZuulException(Throwable throwable, String sMessage, int nStatusCode, String errorCause) {
        super(sMessage, throwable);
        this.nStatusCode = nStatusCode;
        this.errorCause = errorCause;
        incrementCounter("ZUUL::EXCEPTION:" + errorCause + ":" + nStatusCode);
    }

    /**
     * error message, status code and info about the cause
     *
     * @param sMessage
     * @param nStatusCode
     * @param errorCause
     */
    public ZuulException(String sMessage, int nStatusCode, String errorCause) {
        super(sMessage);
        this.nStatusCode = nStatusCode;
        this.errorCause = errorCause;
        incrementCounter("ZUUL::EXCEPTION:" + errorCause + ":" + nStatusCode);
    }

    /**
     * Source Throwable,  status code and info about the cause
     *
     * @param throwable
     * @param nStatusCode
     * @param errorCause
     */
    public ZuulException(Throwable throwable, int nStatusCode, String errorCause) {
        super(throwable.getMessage(), throwable);
        this.nStatusCode = nStatusCode;
        this.errorCause = errorCause;
        incrementCounter("ZUUL::EXCEPTION:" + errorCause + ":" + nStatusCode);
    }

    private static final void incrementCounter(String name) {
        CounterFactory.instance().increment(name);
    }

}
