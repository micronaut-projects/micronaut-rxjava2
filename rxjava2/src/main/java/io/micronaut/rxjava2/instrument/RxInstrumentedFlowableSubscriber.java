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
package io.micronaut.rxjava2.instrument;

import io.micronaut.core.annotation.Internal;
import io.reactivex.FlowableSubscriber;
import org.reactivestreams.Subscriber;

/**
 * Inspired by code in Brave. Provides general instrumentation abstraction for RxJava2.
 * See https://github.com/openzipkin/brave/tree/master/context/rxjava2/src/main/java/brave/context/rxjava2/internal.
 *
 * @param <T> The type
 * @author graemerocher
 * @since 1.1
 */
@Internal
final class RxInstrumentedFlowableSubscriber<T> extends RxInstrumentedSubscriber<T> implements FlowableSubscriber<T>, RxInstrumentedComponent {

    /**
     * Default constructor.
     *
     * @param downstream          the downstream subscriber
     * @param instrumenterFactory The instrumenterFactory
     */
    RxInstrumentedFlowableSubscriber(Subscriber<T> downstream, RxInstrumenterFactory instrumenterFactory) {
        super(downstream, instrumenterFactory);
    }

}
