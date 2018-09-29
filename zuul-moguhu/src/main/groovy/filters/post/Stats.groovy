package filters.post

import com.moguhu.zuul.ZuulFilter
import com.moguhu.zuul.context.RequestContext
import com.moguhu.zuul.stats.StatsManager

/**
 *
 */
class Stats extends ZuulFilter {
    @Override
    String filterType() {
        return "post"
    }

    @Override
    int filterOrder() {
        return 2000
    }

    @Override
    boolean shouldFilter() {
        return true
    }

    @Override
    Object run() {
        int status = RequestContext.getCurrentContext().getResponseStatusCode();
        StatsManager sm = StatsManager.manager
        sm.collectRequestStats(RequestContext.getCurrentContext().getRequest());
        sm.collectRouteStats(RequestContext.getCurrentContext().route, status);
        dumpRoutingDebug()
        dumpRequestDebug()
    }

    public void dumpRequestDebug() {
        List<String> rd = (List<String>) RequestContext.getCurrentContext().get("requestDebug");
        rd?.each {
            println("REQUEST_DEBUG::${it}");
        }
    }

    public void dumpRoutingDebug() {
        List<String> rd = (List<String>) RequestContext.getCurrentContext().get("routingDebug");
        rd?.each {
            println("ZUUL_DEBUG::${it}");
        }
    }

}
