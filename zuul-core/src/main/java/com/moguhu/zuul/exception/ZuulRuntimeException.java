package com.moguhu.zuul.exception;

import org.springframework.http.HttpStatus;

public class ZuulRuntimeException extends RuntimeException {

    public ZuulRuntimeException(ZuulException cause) {
        super(cause);
    }

    public ZuulRuntimeException(Exception ex) {
        this(new ZuulException(ex, HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
    }
}
