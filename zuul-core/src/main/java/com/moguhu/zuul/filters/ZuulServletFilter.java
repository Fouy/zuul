package com.moguhu.zuul.filters;


import com.moguhu.zuul.ZuulRunner;
import com.moguhu.zuul.context.RequestContext;
import com.moguhu.zuul.exception.ZuulException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Zuul Servlet filter to run Zuul within a Servlet Filter. The filter invokes pre-routing filters first,
 * then routing filters, then post routing filters. Handled exceptions in pre-routing and routing
 * call the error filters, then call post-routing filters. Errors in post-routing only invoke the error filters.
 * Unhandled exceptions only invoke the error filters
 *
 * @author Mikey Cohen
 *         Date: 10/12/11
 *         Time: 2:54 PM
 */
public class ZuulServletFilter implements Filter {

    private ZuulRunner zuulRunner;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        String bufferReqsStr = filterConfig.getInitParameter("buffer-requests");
        boolean bufferReqs = bufferReqsStr != null && bufferReqsStr.equals("true") ? true : false;

        zuulRunner = new ZuulRunner(bufferReqs);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            init((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse);
            try {
                preRouting();
            } catch (ZuulException e) {
                error(e);
                postRouting();
                return;
            }

            // Only forward onto to the chain if a zuul response is not being sent
            if (!RequestContext.getCurrentContext().sendZuulResponse()) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }

            try {
                routing();
            } catch (ZuulException e) {
                error(e);
                postRouting();
                return;
            }
            try {
                postRouting();
            } catch (ZuulException e) {
                error(e);
                return;
            }
        } catch (Throwable e) {
            error(new ZuulException(e, 500, "UNCAUGHT_EXCEPTION_FROM_FILTER_" + e.getClass().getName()));
        } finally {
            RequestContext.getCurrentContext().unset();
        }
    }

    void postRouting() throws ZuulException {
        zuulRunner.postRoute();
    }

    void routing() throws ZuulException {
        zuulRunner.route();
    }

    void preRouting() throws ZuulException {
        zuulRunner.preRoute();
    }

    void init(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        zuulRunner.init(servletRequest, servletResponse);
    }

    void error(ZuulException e) {
        RequestContext.getCurrentContext().setThrowable(e);
        zuulRunner.error();
    }

    @Override
    public void destroy() {
    }

}
