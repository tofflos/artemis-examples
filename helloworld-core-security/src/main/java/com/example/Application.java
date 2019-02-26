package com.example;

import java.util.EnumSet;
import java.util.Map;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.security.CheckType;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.impl.ActiveMQServerImpl;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;

public class Application {

    private ActiveMQServer server;

    public static void main(String[] args) throws Exception {
        new Application().start();
    }

    public void start() throws Exception {
        var configuration = new ConfigurationImpl()
                .addAcceptorConfiguration("netty-acceptor-core", "tcp://localhost:61616?protocols=CORE")
                .setPersistenceEnabled(false);

        var securityManager = new InMemorySecurityManager(Map.of(
                new InMemoryUser("customer", "pcustomer"), EnumSet.of(CheckType.CONSUME),
                new InMemoryUser("employee", "pemployee"), EnumSet.of(CheckType.CONSUME, CheckType.SEND)
        ));

        server = new ActiveMQServerImpl(configuration, securityManager);
        server.start();

        AddressSettings addressSettings = new AddressSettings()
                .setDeadLetterAddress(new SimpleString("DeadLetterQueue"))
                .setExpiryAddress(new SimpleString("ExpiryQueue"));

        server.getAddressSettingsRepository().addMatch("*", addressSettings);

        var deadLetterQueue = server.createQueue(new SimpleString("DeadLetterQueue"), RoutingType.ANYCAST, new SimpleString("DeadLetterQueue"), null, false, false);
        var expiryQueue = server.createQueue(new SimpleString("ExpiryQyeye"), RoutingType.ANYCAST, new SimpleString("ExpiryQyeye"), null, false, false);
        var defaultQueue = server.createQueue(new SimpleString("DefaultQueue"), RoutingType.ANYCAST, new SimpleString("DefaultQueue"), null, false, false);
    }

    public void stop() throws Exception {
        if (server != null && server.isStarted()) {
            server.stop();
        }
    }
}
