package com.moguhu.zuul.constants;

/**
 * Zuul 网关常量
 */
public class ZuulConstants {

    public static final String GATE_SERVICE_CODE = "gate.service.code";

    public static final String ZUUL_FILTER_PRE_PATH = "zuul.filter.pre.path";
    public static final String ZUUL_FILTER_POST_PATH = "zuul.filter.post.path";
    public static final String ZUUL_FILTER_ROUTE_PATH = "zuul.filter.route.path";
    public static final String ZUUL_FILTER_ERROR_PATH = "zuul.filter.error.path";
    public static final String ZUUL_FILTER_CUSTOM_PATH = "zuul.filter.custom.path";


    public static final String BAIZE_ZOOKEEPER_HOSTS = "baize.zookeeper.hosts";


    public static final String ZUUL_USE_ACTIVE_FILTERS = "zuul.use.active.filters";
    public static final String ZUUL_USE_CANARY_FILTERS = "zuul.use.canary.filters";


    public static final String ZUUL_FILTER_POLLER_INTERVAL = "zuul.filter.poller.interval";
    public static final String ZUUL_FILTER_DAO_TYPE = "zuul.filter.dao.type";


    public static final String ZUUL_EUREKA = "zuul.eureka.";
    public static final String ZUUL_AUTODETECT_BACKEND_VIPS = "zuul.autodetect-backend-vips";
    public static final String ZUUL_RIBBON_NAMESPACE = "zuul.ribbon.namespace";
    public static final String ZUUL_RIBBON_VIPADDRESS_TEMPLATE = "zuul.ribbon.vipAddress.template";
    public static final String ZUUL_HTTPCLIENT = "zuul.httpClient.";


    public static final String ZUUL_ROUTER_ALT_ROUTE_VIP = "zuul.router.alt.route.vip";
    public static final String ZUUL_ROUTER_ALT_ROUTE_HOST = "zuul.router.alt.route.host";
    public static final String ZUUL_ROUTER_ALT_ROUTE_PERMYRIAD = "zuul.router.alt.route.permyriad";
    public static final String ZUUL_ROUTER_ALT_ROUTE_MAXLIMIT = "zuul.router.alt.route.maxlimit";
    public static final String ZUUL_NIWS_DEFAULTCLIENT = "zuul.niws.defaultClient";
    public static final String ZUUL_DEFAULT_HOST = "zuul.default.host";
    public static final String ZUUL_HOST_SOCKET_TIMEOUT_MILLIS = "zuul.host.socket-timeout-millis";
    public static final String ZUUL_HOST_CONNECT_TIMEOUT_MILLIS = "zuul.host.connect-timeout-millis";
    public static final String ZUUL_INCLUDE_DEBUG_HEADER = "zuul.include-debug-header";
    public static final String ZUUL_INITIAL_STREAM_BUFFER_SIZE = "zuul.initial-stream-buffer-size";
    public static final String ZUUL_SET_CONTENT_LENGTH = "zuul.set-content-length";
    public static final String ZUUL_DEBUGFILTERS_DISABLED = "zuul.debugFilters.disabled";
    public static final String ZUUL_DEBUG_VIP = "zuul.debug.vip";
    public static final String ZUUL_DEBUG_HOST = "zuul.debug.host";

    // Prevent instantiation
    private ZuulConstants() {
        throw new AssertionError("Must not instantiate constant utility class");
    }

}
