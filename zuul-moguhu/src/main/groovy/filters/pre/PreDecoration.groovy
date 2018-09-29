package filters.pre

import com.moguhu.zuul.ZuulFilter
import com.moguhu.zuul.context.RequestContext
import com.moguhu.zuul.exception.ZuulException

import static com.moguhu.zuul.constants.ZuulHeaders.*

/**
 *
 */
public class PreDecoration extends ZuulFilter {

    @Override
    String filterType() {
        return "pre"
    }

    @Override
    int filterOrder() {
        return 20
    }

    @Override
    boolean shouldFilter() {
        return true
    }

    @Override
    Object run() {
        if (RequestContext.currentContext.getRequest().getParameter("url") != null) {
            try {
                RequestContext.getCurrentContext().routeHost = new URL(RequestContext.currentContext.getRequest().getParameter("url"))
                RequestContext.currentContext.setResponseGZipped(true)
            } catch (MalformedURLException e) {
                throw new ZuulException(e, "Malformed URL", 400, "MALFORMED_URL")
            }
        }
        setOriginRequestHeaders()
        return null
    }

    void setOriginRequestHeaders() {
        RequestContext context = RequestContext.currentContext
        context.addZuulRequestHeader("X-Netflix.request.toplevel.uuid", UUID.randomUUID().toString())
        context.addZuulRequestHeader(X_FORWARDED_FOR, context.getRequest().remoteAddr)
        context.addZuulRequestHeader(X_NETFLIX_CLIENT_HOST, context.getRequest().getHeader(HOST))
        if (context.getRequest().getHeader(X_FORWARDED_PROTO) != null) {
            context.addZuulRequestHeader(X_NETFLIX_CLIENT_PROTO, context.getRequest().getHeader(X_FORWARDED_PROTO))
        }
    }

}
