package com.moguhu.zuul.component.http;

import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;

import java.util.concurrent.TimeUnit;

/**
 * Interface for creating an {@link HttpClientConnectionManager}.
 *
 * @author Ryan Baxter
 */
public interface ApacheHttpClientConnectionManagerFactory {

    public static final String HTTP_SCHEME = "http";
    public static final String HTTPS_SCHEME = "https";

    /**
     * Creates a new {@link HttpClientConnectionManager}.
     *
     * @param disableSslValidation   True to disable SSL validation, false otherwise
     * @param maxTotalConnections    The total number of connections
     * @param maxConnectionsPerRoute The total number of connections per route
     * @param timeToLive             The time a connection is allowed to exist
     * @param timeUnit               The time unit for the time to live value
     * @param registryBuilder        The {@link RegistryBuilder} to use in the connection manager
     * @return A new {@link HttpClientConnectionManager}
     */
    HttpClientConnectionManager newConnectionManager(boolean disableSslValidation,
                                                            int maxTotalConnections, int maxConnectionsPerRoute, long timeToLive,
                                                            TimeUnit timeUnit, RegistryBuilder registryBuilder);
}
