package com.example;

import org.apache.activemq.artemis.api.core.ActiveMQSecurityException;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import org.junit.jupiter.api.Test;

public class ApplicationTest {

    private static Application application;

    @BeforeAll
    public static void beforeAll() throws Exception {
        application = new Application();
        application.start();
    }

    @AfterAll
    public static void afterAll() throws Exception {
        if (application != null) {
            application.stop();
        }
    }

    @Test
    public void main() throws Exception {
        try (var locator = ActiveMQClient.createServerLocator("tcp://localhost:61616");
                var factory = locator.createSessionFactory();
                var session = factory.createSession("employee", "pemployee", false, true, true, false, 0);) {
            var producer = session.createProducer("DefaultQueue");
            var consumer = session.createConsumer("DefaultQueue");

            session.start();
            var expected = session.createMessage(true).writeBodyBufferString("Hello world!");
            producer.send(expected);
            var actual = consumer.receive(1000L);

            assertThat(actual.getBodyBuffer().readString()).isEqualTo(expected.getBodyBuffer().readString());
        }
    }

    @Test
    public void unauthenticatedShouldFail() {
        var actual = catchThrowable(() -> {
            try (var locator = ActiveMQClient.createServerLocator("tcp://localhost:61616");
                    var factory = locator.createSessionFactory();
                    var session = factory.createSession(true, true);) {
            }
        });

        assertThat(actual).isInstanceOf(ActiveMQSecurityException.class).hasNoCause();
    }

    @Test
    public void unauthorizedShouldFail() {
        var actual = catchThrowable(() -> {
            try (var locator = ActiveMQClient.createServerLocator("tcp://localhost:61616");
                    var factory = locator.createSessionFactory();
                    var session = factory.createSession("customer", "pcustomer", false, true, true, false, 0);) {
                var producer = session.createProducer("DefaultQueue");

                session.start();
                producer.send(session.createMessage(true).writeBodyBufferString("Hello world!"));
            }
        });

        assertThat(actual).isInstanceOf(ActiveMQSecurityException.class).hasNoCause();
    }
}
