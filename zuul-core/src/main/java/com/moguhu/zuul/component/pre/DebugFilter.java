package com.moguhu.zuul.component.pre;

import com.moguhu.zuul.ZuulFilter;
import com.moguhu.zuul.constants.FilterConstants;
import com.moguhu.zuul.constants.ZuulConstants;
import com.moguhu.zuul.context.RequestContext;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

import javax.servlet.http.HttpServletRequest;

/**
 * 前置过滤器 {@link ZuulFilter}, 当请求参数设置了: debug=true, 会打开调试模式, 即设置 {@link RequestContext} 的 debug 属性为 true
 */
public class DebugFilter extends ZuulFilter {

    private static final DynamicBooleanProperty ROUTING_DEBUG = DynamicPropertyFactory
            .getInstance().getBooleanProperty(ZuulConstants.ZUUL_DEBUG_REQUEST, false);

    private static final DynamicStringProperty DEBUG_PARAMETER = DynamicPropertyFactory
            .getInstance().getStringProperty(ZuulConstants.ZUUL_DEBUG_PARAMETER, "debug");

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.DEBUG_FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
        if ("true".equals(request.getParameter(DEBUG_PARAMETER.get()))) {
            return true;
        }
        return ROUTING_DEBUG.get();
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        ctx.setDebugRouting(true);
        ctx.setDebugRequest(true);
        return null;
    }

}
