package filters.pre

import com.moguhu.zuul.ZuulFilter
import com.moguhu.zuul.constants.ZuulConstants
import com.moguhu.zuul.context.RequestContext
import com.netflix.config.DynamicBooleanProperty
import com.netflix.config.DynamicPropertyFactory
import com.netflix.config.DynamicStringProperty

class DebugFilter extends ZuulFilter {

    static final DynamicBooleanProperty routingDebug = DynamicPropertyFactory.getInstance()
            .getBooleanProperty(ZuulConstants.ZUUL_DEBUG_REQUEST, false)
    static final DynamicStringProperty debugParameter = DynamicPropertyFactory.getInstance()
            .getStringProperty(ZuulConstants.ZUUL_DEBUG_PARAMETER, "debugParameter")

    @Override
    String filterType() {
        return 'pre'
    }

    @Override
    int filterOrder() {
        return 1
    }

    boolean shouldFilter() {
        if ("true".equals(RequestContext.currentContext.getRequest().getParameter(debugParameter.get()))) return true;
        return routingDebug.get();
    }

    Object run() {
        RequestContext.getCurrentContext().setDebugRequest(true)
        RequestContext.getCurrentContext().setDebugRouting(true)
        return null;
    }

}
