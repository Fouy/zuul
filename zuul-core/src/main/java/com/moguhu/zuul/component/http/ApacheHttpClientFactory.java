package com.moguhu.zuul.component.http;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * Factory for creating a new {@link CloseableHttpClient}.
 *
 */
public interface ApacheHttpClientFactory {

    /**
     * Creates an {@link HttpClientBuilder} that can be used to create a new {@link CloseableHttpClient}.
     *
     * @return A {@link HttpClientBuilder}
     */
    HttpClientBuilder createBuilder();
}
