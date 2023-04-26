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
package io.micronaut.rxjava2.server.upload.binders;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.NonBlockingBodyArgumentBinder;
import io.micronaut.http.server.netty.HttpContentProcessorResolver;
import io.micronaut.http.server.netty.binders.PublisherBodyBinder;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import org.reactivestreams.Publisher;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Bindings {@link io.micronaut.http.annotation.Body} arguments of type {@link Single}.
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@Internal
public class SingleBodyBinder implements NonBlockingBodyArgumentBinder<Single> {

    public static final Argument<Single> TYPE = Argument.of(Single.class);

    private final PublisherBodyBinder publisherBodyBinder;

    /**
     * @param conversionService           The conversion service
     * @param httpContentProcessorResolver The http content processor resolver
     */
    public SingleBodyBinder(ConversionService conversionService,
                            HttpContentProcessorResolver httpContentProcessorResolver) {
        this.publisherBodyBinder = new PublisherBodyBinder(conversionService, httpContentProcessorResolver);
    }

    @NonNull
    @Override
    public List<Class<?>> superTypes() {
        return Collections.singletonList(SingleSource.class);
    }

    @Override
    public Argument<Single> argumentType() {
        return TYPE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public BindingResult<Single> bind(ArgumentConversionContext<Single> context, HttpRequest<?> source) {
        Argument<Single> singleArgument = context.getArgument();
        Argument<Publisher<?>> argument = getPublisherArgument(singleArgument);
        BindingResult<Publisher<?>> result = publisherBodyBinder.bind(
            ConversionContext.of(argument),
            source
        );
        if (result.isPresentAndSatisfied()) {
            return () -> Optional.of(Single.fromPublisher(result.get()));
        }
        return BindingResult.EMPTY;
    }

    static Argument<Publisher<?>> getPublisherArgument(Argument<?> singleArgument) {
        Map<String, Argument<?>> typeVariablesMap = singleArgument.getTypeVariables();
        Collection<Argument<?>> typeVariables = typeVariablesMap.values();
        return (Argument<Publisher<?>>) Argument.of(
            singleArgument.getType(),
            singleArgument.getName(),
            singleArgument.getAnnotationMetadata(),
            typeVariables.toArray(Argument.ZERO_ARGUMENTS)
        );
    }
}
