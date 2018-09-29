package com.moguhu.zuul.component;

import com.moguhu.zuul.constants.FilterConstants;
import com.moguhu.zuul.context.RequestContext;
import com.moguhu.zuul.util.HTTPRequestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriTemplate;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;

public class ProxyRequestHelper {

    private static final Log log = LogFactory.getLog(ProxyRequestHelper.class);

    /**
     * Zuul context key for a collection of ignored headers for the current request.
     * Pre-filters can set this up as a set of lowercase strings.
     */
    public static final String IGNORED_HEADERS = "ignoredHeaders";

    private Set<String> ignoredHeaders = new LinkedHashSet<>();

    private Set<String> sensitiveHeaders = new LinkedHashSet<>();

    private Set<String> whitelistHosts = new LinkedHashSet<>();

    private boolean traceRequestBody = true;

    public void setWhitelistHosts(Set<String> whitelistHosts) {
        this.whitelistHosts.addAll(whitelistHosts);
    }

    public void setSensitiveHeaders(Set<String> sensitiveHeaders) {
        this.sensitiveHeaders.addAll(sensitiveHeaders);
    }

    public void setIgnoredHeaders(Set<String> ignoredHeaders) {
        this.ignoredHeaders.addAll(ignoredHeaders);
    }

    public void setTraceRequestBody(boolean traceRequestBody) {
        this.traceRequestBody = traceRequestBody;
    }

    public String buildZuulRequestURI(HttpServletRequest request) {
        RequestContext context = RequestContext.getCurrentContext();
        String uri = request.getRequestURI();
        String contextURI = (String) context.get(FilterConstants.REQUEST_URI_KEY);
        if (contextURI != null) {
            try {
                uri = UriUtils.encodePath(contextURI, characterEncoding(request));
            } catch (Exception e) {
                log.debug("unable to encode uri path from context, falling back to uri from request", e);
            }
        }
        return uri;
    }

    private String characterEncoding(HttpServletRequest request) {
        return request.getCharacterEncoding() != null ? request.getCharacterEncoding() : WebUtils.DEFAULT_CHARACTER_ENCODING;
    }

    public MultiValueMap<String, String> buildZuulRequestQueryParams(
            HttpServletRequest request) {
        Map<String, List<String>> map = HTTPRequestUtils.getInstance().getQueryParams();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        if (map == null) {
            return params;
        }
        for (String key : map.keySet()) {
            for (String value : map.get(key)) {
                params.add(key, value);
            }
        }
        return params;
    }

    public MultiValueMap<String, String> buildZuulRequestHeaders(
            HttpServletRequest request) {
        RequestContext context = RequestContext.getCurrentContext();
        MultiValueMap<String, String> headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                if (isIncludedHeader(name)) {
                    Enumeration<String> values = request.getHeaders(name);
                    while (values.hasMoreElements()) {
                        String value = values.nextElement();
                        headers.add(name, value);
                    }
                }
            }
        }
        Map<String, String> zuulRequestHeaders = context.getZuulRequestHeaders();
        for (String header : zuulRequestHeaders.keySet()) {
            headers.set(header, zuulRequestHeaders.get(header));
        }
        headers.set(HttpHeaders.ACCEPT_ENCODING, "gzip");
        return headers;
    }

    public void setResponse(int status, InputStream entity,
                            MultiValueMap<String, String> headers) throws IOException {
        RequestContext context = RequestContext.getCurrentContext();
        context.setResponseStatusCode(status);
        if (entity != null) {
            context.setResponseDataStream(entity);
        }

        boolean isOriginResponseGzipped = false;
        for (Entry<String, List<String>> header : headers.entrySet()) {
            String name = header.getKey();
            for (String value : header.getValue()) {
                if (name.equalsIgnoreCase(HttpHeaders.CONTENT_ENCODING)
                        && HTTPRequestUtils.getInstance().isGzipped(value)) {
                    isOriginResponseGzipped = true;
                }
                if (name.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
                    context.setOriginContentLength(value);
                }
                if (isIncludedHeader(name)) {
                    context.addZuulResponseHeader(name, value);
                }
            }
        }
        context.setResponseGZipped(isOriginResponseGzipped);
    }

    public void addIgnoredHeaders(String... names) {
        RequestContext ctx = RequestContext.getCurrentContext();
        if (!ctx.containsKey(IGNORED_HEADERS)) {
            ctx.set(IGNORED_HEADERS, new HashSet<String>());
        }
        @SuppressWarnings("unchecked")
        Set<String> set = (Set<String>) ctx.get(IGNORED_HEADERS);
        for (String name : this.ignoredHeaders) {
            set.add(name.toLowerCase());
        }
        for (String name : names) {
            set.add(name.toLowerCase());
        }
    }

    public boolean isIncludedHeader(String headerName) {
        String name = headerName.toLowerCase();
        RequestContext ctx = RequestContext.getCurrentContext();
        if (ctx.containsKey(IGNORED_HEADERS)) {
            Object object = ctx.get(IGNORED_HEADERS);
            if (object instanceof Collection && ((Collection<?>) object).contains(name)) {
                return false;
            }
        }
        switch (name) {
            case "host":
            case "connection":
            case "content-length":
            case "content-encoding":
            case "server":
            case "transfer-encoding":
            case "x-application-context":
                return false;
            default:
                return true;
        }
    }

    public Map<String, Object> debug(String verb, String uri,
                                     MultiValueMap<String, String> headers, MultiValueMap<String, String> params,
                                     InputStream requestEntity) throws IOException {
        Map<String, Object> info = new LinkedHashMap<>();
        return info;
    }

    protected boolean shouldDebugBody(RequestContext ctx) {
        HttpServletRequest request = ctx.getRequest();
        if (!this.traceRequestBody || ctx.isChunkedRequestBody()) {
            return false;
        }
        if (request == null || request.getContentType() == null) {
            return true;
        }
        return !request.getContentType().toLowerCase().contains("multipart");
    }

    public void appendDebug(Map<String, Object> info, int status,
                            MultiValueMap<String, String> headers) {
    }

    /**
     * Get url encoded query string. Pay special attention to single parameters with no values
     * and parameter names with colon (:) from use of UriTemplate.
     *
     * @param params Un-encoded request parameters
     * @return
     */
    public String getQueryString(MultiValueMap<String, String> params) {
        if (params.isEmpty()) {
            return "";
        }
        StringBuilder query = new StringBuilder();
        Map<String, Object> singles = new HashMap<>();
        for (String param : params.keySet()) {
            int i = 0;
            for (String value : params.get(param)) {
                query.append("&");
                query.append(param);
                if (!"".equals(value)) { // don't add =, if original is ?wsdl, output is not ?wsdl=
                    String key = param;
                    // if form feed is already part of param name double
                    // since form feed is used as the colon replacement below
                    if (key.contains("\f")) {
                        key = (key.replaceAll("\f", "\f\f"));
                    }
                    // colon is special to UriTemplate
                    if (key.contains(":")) {
                        key = key.replaceAll(":", "\f");
                    }
                    key = key + i;
                    singles.put(key, value);
                    query.append("={");
                    query.append(key);
                    query.append("}");
                }
                i++;
            }
        }

        UriTemplate template = new UriTemplate("?" + query.toString().substring(1));
        return template.expand(singles).toString();
    }
}
