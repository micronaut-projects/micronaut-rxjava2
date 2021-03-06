package io.micronaut.rxjava2.http.client

import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class RxClientCloseSpec extends Specification {
    def "confirm RxHttpClient can be stopped"() {
        given:
        def client = RxHttpClient.create(new URL("http://localhost"))

        expect:
        client.isRunning()

        when:
        client.stop()
        then:
        new PollingConditions().eventually {
            !client.isRunning()
        }
    }

    def "confirm RxHttpClient can be closed"() {
        given:
        def client = RxHttpClient.create(new URL("http://localhost"))

        expect:
        client.isRunning()

        when:
        client.close()
        then:
        new PollingConditions().eventually {
            !client.isRunning()
        }
    }
}
