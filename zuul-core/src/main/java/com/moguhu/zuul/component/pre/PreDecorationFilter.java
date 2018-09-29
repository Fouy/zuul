package com.moguhu.zuul.component.pre;

import com.moguhu.zuul.ZuulFilter;
import com.moguhu.zuul.component.ProxyRequestHelper;
import com.moguhu.zuul.component.Route;
import com.moguhu.zuul.component.RouteLocator;
import com.moguhu.zuul.component.ZuulProperties;
import com.moguhu.zuul.context.RequestContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;

import static com.moguhu.zuul.constants.FilterConstants.*;

/**
 * 路由规则匹配, 但是不一定适合, 建议删除   (TODO 里面设置了一些参数接收, 如 URI 等)
 *
 * 前置过滤器, that determines where and how to route based on the supplied {@link RouteLocator}.
 * Also sets various proxy related headers for downstream requests.
 */
public class PreDecorationFilter extends ZuulFilter {

    private static final Log logger = LogFactory.getLog(PreDecorationFilter.class);

    private RouteLocator routeLocator;
    private String dispatcherServletPath;
    private ZuulProperties properties;
    private UrlPathHelper urlPathHelper = new UrlPathHelper();
    private ProxyRequestHelper proxyRequestHelper;

    public PreDecorationFilter(RouteLocator routeLocator, String dispatcherServletPath, ZuulProperties properties,
                               ProxyRequestHelper proxyRequestHelper) {
        this.routeLocator = routeLocator;
        this.properties = properties;
        this.urlPathHelper.setRemoveSemicolonContent(properties.isRemoveSemicolonContent());
        this.dispatcherServletPath = dispatcherServletPath;
        this.proxyRequestHelper = proxyRequestHelper;
    }

