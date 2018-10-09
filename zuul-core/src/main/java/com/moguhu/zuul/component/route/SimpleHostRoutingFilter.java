package com.moguhu.zuul.component.route;

import com.moguhu.zuul.ZuulFilter;
import com.moguhu.zuul.component.ProxyRequestHelper;
import com.moguhu.zuul.component.ZuulProperties;
import com.moguhu.zuul.component.http.ApacheHttpClientConnectionManagerFactory;
import com.moguhu.zuul.component.http.ApacheHttpClientFactory;
import com.moguhu.zuul.component.http.DefaultApacheHttpClientConnectionManagerFactory;
import com.moguhu.zuul.component.http.DefaultApacheHttpClientFactory;
import com.moguhu.zuul.context.RequestContext;
import com.moguhu.zuul.exception.ZuulRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import static com.moguhu.zuul.constants.FilterConstants.ROUTE_TYPE;
import static com.moguhu.zuul.constants.FilterConstants.SIMPLE_HOST_ROUTING_FILTER_ORDER;

/**
 * 路由过滤器, 通过HttpClient 发送固定的URL请求. URL从 {@link RequestContext#getRouteHost()} 中获取.
 * <p>
 * TODO 需要改造
 */
public class SimpleHostRoutingFilter extends ZuulFilter {

    private static final Log logger = LogFactory.getLog(SimpleHostRoutingFilter.class);

    private final Timer connectionManagerTimer = new Timer("SimpleHostRoutingFilter.connectionManagerTimer", true);

    private boolean sslHostnameValidationEnabled;
    private boolean forceOriginalQueryStringEncoding;

    private ProxyRequestHelper helper;
    private ZuulProperties.Host hostProperties;
    private ApacheHttpClientConnectionManagerFactory connectionManagerFactory;
    private ApacheHttpClientFactory httpClientFactory;
    private HttpClientConnectionManager connectionManager;
    private CloseableHttpClient httpClient;

    public SimpleHostRoutingFilter(ProxyRequestHelper helper, ZuulProperties properties) {
        this.helper = helper;
        this.hostProperties = properties.getHost();
        this.sslHostnameValidationEnabled = properties.isSslHostnameValidationEnabled();
        this.forceOriginalQueryStringEncoding = properties.isForceOriginalQueryStringEncoding();
        this.connectionManagerFactory = new DefaultApacheHttpClientConnectionManagerFactory();
        this.httpClientFactory = new DefaultApacheHttpClientFactory();
    }

