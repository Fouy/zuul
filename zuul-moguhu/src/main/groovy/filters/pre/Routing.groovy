package filters.pre

import com.moguhu.zuul.FilterProcessor
import com.moguhu.zuul.ZuulApplicationInfo
import com.moguhu.zuul.ZuulFilter
import com.moguhu.zuul.constants.ZuulConstants
import com.moguhu.zuul.context.RequestContext
import com.moguhu.zuul.exception.ZuulException
import com.netflix.config.DynamicPropertyFactory
import com.netflix.config.DynamicStringProperty

/**
 *
 */
class Routing extends ZuulFilter {
    DynamicStringProperty defaultClient = DynamicPropertyFactory.getInstance().getStringProperty(ZuulConstants.ZUUL_NIWS_DEFAULTCLIENT, ZuulApplicationInfo.applicationName);
    DynamicStringProperty defaultHost = DynamicPropertyFactory.getInstance().getStringProperty(ZuulConstants.ZUUL_DEFAULT_HOST, null);

    @Override
    int filterOrder() {
        return 1
    }

    @Override
    String filterType() {
        return "pre"
    }

    boolean shouldFilter() {
        return true
    }

    Object staticRouting() {
        FilterProcessor.instance.runFilters("healthcheck")
        FilterProcessor.instance.runFilters("static")
    }

    Object run() {
        staticRouting() //runs the static Zuul

        ((NFRequestContext) RequestContext.currentContext).routeVIP = defaultClient.get()
        String host = defaultHost.get()
        if (((NFRequestContext) RequestContext.currentContext).routeVIP == null) ((NFRequestContext) RequestContext.currentContext).routeVIP = ZuulApplicationInfo.applicationName
        if (host != null) {
            final URL targetUrl = new URL(host)
            RequestContext.currentContext.setRouteHost(targetUrl);
            ((NFRequestContext) RequestContext.currentContext).routeVIP = null
        }

        if (host == null && RequestContext.currentContext.routeVIP == null) {
            throw new ZuulException("default VIP or host not defined. Define: zuul.niws.defaultClient or zuul.default.host", 501, "zuul.niws.defaultClient or zuul.default.host not defined")
        }

        String uri = RequestContext.currentContext.request.getRequestURI()
        if (RequestContext.currentContext.requestURI != null) {
            uri = RequestContext.currentContext.requestURI
        }
        if (uri == null) uri = "/"
        if (uri.startsWith("/")) {
            uri = uri - "/"
        }

        ((NFRequestContext) RequestContext.currentContext).route = uri.substring(0, uri.indexOf("/") + 1)
    }
}