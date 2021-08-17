package io.micronaut.rxjava2.http.client

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.core.async.publisher.Publishers
import io.micronaut.core.util.StringUtils
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Filter
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.http.client.ProxyHttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import io.micronaut.http.uri.UriBuilder
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.rxjava2.http.client.proxy.RxProxyHttpClient
import org.reactivestreams.Publisher
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class ProxyRequestSpec extends Specification {

    @AutoCleanup
    @Shared
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer, [
            'spec.name': 'ProxyRequestSpec'
    ])

    @Shared
    @AutoCleanup
    RxHttpClient client = embeddedServer.applicationContext.createBean(RxHttpClient, embeddedServer.URL)

    void "test proxy GET request from filter"() {
        when:"A GET request is proxied"
        HttpResponse<String> response = client.exchange("/proxy/get", String).blockingFirst()

        then:
        response.header('X-My-Response-Header') == 'YYY'
        response.body() == 'good XXX'

        when:"A GET request with an error is requested"
        client.exchange("/proxy/error", String).blockingFirst()

        then:
        HttpClientResponseException e = thrown()
        e.response.header('X-My-Response-Header') == 'YYY'
        e.response.getBody(Map).get()._embedded.errors[0].message == "Internal Server Error: Bad things happened"

        when:"A GET request with a 404"
        client.exchange("/proxy/notThere", String).blockingFirst()

        then:
        e = thrown(HttpClientResponseException)
        e.response.header('X-My-Response-Header') == 'YYY'
        e.response.getBody(Map).get()._embedded.errors[0].message == "Page Not Found"
    }

    @Requires(property = 'spec.name', value = 'ProxyRequestSpec')
    @Controller("/real")
    static class TargetController {
        @Get("/get")
        @Produces(MediaType.TEXT_PLAIN)
        String index(HttpHeaders headers) {
            return "good " + headers.get("X-My-Request-Header")
        }


        @Get("/error")
        @Produces(MediaType.TEXT_PLAIN)
        String error() {
            throw new RuntimeException("Bad things happened")
        }
    }

    @Requires(property = 'spec.name', value = 'ProxyRequestSpec')
    @Filter("/proxy/**")
    static class ProxyFilter implements HttpServerFilter {
        private final RxProxyHttpClient client
        private final EmbeddedServer embeddedServer

        ProxyFilter(RxProxyHttpClient client,
                    EmbeddedServer embeddedServer) {
            this.client = client
            this.embeddedServer = embeddedServer
        }

        @Override
        Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request,
                                                   ServerFilterChain chain) {
            Publishers.map(client.proxy(
                    request.mutate()
                            .uri { UriBuilder b ->
                                b.with {
                                    scheme("http")
                                    host(embeddedServer.host)
                                    port(embeddedServer.port)
                                    replacePath(StringUtils.prependUri(
                                            "/real",
                                            request.path.substring("/proxy".length())
                                    ))
                                }
                            }
                            .header("X-My-Request-Header", "XXX")
            ), { it.header("X-My-Response-Header", "YYY") })
        }
    }
}
