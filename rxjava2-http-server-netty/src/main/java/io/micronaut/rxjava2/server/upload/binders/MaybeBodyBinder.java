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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.GenericArgument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.BodyArgumentBinder;
import io.micronaut.http.bind.binders.NonBlockingBodyArgumentBinder;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Single;
import org.reactivestreams.Publisher;

/**
 * Bindings {@link io.micronaut.http.annotation.Body} arguments of type {@link Maybe}.
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@Internal
public class MaybeBodyBinder implements NonBlockingBodyArgumentBinder<Maybe> {

    public static final Argument<Maybe> TYPE = Argument.of(Maybe.class);

    private final BodyArgumentBinder<Publisher<Object>> publisherBodyBinder;

    /**
     * @param publisherBodyBinder          the publisher body binder
     */
    public MaybeBodyBinder(BodyArgumentBinder<Publisher<Object>> publisherBodyBinder) {
        this.publisherBodyBinder = publisherBodyBinder;
    }

    @NonNull
    @Override
    public List<Class<?>> superTypes() {
        return Collections.singletonList(MaybeSource.class);
    }

    @Override
    public Argument<Maybe> argumentType() {
        return TYPE;
    }

    @Override
    public BindingResult<Maybe> bind(ArgumentConversionContext<Maybe> context, HttpRequest<?> source) {
        Argument<Maybe> maybeArgument = context.getArgument();
        Argument<Publisher<Object>> argument = new GenericArgument<>() {
            @Override
            public Argument[] getTypeParameters() {
                return maybeArgument.getTypeParameters();
            }

            @Override
            public Map<String, Argument<?>> getTypeVariables() {
                return maybeArgument.getTypeVariables();
            }

            @Override
            public AnnotationMetadata getAnnotationMetadata() {
                return maybeArgument.getAnnotationMetadata();
            }
        };
        BindingResult<Publisher<Object>> result = publisherBodyBinder.bind(
                ConversionContext.of(argument),
                source
        );
        if (result.isPresentAndSatisfied()) {
            return () -> Optional.of(Single.fromPublisher(result.get()).toMaybe());
        }
        return () -> Optional.of(Maybe.empty());
    }
}
