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
package io.micronaut.rxjava2.server.upload.binders;

import java.util.Collections;
import java.util.List;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.bind.annotation.Bindable;
import io.micronaut.core.type.GenericArgument;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.bind.RequestBinderRegistry;
import io.micronaut.http.bind.binders.BodyArgumentBinder;
import io.micronaut.inject.annotation.MutableAnnotationMetadata;
import io.reactivex.Flowable;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;

@Singleton
@Internal
@Requires(classes = Flowable.class)
class RxJava2NettyBinderRegistrar implements BeanCreatedEventListener<RequestBinderRegistry> {

    /**
     * Default constructor.
     */
    RxJava2NettyBinderRegistrar() {
    }

    @Override
    @SuppressWarnings("java:S1171")
    public RequestBinderRegistry onCreated(BeanCreatedEvent<RequestBinderRegistry> event) {
        RequestBinderRegistry registry = event.getBean();
        registry.findArgumentBinder(new GenericArgument<Publisher<Object>>() {
            final MutableAnnotationMetadata annotationMetadata = new MutableAnnotationMetadata();
            {
                annotationMetadata.addAnnotation(Body.class.getName(), Collections.emptyMap());
                annotationMetadata.addStereotype(List.of(Body.class.getName()), Bindable.class.getName(), Collections.emptyMap());
            }
            @Override
            public AnnotationMetadata getAnnotationMetadata() {
                return annotationMetadata;
            }
        }).ifPresent(argumentBinder -> {
            if (argumentBinder instanceof BodyArgumentBinder<Publisher<Object>> bodyArgumentBinder) {

                registry.addArgumentBinder(new MaybeBodyBinder(
                    bodyArgumentBinder
                ));
                registry.addArgumentBinder(new ObservableBodyBinder(
                    bodyArgumentBinder
                ));
                registry.addArgumentBinder(new SingleBodyBinder(
                    bodyArgumentBinder
                ));
            }
        });

        return registry;
    }
}
