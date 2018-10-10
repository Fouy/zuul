package com.moguhu.zuul.component.pre;

import com.moguhu.baize.client.model.ApiDto;
import com.moguhu.baize.client.model.ComponentDto;
import com.moguhu.zuul.ZuulFilter;
import com.moguhu.zuul.constants.FilterConstants;
import com.moguhu.zuul.context.NFRequestContext;
import com.moguhu.zuul.context.RequestContext;

import javax.servlet.http.HttpServletRequest;

/**
 * 前置-debug过滤器
 * <p>
 * 启用方式如下:
 * 1. 请求头中设置了debug=true;
 * 2. baize管理平台显示启用组件; 即设置 {@link RequestContext} 的 debug 属性为 true
 */
public class DebugFilter extends ZuulFilter {

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
        HttpServletRequest request = NFRequestContext.getCurrentContext().getRequest();
        String debug = request.getHeader("debug");
        if ("true".equals(debug)) {
            return true;
        }

        ApiDto api = NFRequestContext.getCurrentContext().getBackendApi();
        for (ComponentDto componentDto : api.getComponentList()) {
            if (componentDto.getCompCode().equals(componentName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        ctx.setDebugRouting(true);
        ctx.setDebugRequest(true);
        return null;
    }

}
