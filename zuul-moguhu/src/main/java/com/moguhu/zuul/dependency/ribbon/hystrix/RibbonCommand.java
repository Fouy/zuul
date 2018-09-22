package com.moguhu.zuul.dependency.ribbon.hystrix;

import com.netflix.client.AbstractLoadBalancerAwareClient;
import com.netflix.client.ClientException;
import com.netflix.client.http.HttpRequest;
import com.netflix.client.http.HttpResponse;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.moguhu.zuul.constants.ZuulConstants;
import com.moguhu.zuul.context.NFRequestContext;

import javax.ws.rs.core.MultivaluedMap;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static com.netflix.client.http.HttpRequest.Verb;

/**
 * Hystrix wrapper around Eureka Ribbon command
 */
public class RibbonCommand<T extends AbstractLoadBalancerAwareClient<HttpRequest, HttpResponse>> extends HystrixCommand<HttpResponse> {

    private final T restClient;
    private final Verb verb;
    private final URI uri;
    private final MultivaluedMap<String, String> headers;
    private final MultivaluedMap<String, String> params;
    private final InputStream requestEntity;


    public RibbonCommand(T restClient,
                         Verb verb,
                         String uri,
                         MultivaluedMap<String, String> headers,
                         MultivaluedMap<String, String> params,
                         InputStream requestEntity) throws URISyntaxException {
        this("default", restClient, verb, uri, headers, params, requestEntity);
    }


    public RibbonCommand(String commandKey,
                         T restClient,
                         Verb verb,
                         String uri,
                         MultivaluedMap<String, String> headers,
                         MultivaluedMap<String, String> params,
                         InputStream requestEntity) throws URISyntaxException {

        // Switch the command/group key to remain passive with the previous release which used the command key as the group key
        this(commandKey, RibbonCommand.class.getSimpleName(), restClient, verb, uri, headers, params, requestEntity);
    }

    public RibbonCommand(String groupKey,
                         String commandKey,
                         T restClient,
                         Verb verb,
                         String uri,
                         MultivaluedMap<String, String> headers,
                         MultivaluedMap<String, String> params,
                         InputStream requestEntity) throws URISyntaxException {

        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(groupKey))
                .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey)).andCommandPropertiesDefaults(
                        // we want to default to semaphore-isolation since this wraps
                        // 2 others commands that are already thread isolated
                        HystrixCommandProperties.Setter().withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)
                                .withExecutionIsolationSemaphoreMaxConcurrentRequests(DynamicPropertyFactory.getInstance().
                                        getIntProperty(ZuulConstants.ZUUL_EUREKA + commandKey + ".semaphore.maxSemaphores", 100).get())));

        this.restClient = restClient;
        this.verb = verb;
        this.uri = new URI(uri);
        this.headers = headers;
        this.params = params;
        this.requestEntity = requestEntity;
    }

    protected T getRestClient() {
        return restClient;
    }

    protected Verb getVerb() {
        return verb;
    }

    protected URI getUri() {
        return uri;
    }

    protected MultivaluedMap<String, String> getHeaders() {
        return headers;
    }

    protected MultivaluedMap<String, String> getParams() {
        return params;
    }

    protected InputStream getRequestEntity() {
        return requestEntity;
    }

    @Override
    protected HttpResponse run() throws Exception {
        try {
            return forward();
        } catch (Exception e) {
            throw e;
        }
    }

    HttpResponse forward() throws Exception {

        NFRequestContext context = NFRequestContext.getCurrentContext();


        HttpRequest.Builder builder = HttpRequest.newBuilder().
                verb(verb).
                uri(uri).
                entity(requestEntity);

        for (String name : headers.keySet()) {
            List<String> values = headers.get(name);
            for (String value : values) {
                builder.header(name, value);
            }
        }

        for (String name : params.keySet()) {
            List<String> values = params.get(name);
            for (String value : values) {
                builder.queryParams(name, value);
            }
        }

        HttpRequest httpClientRequest = builder.build();

        HttpResponse response = execute(httpClientRequest);
        context.setZuulResponse(response);

        // Here we want to handle the case where this hystrix command timed-out before the
        // ribbon client received a response from origin. So in this situation we want to
        // cleanup this response (release connection) now, as we know that the zuul filter
        // chain has already continued without us and therefore won't cleanup itself.
        if (isResponseTimedOut()) {
            response.close();
        }

        return response;
    }

    protected HttpResponse execute(HttpRequest httpClientRequest) throws ClientException {
        return restClient.executeWithLoadBalancer(httpClientRequest);
    }

}