    @Override
    public int filterOrder() {
        return PRE_DECORATION_FILTER_ORDER;
    }

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        return !ctx.containsKey(FORWARD_TO_KEY) // a filter has already forwarded
                && !ctx.containsKey(SERVICE_ID_KEY); // a filter has already determined serviceId
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        final String requestURI = this.urlPathHelper.getPathWithinApplication(ctx.getRequest());
        Route route = this.routeLocator.getMatchingRoute(requestURI);
        if (route != null) {
            String location = route.getLocation();
            if (location != null) {
                ctx.put(REQUEST_URI_KEY, route.getPath());
                ctx.put(PROXY_KEY, route.getId());
                if (!route.isCustomSensitiveHeaders()) {
                    this.proxyRequestHelper
                            .addIgnoredHeaders(this.properties.getSensitiveHeaders().toArray(new String[0]));
                } else {
                    this.proxyRequestHelper.addIgnoredHeaders(route.getSensitiveHeaders().toArray(new String[0]));
                }

                if (route.getRetryable() != null) {
                    ctx.put(RETRYABLE_KEY, route.getRetryable());
                }

                if (location.startsWith(HTTP_SCHEME + ":") || location.startsWith(HTTPS_SCHEME + ":")) {
                    ctx.setRouteHost(getUrl(location));
                    ctx.addOriginResponseHeader(SERVICE_HEADER, location);
                } else if (location.startsWith(FORWARD_LOCATION_PREFIX)) {
                    ctx.set(FORWARD_TO_KEY,
                            StringUtils.cleanPath(location.substring(FORWARD_LOCATION_PREFIX.length()) + route.getPath()));
                    ctx.setRouteHost(null);
                    return null;
                } else {
                    // set serviceId for use in filters.route.RibbonRequest
                    ctx.set(SERVICE_ID_KEY, location);
                    ctx.setRouteHost(null);
                    ctx.addOriginResponseHeader(SERVICE_ID_HEADER, location);
                }
                if (this.properties.isAddProxyHeaders()) {
                    addProxyHeaders(ctx, route);
                    String xforwardedfor = ctx.getRequest().getHeader(X_FORWARDED_FOR_HEADER);
                    String remoteAddr = ctx.getRequest().getRemoteAddr();
                    if (xforwardedfor == null) {
                        xforwardedfor = remoteAddr;
                    } else if (!xforwardedfor.contains(remoteAddr)) { // Prevent duplicates
                        xforwardedfor += ", " + remoteAddr;
                    }
                    ctx.addZuulRequestHeader(X_FORWARDED_FOR_HEADER, xforwardedfor);
                }
                if (this.properties.isAddHostHeader()) {
                    ctx.addZuulRequestHeader(HttpHeaders.HOST, toHostHeader(ctx.getRequest()));
                }
            }
        } else {
            logger.warn("No route found for uri: " + requestURI);

            String fallBackUri = requestURI;
            String fallbackPrefix = this.dispatcherServletPath; // default fallback

            // remove the Zuul servletPath from the requestUri
            logger.debug("zuulServletPath=" + this.properties.getServletPath());
            fallBackUri = fallBackUri.replaceFirst(this.properties.getServletPath(), "");
            logger.debug("Replaced Zuul servlet path:" + fallBackUri);

            if (!fallBackUri.startsWith("/")) {
                fallBackUri = "/" + fallBackUri;
            }
            String forwardURI = fallbackPrefix + fallBackUri;
            forwardURI = forwardURI.replaceAll("//", "/");
            ctx.set(FORWARD_TO_KEY, forwardURI);
        }
        return null;
    }

    private void addProxyHeaders(RequestContext ctx, Route route) {
        HttpServletRequest request = ctx.getRequest();
        String host = toHostHeader(request);
        String port = String.valueOf(request.getServerPort());
        String proto = request.getScheme();
        if (hasHeader(request, X_FORWARDED_HOST_HEADER)) {
            host = request.getHeader(X_FORWARDED_HOST_HEADER) + "," + host;
        }
        if (!hasHeader(request, X_FORWARDED_PORT_HEADER)) {
            if (hasHeader(request, X_FORWARDED_PROTO_HEADER)) {
                StringBuilder builder = new StringBuilder();
                for (String previous : StringUtils.commaDelimitedListToStringArray(request.getHeader(X_FORWARDED_PROTO_HEADER))) {
                    if (builder.length() > 0) {
                        builder.append(",");
                    }
                    builder.append(HTTPS_SCHEME.equals(previous) ? HTTPS_PORT : HTTP_PORT);
                }
                builder.append(",").append(port);
                port = builder.toString();
            }
        } else {
            port = request.getHeader(X_FORWARDED_PORT_HEADER) + "," + port;
        }
        if (hasHeader(request, X_FORWARDED_PROTO_HEADER)) {
            proto = request.getHeader(X_FORWARDED_PROTO_HEADER) + "," + proto;
        }
        ctx.addZuulRequestHeader(X_FORWARDED_HOST_HEADER, host);
        ctx.addZuulRequestHeader(X_FORWARDED_PORT_HEADER, port);
        ctx.addZuulRequestHeader(X_FORWARDED_PROTO_HEADER, proto);
        addProxyPrefix(ctx, route);
    }

    private boolean hasHeader(HttpServletRequest request, String name) {
        return StringUtils.hasLength(request.getHeader(name));
    }

    private void addProxyPrefix(RequestContext ctx, Route route) {
        String forwardedPrefix = ctx.getRequest().getHeader(X_FORWARDED_PREFIX_HEADER);
        String contextPath = ctx.getRequest().getContextPath();
        String prefix = StringUtils.hasLength(forwardedPrefix) ? forwardedPrefix
                : (StringUtils.hasLength(contextPath) ? contextPath : null);
        if (StringUtils.hasText(route.getPrefix())) {
            StringBuilder newPrefixBuilder = new StringBuilder();
            if (prefix != null) {
                if (prefix.endsWith("/") && route.getPrefix().startsWith("/")) {
                    newPrefixBuilder.append(prefix, 0, prefix.length() - 1);
                } else {
                    newPrefixBuilder.append(prefix);
                }
            }
            newPrefixBuilder.append(route.getPrefix());
            prefix = newPrefixBuilder.toString();
        }
        if (prefix != null) {
            ctx.addZuulRequestHeader(X_FORWARDED_PREFIX_HEADER, prefix);
        }
    }

    private String toHostHeader(HttpServletRequest request) {
        int port = request.getServerPort();
        if ((port == HTTP_PORT && HTTP_SCHEME.equals(request.getScheme()))
                || (port == HTTPS_PORT && HTTPS_SCHEME.equals(request.getScheme()))) {
            return request.getServerName();
        } else {
            return request.getServerName() + ":" + port;
        }
    }

    private URL getUrl(String target) {
        try {
            return new URL(target);
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("Target URL is malformed", ex);
        }
    }
}
