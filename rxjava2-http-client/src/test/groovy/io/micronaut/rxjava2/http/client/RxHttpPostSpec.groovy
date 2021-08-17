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

import groovy.transform.EqualsAndHashCode
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.core.annotation.Introspected
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.multipart.CompletedFileUpload
import io.micronaut.runtime.server.EmbeddedServer
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.functions.Function
import spock.lang.AutoCleanup
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Specification

import javax.validation.Valid
import javax.validation.constraints.NotNull

/**
 * @author Graeme Rocher
 * @since 1.0
 */
class RxHttpPostSpec extends Specification {

    @AutoCleanup
    @Shared
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer, ['spec.name': 'RxHttpPostSpec'])

    @Shared
    @AutoCleanup
    ApplicationContext context = embeddedServer.applicationContext

    @Shared
    @AutoCleanup
    RxHttpClient client = context.createBean(RxHttpClient, embeddedServer.getURL())

    @Shared
    @AutoCleanup
    RxStreamingHttpClient streamingHttpClient = context.createBean(RxStreamingHttpClient, embeddedServer.getURL())

    void "test simple post exchange request with JSON"() {
        when:
        Flowable<HttpResponse<Book>> flowable = client.exchange(
                HttpRequest.POST("/post/simple", new Book(title: "The Stand", pages: 1000))
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .header("X-My-Header", "Foo"),

                Book
        )
        HttpResponse<Book> response = flowable.blockingFirst()
        Optional<Book> body = response.getBody()

        then:
        response.status == HttpStatus.OK
        response.contentType.get() == MediaType.APPLICATION_JSON_TYPE
        response.contentLength == 34
        body.isPresent()
        body.get() instanceof Book
        body.get().title == 'The Stand'
    }

    void "test simple post retrieve request with JSON"() {
        when:
        Flowable<Book> flowable = client.retrieve(
                HttpRequest.POST("/post/simple", new Book(title: "The Stand", pages: 1000))
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .header("X-My-Header", "Foo"),

                Book
        )
        Book book = flowable.blockingFirst()

        then:
        book.title == "The Stand"
    }

    void "test simple post retrieve blocking request with JSON"() {
        when:
        BlockingHttpClient blockingHttpClient = client.toBlocking()
        Book book = blockingHttpClient.retrieve(
                HttpRequest.POST("/post/simple", new Book(title: "The Stand", pages: 1000))
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .header("X-My-Header", "Foo"),

                Book
        )

        then:
        book.title == "The Stand"
    }

    void "test reactive single post retrieve request with JSON"() {
        when:
        Flowable<Book> flowable = client.retrieve(
                HttpRequest.POST("/reactive/post/single", Single.just(new Book(title: "The Stand", pages: 1000)))
                        .accept(MediaType.APPLICATION_JSON_TYPE),

                Book
        )
        Book book = flowable.blockingFirst()

        then:
        book.title == "The Stand"
    }

    void "test reactive maybe post retrieve request with JSON"() {
        when:
        Flowable<Book> flowable = client.retrieve(
                HttpRequest.POST("/reactive/post/maybe", Maybe.just(new Book(title: "The Stand", pages: 1000)))
                        .accept(MediaType.APPLICATION_JSON_TYPE),

                Book
        )
        Book book = flowable.blockingFirst()

        then:
        book.title == "The Stand"
    }

    void "test reactive post with unserializable data"() {
        when:
        Flowable<User> flowable = client.retrieve(
                HttpRequest.POST("/reactive/post/user", '{"userName" : "edwin","movies" : [ {"imdbId" : "tt1285016","inCollection": "true"},{"imdbId" : "tt0100502","inCollection" : "false"} ]}')
                        .accept(MediaType.APPLICATION_JSON_TYPE),

                User
        )
        User user = flowable.blockingFirst()

        then:
        def e = thrown(HttpClientResponseException)
        e.response.getBody(Map).get()._embedded.errors[0].message.contains('Cannot construct instance of `io.micronaut.rxjava2.http.client.Movie`')
    }

    void "test reactive post error handling"() {
        when:
        Flowable<User> flowable = client.retrieve(
                HttpRequest.POST("/reactive/post/user-error", '{"userName":"edwin","movies":[]}')
                        .accept(MediaType.APPLICATION_JSON_TYPE),

                Argument.of(User),
                Argument.of(User)
        )
        User user = flowable.onErrorResumeNext((Function){ t ->
            Flowable.just(((HttpClientResponseException) t).response.getBody(User).get())
        }).blockingFirst()

        then:
        user.userName == "edwin"
    }

    @IgnoreIf({env["GITHUB_WORKFLOW"]})
    // investigate intermitten issues with this test on Github Actions
    void "test reactive post error handling without specifying error body type"() {
        when:
        Flowable<User> flowable = client.retrieve(
                HttpRequest.POST("/reactive/post/user-error", '{"userName":"edwin","movies":[]}')
                        .accept(MediaType.APPLICATION_JSON_TYPE),

                Argument.of(User)
        )
        User user = flowable.onErrorResumeNext((Function){ t ->
            if (t instanceof HttpClientResponseException) {
                try {
                    return Flowable.just(((HttpClientResponseException) t).response.getBody(User).get())
                } catch (e) {
                    return Flowable.error(e)
                }
            } else {
                return Flowable.error(t)
            }
        }).blockingFirst()

        then:
        user.userName == "edwin"
    }

    void "test posting an array of simple types"() {
        List<Boolean> booleans = streamingHttpClient.jsonStream(
                HttpRequest.POST("/reactive/post/booleans", "[true, true, false]"),
                Boolean.class
        ).toList().blockingGet()

        expect:
        booleans[0] == true
        booleans[1] == true
        booleans[2] == false
    }

    void "test creating a person"() {
        Flowable<Person> flowable = client.retrieve(
                HttpRequest.POST("/reactive/post/person", 'firstName=John')
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.APPLICATION_JSON_TYPE),

                Argument.of(Person)
        )

        when:
        Person person = flowable.blockingFirst()

        then:
        thrown(HttpClientResponseException)
    }

    void "test a local error handler that returns a single"() {
        Flowable<Person> flowable = client.retrieve(
                HttpRequest.POST("/reactive/post/error", '{"firstName": "John"}'),
                Argument.of(Person),
                Argument.of(String)
        )

        when:
        flowable.blockingFirst()

        then:
        def ex = thrown(HttpClientResponseException)
        ex.status == HttpStatus.NOT_FOUND
        ex.response.getBody(String).get() == "illegal.argument"
    }

    @Introspected
    static class Person {

        @NotNull
        String firstName
        @NotNull
        String lastName
    }

    @Requires(property = 'spec.name', value = 'RxHttpPostSpec')
    @Controller('/reactive/post')
    static class ReactivePostController {

        @Post('/single')
        Single<Book> simple(@Body Single<Book> book) {
            return book
        }

        @Post('/maybe')
        Maybe<Book> maybe(@Body Maybe<Book> book) {
            return book
        }

        @Post("/user")
        Single<HttpResponse<User>> postUser(@Body Single<User> user) {
            return user.map({ User u->
                return HttpResponse.ok(u)
            })
        }

        @Post("/user-error")
        Single<HttpResponse<User>> postUserError(@Body Single<User> user) {
            return user.map({ User u->
                return HttpResponse.badRequest(u)
            })
        }

        @Post(uri = "/booleans")
        Flowable<Boolean> booleans(@Body Flowable<Boolean> booleans) {
            return booleans
        }

        @Post(uri = "/person", consumes = MediaType.APPLICATION_FORM_URLENCODED)
        Single<HttpResponse<Person>> createPerson(@Valid @Body Person person)  {
            return Single.just(HttpResponse.created(person))
        }

        @Post(uri = "/error")
        Single<HttpResponse<Person>> emitError(@Body Person person)  {
            return Single.error(new IllegalArgumentException())
        }

        @Error(exception = IllegalArgumentException.class)
        Single<HttpResponse<String>> illegalArgument(HttpRequest request, IllegalArgumentException e) {
            Single.just(HttpResponse.notFound("illegal.argument"))
        }
    }

    @Requires(property = 'spec.name', value = 'RxHttpPostSpec')
    @Controller('/post')
    static class PostController {

        @Post('/simple')
        Book simple(@Body Book book, @Header String contentType, @Header long contentLength, @Header accept, @Header('X-My-Header') custom) {
            assert contentType == MediaType.APPLICATION_JSON
            assert contentLength == 34
            assert accept == MediaType.APPLICATION_JSON
            assert custom == 'Foo'
            return book
        }

        @Post('/query')
        Book simple(@Body Book book, @QueryValue String title) {
            assert title == book.title
            return book
        }

        @Post(uri = '/query/url-encoded', consumes = MediaType.APPLICATION_FORM_URLENCODED)
        Book simpleUrlEncoded(@Body Book book, String title) {
            assert title == book.title
            return book
        }


        @Post('/queryNoBody')
        Book simple(@QueryValue("title") String title) {
            return new Book(title: title, pages: 0)
        }

        @Post('/noBody')
        String noBody(@Header("Content-Length") String contentLength) {
            return contentLength
        }

        @Post('/title/{title}')
        Book title(@Body Book book, String title, @Header String contentType, @Header long contentLength, @Header accept, @Header('X-My-Header') custom) {
            assert title == book.title
            assert contentType == MediaType.APPLICATION_JSON
            assert contentLength == 34
            assert accept == MediaType.APPLICATION_JSON
            assert custom == 'Foo'
            return book
        }

        @Post(uri = '/form', consumes = MediaType.APPLICATION_FORM_URLENCODED)
        Book form(@Body Book book, @Header String contentType, @Header long contentLength, @Header accept, @Header('X-My-Header') custom) {
            assert contentType == MediaType.APPLICATION_FORM_URLENCODED
            assert contentLength == 26
            assert accept == MediaType.APPLICATION_JSON
            assert custom == 'Foo'
            return book
        }

        @Post(uri = "/multipleParams",
                consumes = [MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA],
                produces = MediaType.TEXT_PLAIN)
        String multipleParams(@Body Map data) {
            if (data.param instanceof Collection) {
                return ((Collection) data.param).join(",")
            } else {
                return "value=${data.param}"
            }
        }

        @Post(uri = "/multipleParamsBody",
                consumes = [MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA],
                produces = MediaType.TEXT_PLAIN)
        String multipleParams(@Body Params data) {
            return data.param.join(",")
        }

        @Post(uri = "/multipartCharset",
                consumes = MediaType.MULTIPART_FORM_DATA,
                produces = MediaType.TEXT_PLAIN)
        String multipartCharset(@Body CompletedFileUpload file) {
            return file.fileUpload.getCharset()
        }

        @Post(uri = "/booleans")
        List<Boolean> booleans(@Body List<Boolean> booleans) {
            return booleans
        }

        @Post("/requestObject")
        String requestObject(HttpRequest<Object> request) {
            "request-object"
        }

        @Post(uri = "/bodyParts", produces = MediaType.TEXT_PLAIN)
        String bodyParts(String name, Integer id) {
            "$id - $name"
        }

        @Post(uris = ["/multiple", "/multiple/mappings"])
        String multipleMappings() {
            return "multiple mappings"
        }

        @Post(uri = "/emptyBody")
        HttpResponse emptyBody() {
            HttpResponse.noContent()
        }
    }

    static class Params {
        List<String> param
    }

    @EqualsAndHashCode
    @Introspected
    static class Book {
        String title
        Integer pages
    }
}
