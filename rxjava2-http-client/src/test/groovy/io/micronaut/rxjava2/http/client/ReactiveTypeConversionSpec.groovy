package io.micronaut.rxjava2.http.client

import io.micronaut.core.convert.ConversionService
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.Observable
import spock.lang.Specification
import spock.lang.Unroll

@MicronautTest
class ReactiveTypeConversionSpec extends Specification {

    @Unroll
    void 'test converting reactive type #from.getClass().getSimpleName() to #target'() {
        expect:
        ConversionService.SHARED.convert(from, target).isPresent()

        where:
        from                   | target
        Completable.complete() | Observable
        Completable.complete() | Flowable
        Completable.complete() | Single
        Completable.complete() | Maybe
    }
}
