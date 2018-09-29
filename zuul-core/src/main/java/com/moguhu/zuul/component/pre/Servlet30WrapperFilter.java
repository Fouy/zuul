package com.moguhu.zuul.component.pre;

import com.moguhu.zuul.ZuulFilter;
import com.moguhu.zuul.constants.FilterConstants;
import com.moguhu.zuul.context.RequestContext;
import com.moguhu.zuul.http.HttpServletRequestWrapper;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;

/**
 * 前置过滤器, Servlet 3.0 兼容 wrapper, Zuul 默认只支持 Servlet 2.5.
 */
public class Servlet30WrapperFilter extends ZuulFilter {

    private Field requestField = null;

    public Servlet30WrapperFilter() {
        this.requestField = ReflectionUtils.findField(HttpServletRequestWrapper.class, "req", HttpServletRequest.class);
        Assert.notNull(this.requestField, "HttpServletRequestWrapper.req field not found");
        this.requestField.setAccessible(true);
    }

    protected Field getRequestField() {
        return this.requestField;
    }

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.SERVLET_30_WRAPPER_FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        return true; // TODO: only if in servlet 3.0 env
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        if (request instanceof HttpServletRequestWrapper) {
            request = (HttpServletRequest) ReflectionUtils.getField(this.requestField, request);
            ctx.setRequest(new Servlet30RequestWrapper(request));
        }
        return null;
    }

}
