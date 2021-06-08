/*
 * Copyright 2017-2021 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.rxjava2.http.client;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.http.client.HttpClientConfiguration;
import java.net.URL;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Provides static methods to create {@link Rx2HttpClient} and {@link Rx2StreamingHttpClient}.
 *
 * @author Sergio del Amo
 * @since 1.0.0
 */
public class Rx2HttpClientConfiguration {

    private static Rx2HttpClientFactory clientFactory = null;

    /**
     * Create a new {@link io.micronaut.http.client.HttpClient} with the specified configuration. Note that this method should only be used
     * outside of the context of an application. Within Micronaut use {@link javax.inject.Inject} to inject a client instead
     *
     * @param url The base URL
     * @param configuration The client configuration
     * @return The client
     * @since 2.2.0
     */
    @Internal
    static Rx2StreamingHttpClient createStreamingClient(@NonNull URL url, HttpClientConfiguration configuration) {
        ArgumentUtils.requireNonNull("url", url);
        Rx2HttpClientFactory clientFactory = getRx2HttpClientFactory();
        return clientFactory.createStreamingClient(url, configuration);
    }

    /**
     * Create a new {@link io.micronaut.http.client.HttpClient}. Note that this method should only be used outside of the context of an application. Within Micronaut use
     * {@link javax.inject.Inject} to inject a client instead
     *
     * @param url The base URL
     * @return The client
     */
    @Internal
    static Rx2StreamingHttpClient createStreamingClient(@NonNull URL url) {
        ArgumentUtils.requireNonNull("url", url);
        Rx2HttpClientFactory clientFactory = getRx2HttpClientFactory();
        return clientFactory.createStreamingClient(url);
    }

    private static Rx2HttpClientFactory getRx2HttpClientFactory() {
        Rx2HttpClientFactory clientFactory = Rx2HttpClientConfiguration.clientFactory;
        if (clientFactory == null) {
            synchronized (Rx2HttpClientConfiguration.class) { // double check
                clientFactory = Rx2HttpClientConfiguration.clientFactory;
                if (clientFactory == null) {
                    clientFactory = resolveClientFactory();
                    Rx2HttpClientConfiguration.clientFactory = clientFactory;
                }
            }
        }
        return clientFactory;
    }

    private static Rx2HttpClientFactory resolveClientFactory() {
        final Iterator<Rx2HttpClientFactory> i = ServiceLoader.load(Rx2HttpClientFactory.class).iterator();
        if (i.hasNext()) {
            return i.next();
        }
        throw new IllegalStateException("No Rx2HttpClientFactory present on classpath, cannot create HTTP client");
    }


    /**
     * Create a new {@link io.micronaut.http.client.HttpClient}. Note that this method should only be used outside of the context of an application. Within Micronaut use
     * {@link javax.inject.Inject} to inject a client instead
     *
     * @param url The base URL
     * @return The client
     */
    @Internal
    static Rx2HttpClient createClient(@Nullable URL url) {
        Rx2HttpClientFactory clientFactory = getRx2HttpClientFactory();
        return clientFactory.createClient(url);
    }

}
