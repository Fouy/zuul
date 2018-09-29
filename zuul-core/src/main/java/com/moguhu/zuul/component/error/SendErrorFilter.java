package com.moguhu.zuul.component.error;

import com.moguhu.zuul.ZuulFilter;
import com.moguhu.zuul.constants.FilterConstants;
import com.moguhu.zuul.context.RequestContext;
import com.moguhu.zuul.exception.ZuulException;
import com.moguhu.zuul.exception.ZuulRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 异常过滤器, 默认跳转至 /error
 */
public class SendErrorFilter extends ZuulFilter {

    private static final Log logger = LogFactory.getLog(SendErrorFilter.class);

    protected static final String SEND_ERROR_FILTER_RAN = "sendErrorFilter.ran";

    @Value("${error.path:/error}")
    private String errorPath;

    @Override
    public String filterType() {
        return FilterConstants.ERROR_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.SEND_ERROR_FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        // only forward to errorPath if it hasn't been forwarded to already
        return ctx.getThrowable() != null && !ctx.getBoolean(SEND_ERROR_FILTER_RAN, false);
    }

    @Override
    public Object run() {
        try {
            RequestContext ctx = RequestContext.getCurrentContext();
            ZuulException exception = findZuulException(ctx.getThrowable());
            HttpServletRequest request = ctx.getRequest();

            request.setAttribute("javax.servlet.error.status_code", exception.nStatusCode);

            logger.warn("Error during filtering", exception);
            request.setAttribute("javax.servlet.error.exception", exception);

            if (StringUtils.hasText(exception.errorCause)) {
                request.setAttribute("javax.servlet.error.message", exception.errorCause);
            }

            RequestDispatcher dispatcher = request.getRequestDispatcher(this.errorPath);
            if (dispatcher != null) {
                ctx.set(SEND_ERROR_FILTER_RAN, true);
                if (!ctx.getResponse().isCommitted()) {
                    ctx.setResponseStatusCode(exception.nStatusCode);
                    dispatcher.forward(request, ctx.getResponse());
                }
            }
        } catch (Exception ex) {
            ReflectionUtils.rethrowRuntimeException(ex);
        }
        return null;
    }

    ZuulException findZuulException(Throwable throwable) {
        if (throwable.getCause() instanceof ZuulRuntimeException) {
            // this was a failure initiated by one of the local filters
            return (ZuulException) throwable.getCause().getCause();
        }

        if (throwable.getCause() instanceof ZuulException) {
            // wrapped zuul exception
            return (ZuulException) throwable.getCause();
        }

        if (throwable instanceof ZuulException) {
            // exception thrown by zuul lifecycle
            return (ZuulException) throwable;
        }

        // fallback, should never get here
        return new ZuulException(throwable, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null);
    }

    public void setErrorPath(String errorPath) {
        this.errorPath = errorPath;
    }

}
