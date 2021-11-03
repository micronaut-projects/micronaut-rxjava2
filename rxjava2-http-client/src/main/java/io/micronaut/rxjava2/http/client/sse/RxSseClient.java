/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.rxjava2.http.client.sse;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClientConfiguration;
import io.micronaut.http.client.sse.SseClient;
import io.micronaut.http.sse.Event;
import io.reactivex.Flowable;

import java.net.URL;

/**
 * Extended version of {@link SseClient} for RxJava 2.x.
 *
 * @author Graeme Rocher
 * @since 1.0
 */
public interface RxSseClient extends SseClient {

    @Override
    <I> Flowable<Event<ByteBuffer<?>>> eventStream(@NonNull HttpRequest<I> request);

    @Override
    <I, B> Flowable<Event<B>> eventStream(@NonNull HttpRequest<I> request, @NonNull Argument<B> eventType);

    @Override
    default <I, B> Flowable<Event<B>> eventStream(@NonNull HttpRequest<I> request, @NonNull Class<B> eventType) {
        return (Flowable<Event<B>>) SseClient.super.eventStream(request, eventType);
    }

    @Override
    default <B> Flowable<Event<B>> eventStream(@NonNull String uri, @NonNull Class<B> eventType) {
        return (Flowable<Event<B>>) SseClient.super.eventStream(uri, eventType);
    }

    @Override
    default <B> Flowable<Event<B>> eventStream(@NonNull String uri, @NonNull Argument<B> eventType) {
        return (Flowable<Event<B>>) SseClient.super.eventStream(uri, eventType);
    }

    /**
     * Create a new {@link RxSseClient}.
     * Note that this method should only be used outside of the context of a Micronaut application.
     * The returned {@link RxSseClient} is not subject to dependency injection.
     * The creator is responsible for closing the client to avoid leaking connections.
     * Within a Micronaut application use {@link jakarta.inject.Inject} to inject a client instead.
     *
     * @param url The base URL
     * @return The client
     * @since 1.1.0
     */
    static RxSseClient create(@Nullable URL url) {
        return new BridgedRxSseClient(SseClient.create(url));
    }

    /**
     * Create a new {@link RxSseClient} with the specified configuration. Note that this method should only be used
     * outside of the context of an application. Within Micronaut use {@link jakarta.inject.Inject} to inject a client instead
     *
     * @param url The base URL
     * @param configuration the client configuration
     * @return The client
     * @since 1.1.0
     */
    static RxSseClient create(@Nullable URL url, @NonNull HttpClientConfiguration configuration) {
        return new BridgedRxSseClient(SseClient.create(url, configuration));
    }
}
