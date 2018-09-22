package com.moguhu.zuul.context;

import javax.servlet.*;
import java.io.IOException;

/**
 * Manages Zuul <code>RequestContext</code> lifecycle.
 *
 * @author mhawthorne
 */
public class ContextLifecycleFilter implements Filter {

    public void destroy() {
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(req, res);
        } finally {
            RequestContext.getCurrentContext().unset();
        }
    }

}
