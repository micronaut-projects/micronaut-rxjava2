package io.micronaut.rxjava2.http.client.streaming

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.rxjava2.http.client.RxStreamingHttpClient
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.reactivex.Flowable
import org.reactivestreams.Publisher
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class JsonStreamSpec extends Specification {

    @AutoCleanup
    @Shared
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer, [
            'spec.name': 'JsonStreamSpec'
    ])

    @AutoCleanup
    @Shared
    RxStreamingHttpClient client = embeddedServer.applicationContext.createBean(RxStreamingHttpClient, embeddedServer.getURL())

    void "test read JSON stream demand all"() {
        when:
        List<Map> jsonObjects = client.jsonStream(HttpRequest.GET(
                '/jsonstream/books'
        )).toList().blockingGet()

        then:
        jsonObjects.size() == 2
        jsonObjects[0].title == 'The Stand'
        jsonObjects[1].title == 'The Shining'

        cleanup:
        client.stop()
    }

    @Requires(property = 'spec.name', value = 'JsonStreamSpec')
    @Controller("/jsonstream/books")
    @ExecuteOn(TaskExecutors.IO)
    static class BookController {

        @Get(produces = MediaType.APPLICATION_JSON_STREAM)
        Publisher<Book> list() {
            return Flowable.just(new Book(title: "The Stand"), new Book(title: "The Shining"))
        }
    }

    static class Book {
        String title
    }
}
