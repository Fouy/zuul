package com.moguhu.zuul.component.route;

import com.moguhu.zuul.ZuulFilter;
import com.moguhu.zuul.context.RequestContext;
import org.springframework.util.ReflectionUtils;

import javax.servlet.RequestDispatcher;

import static com.moguhu.zuul.constants.FilterConstants.*;

/**
 * Route {@link ZuulFilter} that forwards requests using the {@link RequestDispatcher}.
 * Forwarding location is located in the {@link RequestContext} attribute {@link FilterConstants#FORWARD_TO_KEY}.
 * Useful for forwarding to endpoints in the current application.
 */
public class SendForwardFilter extends ZuulFilter {

    protected static final String SEND_FORWARD_FILTER_RAN = "sendForwardFilter.ran";

    @Override
    public String filterType() {
        return ROUTE_TYPE;
    }

    @Override
    public int filterOrder() {
        return SEND_FORWARD_FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        return ctx.containsKey(FORWARD_TO_KEY)
                && !ctx.getBoolean(SEND_FORWARD_FILTER_RAN, false);
    }

    @Override
    public Object run() {
        try {
            RequestContext ctx = RequestContext.getCurrentContext();
            String path = (String) ctx.get(FORWARD_TO_KEY);
            RequestDispatcher dispatcher = ctx.getRequest().getRequestDispatcher(path);
            if (dispatcher != null) {
                ctx.set(SEND_FORWARD_FILTER_RAN, true);
                if (!ctx.getResponse().isCommitted()) {
                    dispatcher.forward(ctx.getRequest(), ctx.getResponse());
                    ctx.getResponse().flushBuffer();
                }
            }
        } catch (Exception ex) {
            ReflectionUtils.rethrowRuntimeException(ex);
        }
        return null;
    }

}
