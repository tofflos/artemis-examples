package com.example;

import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ApplicationTest {

    private static Application application;

    @BeforeAll
    public static void beforeAll() throws Exception {
        application = new Application();
        application.configureServer();
        application.start();
    }

    @AfterAll
    public static void afterAll() throws Exception {
        if (application != null) {
            application.stop();
        }
    }

    /**
     * This example connects to the server using the CORE protocol.
     * Apache ActiveMQ Artemis does not seem to come with an AMQP client.
     * The client connection is configured using the CORE API. See https://activemq.apache.org/artemis/docs/latest/core.html.
     * The client programming model is the CORE API.
     * @throws Exception
     */
    @Test
    public void main() throws Exception {
        try (var locator = ActiveMQClient.createServerLocator("tcp://localhost:61616");
                var factory = locator.createSessionFactory();
                var session = factory.createSession(true, true);) {
            var producer = session.createProducer("DefaultQueue");
            var consumer = session.createConsumer("DefaultQueue");
            
            session.start();
            var expected = session.createMessage(true).writeBodyBufferString("Hello world!");
            producer.send(expected);
            var actual = consumer.receive(1000L);

            assertThat(actual.getBodyBuffer().readString()).isEqualTo(expected.getBodyBuffer().readString());
        }
    }
}
