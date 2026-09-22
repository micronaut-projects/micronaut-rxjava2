package io.micronaut.rxjava2.docs

import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

// tag::imports[]
import io.micronaut.http.client.annotation.Client
import io.micronaut.rxjava2.http.client.RxHttpClient
import io.micronaut.rxjava2.http.client.RxStreamingHttpClient
import io.micronaut.rxjava2.http.client.sse.RxSseClient
import jakarta.inject.Inject
// end::imports[]

@Property(name = "spec.name", value = "RxHttpClientTest")
@MicronautTest
class RxHttpClientTest extends Specification {

    // tag::clients[]
    @Inject @Client("/") RxHttpClient httpClient // <1>
    @Inject @Client("/") RxSseClient sseClient // <2>
    @Inject @Client("/") RxStreamingHttpClient streamingClient // <3>
    // end::clients[]

    void "the regular client returns RxJava types"() {
        when:
        // tag::retrieve[]
        String greeting = httpClient.retrieve("/hello").blockingFirst()
        // end::retrieve[]

        then:
        greeting == "Hello World"
    }

    void "the streaming client returns a Flowable of the streamed items"() {
        when:
        List<String> words = streamingClient.jsonStream(HttpRequest.GET("/hello/stream"), Message)
            .map { it.text }
            .toList()
            .blockingGet()

        then:
        words == ["Hello", "World"]
    }

    void "the SSE client returns a Flowable of events"() {
        when:
        List<String> words = sseClient.eventStream(HttpRequest.GET("/hello/events"), String)
            .map { it.data }
            .toList()
            .blockingGet()

        then:
        words == ["Hello", "World"]
    }
}
