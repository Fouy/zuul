package com.moguhu.zuul.context;

import com.moguhu.baize.client.model.ApiDto;
import com.moguhu.baize.client.model.ApiGroupDto;
import com.netflix.client.http.HttpResponse;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Extended RequestContext adding Netflix library specific concepts and data
 */
public class NFRequestContext extends RequestContext {

    private static final String EVENT_PROPS_KEY = "eventProperties";
    private static final String BACKEND_PARAMS_KEY = "backendParams";
    private static final String BACKEND_API_KEY = "backendApi";
    private static final String BACKEND_GROUP_KEY = "backendGroup";

    private static final String ERROR_HANDLED_KEY = "errorHandled";

    static {
        setContextClass(NFRequestContext.class);
    }

    /**
     * creates a new NFRequestContext
     */
    public NFRequestContext() {
        super();
        put(EVENT_PROPS_KEY, new HashMap<String, Object>());
    }

    /**
     * returns a NFRequestContext from the threadLocal
     *
     * @return
     */
    public static NFRequestContext getCurrentContext() {
        return (NFRequestContext) threadLocal.get();
    }

    /**
     * returns the routeVIP; that is the Eureka "vip" of registered instances
     *
     * @return
     */
    public String getRouteVIP() {
        return (String) get("routeVIP");
    }

    /**
     * sets routeVIP; that is the Eureka "vip" of registered instances
     *
     * @return
     */

    public void setRouteVIP(String sVip) {
        set("routeVIP", sVip);
    }

    /**
     * @return true if a routeHost or routeVip has been defined
     */
    public boolean hasRouteVIPOrHost() {
        return (getRouteVIP() != null) || (getRouteHost() != null);
    }

    /**
     * unsets the requestContextVariables
     */
    @Override
    public void unset() {
        if (getZuulResponse() != null) {
            getZuulResponse().close(); //check this?
        }
        super.unset();
    }

    /**
     * sets the requestEntity; the inputStream of the Request
     *
     * @param entity
     */
    public void setRequestEntity(InputStream entity) {
        set("requestEntity", entity);
    }

    /**
     * @return the requestEntity; the inputStream of the request
     */
    public InputStream getRequestEntity() {
        return (InputStream) get("requestEntity");
    }

    /**
     * Sets the HttpResponse response that comes back from a Ribbon client.
     *
     * @param response
     */
    public void setZuulResponse(HttpResponse response) {
        set("zuulResponse", response);
    }

    /**
     * gets the "zuulResponse"
     *
     * @return returns the HttpResponse from a Ribbon call to an origin
     */
    public HttpResponse getZuulResponse() {
        return (HttpResponse) get("zuulResponse");
    }

    /**
     * returns the "route". This is a Zuul defined bucket for collecting request metrics. By default the route is the
     * first segment of the uri  eg /get/my/stuff : route is "get"
     *
     * @return
     */
    public String getRoute() {
        return (String) get("route");
    }

    public void setEventProperty(String key, Object value) {
        getEventProperties().put(key, value);
    }

    public Map<String, Object> getEventProperties() {
        return (Map<String, Object>) this.get(EVENT_PROPS_KEY);
    }

    public void setBackendParams(Object value) {
        this.put(BACKEND_PARAMS_KEY, value);
    }

    public Map<String, Map<String, String>> getBackendParams() {
        return (Map<String, Map<String, String>>) this.get(BACKEND_PARAMS_KEY);
    }

    public void setBackendApi(Object value) {
        this.put(BACKEND_API_KEY, value);
    }

    public ApiDto getBackendApi() {
        return (ApiDto) this.get(BACKEND_API_KEY);
    }

    public void setBackendGroup(Object value) {
        this.put(BACKEND_GROUP_KEY, value);
    }

    public ApiGroupDto getBackendGroup() {
        return (ApiGroupDto) this.get(BACKEND_GROUP_KEY);
    }

    /**
     * sets the flag errorHandled if there is an exception.
     *
     * @param handled
     */
    public void setErrorHandled(boolean handled) {
        put("errorHandled", handled);
    }

    /**
     * @return true if there is an exception and has been handled.
     */
    public boolean errorHandled() {
        return getBoolean("errorHandled", false);
    }

}