    @PostConstruct
    private void initialize() {
        this.connectionManager = connectionManagerFactory.newConnectionManager(!this.sslHostnameValidationEnabled,
                this.hostProperties.getMaxTotalConnections(), this.hostProperties.getMaxPerRouteConnections(),
                this.hostProperties.getTimeToLive(), this.hostProperties.getTimeUnit(), null);
        this.httpClient = newClient();
        this.connectionManagerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (SimpleHostRoutingFilter.this.connectionManager == null) {
                    return;
                }
                SimpleHostRoutingFilter.this.connectionManager.closeExpiredConnections();
            }
        }, 30000, 5000);
    }

    public void stop() {
        this.connectionManagerTimer.cancel();
    }

    @Override
    public String filterType() {
        return ROUTE_TYPE;
    }

    @Override
    public int filterOrder() {
        return SIMPLE_HOST_ROUTING_FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        return RequestContext.getCurrentContext().getRouteHost() != null
                && RequestContext.getCurrentContext().sendZuulResponse();
    }

    @Override
    public Object run() {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();
        MultiValueMap<String, String> headers = this.helper.buildZuulRequestHeaders(request);
        MultiValueMap<String, String> params = this.helper.buildZuulRequestQueryParams(request);
        String verb = getVerb(request);
        InputStream requestEntity = getRequestBody(request);
        if (request.getContentLength() < 0) {
            context.setChunkedRequestBody();
        }

        String uri = this.helper.buildZuulRequestURI(request);
        this.helper.addIgnoredHeaders();

        try {
            CloseableHttpResponse response = forward(this.httpClient, verb, uri, request, headers, params, requestEntity);
            setResponse(response);
        } catch (Exception ex) {
            throw new ZuulRuntimeException(ex);
        }
        return null;
    }

    protected HttpClientConnectionManager getConnectionManager() {
        return connectionManager;
    }

    protected CloseableHttpClient newClient() {
        final RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(this.hostProperties.getSocketTimeoutMillis())
                .setConnectTimeout(this.hostProperties.getConnectTimeoutMillis())
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
        return httpClientFactory.createBuilder().setDefaultRequestConfig(requestConfig).
                setConnectionManager(this.connectionManager).disableRedirectHandling().build();
    }

    private CloseableHttpResponse forward(CloseableHttpClient httpclient, String verb,
                                          String uri, HttpServletRequest request, MultiValueMap<String, String> headers,
                                          MultiValueMap<String, String> params, InputStream requestEntity)
            throws Exception {
        Map<String, Object> info = this.helper.debug(verb, uri, headers, params, requestEntity);
        URL host = RequestContext.getCurrentContext().getRouteHost();
        HttpHost httpHost = getHttpHost(host);
        uri = StringUtils.cleanPath((host.getPath() + uri).replaceAll("/{2,}", "/"));
        int contentLength = request.getContentLength();

        ContentType contentType = null;
        if (request.getContentType() != null) {
            contentType = ContentType.parse(request.getContentType());
        }

        InputStreamEntity entity = new InputStreamEntity(requestEntity, contentLength, contentType);
        HttpRequest httpRequest = buildHttpRequest(verb, uri, entity, headers, params, request);
        try {
            logger.debug(httpHost.getHostName() + " " + httpHost.getPort() + " " + httpHost.getSchemeName());
            CloseableHttpResponse zuulResponse = forwardRequest(httpclient, httpHost, httpRequest);
            this.helper.appendDebug(info, zuulResponse.getStatusLine().getStatusCode(),
                    revertHeaders(zuulResponse.getAllHeaders()));
            return zuulResponse;
        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            // httpclient.getConnectionManager().shutdown();
        }
    }

    protected HttpRequest buildHttpRequest(String verb, String uri,
                                           InputStreamEntity entity, MultiValueMap<String, String> headers,
                                           MultiValueMap<String, String> params, HttpServletRequest request) {
        HttpRequest httpRequest;
        String uriWithQueryString = uri + (this.forceOriginalQueryStringEncoding
                ? getEncodedQueryString(request) : this.helper.getQueryString(params));

        switch (verb.toUpperCase()) {
            case "POST":
                HttpPost httpPost = new HttpPost(uriWithQueryString);
                httpRequest = httpPost;
                httpPost.setEntity(entity);
                break;
            case "PUT":
                HttpPut httpPut = new HttpPut(uriWithQueryString);
                httpRequest = httpPut;
                httpPut.setEntity(entity);
                break;
            case "PATCH":
                HttpPatch httpPatch = new HttpPatch(uriWithQueryString);
                httpRequest = httpPatch;
                httpPatch.setEntity(entity);
                break;
            case "DELETE":
                BasicHttpEntityEnclosingRequest entityRequest = new BasicHttpEntityEnclosingRequest(
                        verb, uriWithQueryString);
                httpRequest = entityRequest;
                entityRequest.setEntity(entity);
                break;
            default:
                httpRequest = new BasicHttpRequest(verb, uriWithQueryString);
                logger.debug(uriWithQueryString);
        }

        httpRequest.setHeaders(convertHeaders(headers));
        return httpRequest;
    }

    private String getEncodedQueryString(HttpServletRequest request) {
        String query = request.getQueryString();
        return (query != null) ? "?" + query : "";
    }

    private MultiValueMap<String, String> revertHeaders(Header[] headers) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        for (Header header : headers) {
            String name = header.getName();
            if (!map.containsKey(name)) {
                map.put(name, new ArrayList<String>());
            }
            map.get(name).add(header.getValue());
        }
        return map;
    }

    private Header[] convertHeaders(MultiValueMap<String, String> headers) {
        List<Header> list = new ArrayList<>();
        for (String name : headers.keySet()) {
            for (String value : headers.get(name)) {
                list.add(new BasicHeader(name, value));
            }
        }
        return list.toArray(new BasicHeader[0]);
    }

    private CloseableHttpResponse forwardRequest(CloseableHttpClient httpclient,
                                                 HttpHost httpHost, HttpRequest httpRequest) throws IOException {
        return httpclient.execute(httpHost, httpRequest);
    }

    private HttpHost getHttpHost(URL host) {
        HttpHost httpHost = new HttpHost(host.getHost(), host.getPort(), host.getProtocol());
        return httpHost;
    }

    private InputStream getRequestBody(HttpServletRequest request) {
        InputStream requestEntity = null;
        try {
            requestEntity = request.getInputStream();
        } catch (IOException ex) {
            // no requestBody is ok.
        }
        return requestEntity;
    }

    private String getVerb(HttpServletRequest request) {
        String sMethod = request.getMethod();
        return sMethod.toUpperCase();
    }

    private void setResponse(HttpResponse response) throws IOException {
        RequestContext.getCurrentContext().set("zuulResponse", response);
        this.helper.setResponse(response.getStatusLine().getStatusCode(),
                response.getEntity() == null ? null : response.getEntity().getContent(),
                revertHeaders(response.getAllHeaders()));
    }

    /**
     * Add header names to exclude from proxied response in the current request.
     *
     * @param names
     */
    protected void addIgnoredHeaders(String... names) {
        this.helper.addIgnoredHeaders(names);
    }

    /**
     * Determines whether the filter enables the validation for ssl hostnames.
     *
     * @return true if enabled
     */
    boolean isSslHostnameValidationEnabled() {
        return this.sslHostnameValidationEnabled;
    }
}
