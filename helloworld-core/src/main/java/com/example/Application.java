package com.example;

import java.io.File;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.ActiveMQServers;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

public class Application {

    private Configuration configuration;
    private ActiveMQServer server;

    public static void main(String[] args) throws Exception {
        Application application = new Application();

        application.configureServer();
        application.start();
    }

    public void configureServer() throws Exception {
        Config config = ConfigProvider.getConfig();

        configuration = new ConfigurationImpl()
                .setName(config.getValue("core.name", String.class))
                .setPersistenceEnabled(config.getValue("core.persistence-enabled", Boolean.class))
                .setSecurityEnabled(config.getValue("core.security-enabled", Boolean.class));

        String[] acceptors = config.getValue("core.acceptors", String[].class);

        for (int i = 0; i < acceptors.length; i++) {
            configuration.addAcceptorConfiguration(String.format("acceptor-%2d", i), acceptors[i]);
        }
    }

    public void start() throws Exception {
        server = ActiveMQServers.newActiveMQServer(configuration);
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
