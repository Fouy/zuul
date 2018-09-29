package com.moguhu.zuul.component.http;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * Factory for creating a new {@link CloseableHttpClient}.
 *
 * @author Ryan Baxter
 */
public interface ApacheHttpClientFactory {

    /**
     * Creates an {@link HttpClientBuilder} that can be used to create a new {@link CloseableHttpClient}.
     *
     * @return A {@link HttpClientBuilder}
     */
    public HttpClientBuilder createBuilder();
}
