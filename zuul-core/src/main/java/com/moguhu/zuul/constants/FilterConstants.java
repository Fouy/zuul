package com.moguhu.zuul.constants;

import com.moguhu.zuul.component.pre.DebugFilter;
import com.moguhu.zuul.component.pre.Servlet30WrapperFilter;
import com.moguhu.zuul.component.route.SimpleHostRoutingFilter;
import com.moguhu.zuul.context.RequestContext;

/**
 * Zuul Filter 常量
 */
public class FilterConstants {

    // KEY 常量 -----------------------------------

    /**
     * Zuul {@link RequestContext} key for use in {@link RibbonRoutingFilter}
     */
    public static final String REQUEST_ENTITY_KEY = "requestEntity";

    /**
     * Zuul {@link RequestContext} key for use in to override the path of the request.
     */
    public static final String REQUEST_URI_KEY = "requestURI";

    /**
     * Zuul {@link RequestContext} key for use in {@link RibbonRoutingFilter}
     */
    public static final String RETRYABLE_KEY = "retryable";

    /**
     * Zuul {@link RequestContext} key for use in {@link SendResponseFilter}
     */
    public static final String ROUTING_DEBUG_KEY = "routingDebug";

    /**
     * Zuul {@link RequestContext} key for use in {@link RibbonRoutingFilter}
     */
    public static final String SERVICE_ID_KEY = "serviceId";

    /**
     * Zuul {@link RequestContext} key for use in {@link RibbonRoutingFilter}
     */
    public static final String LOAD_BALANCER_KEY = "loadBalancerKey";


    // ORDER 常量 -----------------------------------
    /**
     * api 匹配过滤器, 优先级最高
     */
    public static final int API_MAPPING_FILTER_ORDER = -100;

    /**
     * filter order for {@link Servlet30WrapperFilter#filterOrder()}
     */
    public static final int SERVLET_30_WRAPPER_FILTER_ORDER = -2;

    /**
     * Filter Order for {@link DebugFilter#filterOrder()}
     */
    public static final int DEBUG_FILTER_ORDER = 1;

    /**
     * Filter Order for {@link RibbonRoutingFilter#filterOrder()}
     */
    public static final int RIBBON_ROUTING_FILTER_ORDER = 10;

    /**
     * Filter Order for {@link SendForwardFilter#filterOrder()}
     */
    public static final int SEND_FORWARD_FILTER_ORDER = 500;

    /**
     * Filter Order for {@link SendResponseFilter#filterOrder()}
     */
    public static final int SEND_RESPONSE_FILTER_ORDER = 1000;

    /**
     * Filter Order for {@link SimpleHostRoutingFilter#filterOrder()}
     */
    public static final int SIMPLE_HOST_ROUTING_FILTER_ORDER = 100;

    /**
     * 异常过滤器优先级
     */
    public static final int SEND_ERROR_FILTER_ORDER = 0;


    // Zuul Filter 类型常量 -----------------------------------

    public static final String ERROR_TYPE = "error";

    public static final String POST_TYPE = "post";

    public static final String PRE_TYPE = "pre";

    public static final String ROUTE_TYPE = "route";


    // 其他常量 -----------------------------------

    /**
     * Zuul {@link RequestContext} key for use in {@link SendForwardFilter}
     */
    public static final String FORWARD_LOCATION_PREFIX = "forward:";

    /**
     * default http port
     */
    public static final int HTTP_PORT = 80;

    /**
     * default https port
     */
    public static final int HTTPS_PORT = 443;

    /**
     * http url scheme
     */
    public static final String HTTP_SCHEME = "http";

    /**
     * https url scheme
     */
    public static final String HTTPS_SCHEME = "https";



    // HEADER 常量 -----------------------------------
    /**
     * X-* Header for the matching url. Used when routes use a url rather than serviceId
     */
    public static final String SERVICE_HEADER = "X-Zuul-Service";

    /**
     * X-* Header for the matching serviceId
     */
    public static final String SERVICE_ID_HEADER = "X-Zuul-ServiceId";

    /**
     * X-Forwarded-For Header
     */
    public static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";

    /**
     * X-Forwarded-Host Header
     */
    public static final String X_FORWARDED_HOST_HEADER = "X-Forwarded-Host";

    /**
     * X-Forwarded-Prefix Header
     */
    public static final String X_FORWARDED_PREFIX_HEADER = "X-Forwarded-Prefix";

    /**
     * X-Forwarded-Port Header
     */
    public static final String X_FORWARDED_PORT_HEADER = "X-Forwarded-Port";

    /**
     * X-Forwarded-Proto Header
     */
    public static final String X_FORWARDED_PROTO_HEADER = "X-Forwarded-Proto";

    /**
     * X-Zuul-Debug Header
     */
    public static final String X_ZUUL_DEBUG_HEADER = "X-Zuul-Debug-Header";


    // Prevent instantiation
    private FilterConstants() {
        throw new AssertionError("Must not instantiate constant utility class");
    }

}
