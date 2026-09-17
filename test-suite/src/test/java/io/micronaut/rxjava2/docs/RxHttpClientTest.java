package io.micronaut.rxjava2.docs;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.sse.Event;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import java.util.List;

// tag::imports[]
import io.micronaut.http.client.annotation.Client;
import io.micronaut.rxjava2.http.client.RxHttpClient;
import io.micronaut.rxjava2.http.client.RxStreamingHttpClient;
import io.micronaut.rxjava2.http.client.sse.RxSseClient;
import jakarta.inject.Inject;
// end::imports[]

import static org.junit.jupiter.api.Assertions.assertEquals;

@Property(name = "spec.name", value = "RxHttpClientTest")
@MicronautTest
class RxHttpClientTest {

    // tag::clients[]
    @Inject @Client("/") RxHttpClient httpClient; // <1>
    @Inject @Client("/") RxSseClient sseClient; // <2>
    @Inject @Client("/") RxStreamingHttpClient streamingClient; // <3>
    // end::clients[]

    @Test
    void theRegularClientReturnsRxJavaTypes() {
        // tag::retrieve[]
        String greeting = httpClient.retrieve("/hello").blockingFirst();
        // end::retrieve[]
        assertEquals("Hello World", greeting);
    }

    @Test
    void theStreamingClientReturnsAFlowableOfTheStreamedItems() {
        List<String> words = streamingClient.jsonStream(HttpRequest.GET("/hello/stream"), Message.class)
            .map(Message::text)
            .toList()
            .blockingGet();
        assertEquals(List.of("Hello", "World"), words);
    }

    @Test
    void theSseClientReturnsAFlowableOfEvents() {
        List<String> words = sseClient.eventStream(HttpRequest.GET("/hello/events"), String.class)
            .map(Event::getData)
            .toList()
            .blockingGet();
        assertEquals(List.of("Hello", "World"), words);
    }
}
