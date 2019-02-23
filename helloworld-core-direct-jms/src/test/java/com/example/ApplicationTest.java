package com.example;

import java.util.Map;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.JMSFactoryType;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants;
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
     * The client connection is configured using "Directly instantiating JMS Resources without using JNDI". See
     * https://activemq.apache.org/artemis/docs/latest/using-jms.html.
     * The client programming model is JMS.
     * @throws Exception
     */
    @Test
    public void main() throws Exception {
        var configuration = new TransportConfiguration(NettyConnectorFactory.class.getName(),
                Map.of(TransportConstants.PORT_PROP_NAME, 61616));
        var factory = ActiveMQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, configuration);

        try (var connection = factory.createConnection();
                var session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
            var queue = session.createQueue("DefaultQueue");
            var producer = session.createProducer(queue);
            var consumer = session.createConsumer(queue);

            connection.start();
            var expected = session.createTextMessage("Hello world!");
            producer.send(expected);
            var actual = (TextMessage) consumer.receive(1000L);

            assertThat(actual.getText()).isEqualTo(expected.getText());
        }
    }
}
