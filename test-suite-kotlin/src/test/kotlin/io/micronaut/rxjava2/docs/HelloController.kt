package io.micronaut.rxjava2.docs

import io.micronaut.context.annotation.Requires
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.sse.Event
import io.reactivex.Flowable

@Requires(property = "spec.name", value = "RxHttpClientTest")
@Controller("/hello")
class HelloController {

    @Get(produces = [MediaType.TEXT_PLAIN])
    fun hello(): String = "Hello World"

    @Get("/stream", produces = [MediaType.APPLICATION_JSON_STREAM])
    fun stream(): Flowable<Message> = Flowable.just(Message("Hello"), Message("World"))

    @Get("/events", produces = [MediaType.TEXT_EVENT_STREAM])
    fun events(): Flowable<Event<String>> = Flowable.just(Event.of("Hello"), Event.of("World"))
}
