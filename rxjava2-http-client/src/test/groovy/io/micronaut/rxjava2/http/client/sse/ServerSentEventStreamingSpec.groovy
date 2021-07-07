package io.micronaut.rxjava2.http.client.sse

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.sse.Event
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.reactivex.Flowable
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class ServerSentEventStreamingSpec extends Specification {

    @Shared
    @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer, [
            'spec.name': 'ServerSentEventStreamingSpec'
    ])

    @AutoCleanup
    @Shared
    RxSseClient sseClient = embeddedServer.applicationContext.createBean(RxSseClient, embeddedServer.getURL())

    void "test consume SSE stream with RxSseClient"() {
        when:
        List<Event<Product>> results = sseClient.eventStream("/stream/sse/pojo/events", Product).toList().blockingGet()

        then:
        results[0].data.name == "Apple"
        results[1].data.name == "Orange"
        results[1].data.quantity == 2
        results[1].id == 'o1'
        results[2].data.name == "Banana"
        results[3].data.name == "Kiwi"
    }

    static List<Event<Product>> dataSet() {
        [
                Event.of(new Product(name: "Apple")),
                Event.of(new Product(name: "Orange", quantity: 2))
                        .id('o1')
                        .comment("From Valencia"),
                Event.of(new Product(name: "Banana", quantity: 5)),
                Event.of(new Product(name: "Kiwi", quantity: 15))
                        .comment("Green")
                        .id('k1')

        ]
    }

    @Requires(property = 'spec.name', value = 'ServerSentEventStreamingSpec')
    @Controller("/stream/sse")
    @ExecuteOn(TaskExecutors.IO)
    static class SseController {
        @Get(value = '/pojo/events', produces = MediaType.TEXT_EVENT_STREAM)
        Flowable<Event<Product>> pojoEventStream() {
            return Flowable.fromIterable(dataSet())
        }
    }

    @EqualsAndHashCode
    @ToString(includePackage = false)
    static class Product {
        String name
        int quantity = 1
    }
}