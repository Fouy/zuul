package com.moguhu.zuul;

import com.moguhu.zuul.context.RequestContext;
import com.moguhu.zuul.exception.ZuulException;
import com.moguhu.zuul.http.HttpServletRequestWrapper;
import com.moguhu.zuul.http.HttpServletResponseWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * This class initializes servlet requests and responses into the RequestContext and wraps the FilterProcessor calls
 * to preRoute(), route(),  postRoute(), and error() methods
 */
public class ZuulRunner {

    private boolean bufferRequests;

    /**
     * Creates a new <code>ZuulRunner</code> instance.
     */
    public ZuulRunner() {
        this.bufferRequests = true;
    }

    /**
     * @param bufferRequests - whether to wrap the ServletRequest in HttpServletRequestWrapper and buffer the body.
     */
    public ZuulRunner(boolean bufferRequests) {
        this.bufferRequests = bufferRequests;
    }

    /**
     * sets HttpServlet request and HttpResponse
     *
     * @param servletRequest
     * @param servletResponse
     */
    public void init(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {

        RequestContext ctx = RequestContext.getCurrentContext();
        if (bufferRequests) {
            ctx.setRequest(new HttpServletRequestWrapper(servletRequest));
        } else {
            ctx.setRequest(servletRequest);
        }

        ctx.setResponse(new HttpServletResponseWrapper(servletResponse));
    }

    /**
     * executes "post" filterType  ZuulFilters
     *
     * @throws ZuulException
     */
    public void postRoute() throws ZuulException {
        FilterProcessor.getInstance().postRoute();
    }

    /**
     * executes "route" filterType  ZuulFilters
     *
     * @throws ZuulException
     */
    public void route() throws ZuulException {
        FilterProcessor.getInstance().route();
    }

    /**
     * executes "pre" filterType  ZuulFilters
     *
     * @throws ZuulException
     */
    public void preRoute() throws ZuulException {
        FilterProcessor.getInstance().preRoute();
    }

    /**
     * executes "error" filterType  ZuulFilters
     */
    public void error() {
        FilterProcessor.getInstance().error();
    }

}