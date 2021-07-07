/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.rxjava2.http.client

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import io.micronaut.runtime.server.EmbeddedServer
import io.reactivex.Flowable
import io.reactivex.Maybe
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class NotFoundSpec extends Specification {

    @Shared
    @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer, [
            'spec.name': 'NotFoundSpec'
    ])

    void "test 404 handling with Flowable"() {
        given:
        InventoryClient client = embeddedServer.getApplicationContext().getBean(InventoryClient)

        expect:
        client.flowable('1234').blockingFirst()
        client.flowable('notthere').toList().blockingGet() == []
    }

    void "test 404 handling with Maybe"() {
        given:
        InventoryClient client = embeddedServer.getApplicationContext().getBean(InventoryClient)

        expect:
        client.maybe('1234').blockingGet()
        client.maybe('notthere').blockingGet() == null
    }

    @Requires(property = 'spec.name', value = 'NotFoundSpec')
    @Client('/not-found')
    static interface InventoryClient {
        @Get('/maybe/{isbn}')
        @Consumes(MediaType.TEXT_PLAIN)
        Maybe<Boolean> maybe(String isbn)

        @Get(value = '/flowable/{isbn}', processes = MediaType.TEXT_EVENT_STREAM)
        Flowable<Boolean> flowable(String isbn)
    }

    @Requires(property = 'spec.name', value = 'NotFoundSpec')
    @Controller(value = "/not-found", produces = MediaType.TEXT_PLAIN)
    static class InventoryController {
        Map<String, Boolean> stock = [
                '1234': true
        ]

        @Get('/maybe/{isbn}')
        Maybe<Boolean> maybe(String isbn) {
            Boolean value = stock[isbn]
            if (value != null) {
                return Maybe.just(value)
            }
            return Maybe.empty()
        }

        @Get(value = '/flowable/{isbn}', processes = MediaType.TEXT_EVENT_STREAM)
        Flowable<Boolean> flowable(String isbn) {
            Boolean value = stock[isbn]
            if (value != null) {
                return Flowable.just(value)
            }
            return Flowable.empty()
        }
    }
}
