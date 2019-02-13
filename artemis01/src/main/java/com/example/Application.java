/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import org.apache.activemq.artemis.api.config.ActiveMQDefaultConfiguration;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.ActiveMQServers;

/**
 *
 * @author Erik
 */
public class Application {

    private ActiveMQServer server;

    public static void main(String[] args) throws Exception {
        var application = new Application();

        application.start();
        System.out.println("Press any key to continue...");
        new BufferedReader(new InputStreamReader(System.in)).read();
        application.stop();
    }

    public void start() throws Exception {
        server = ActiveMQServers.newActiveMQServer(new ConfigurationImpl()
                .setPersistenceEnabled(true)
                .setBindingsDirectory("target/server01/" + ActiveMQDefaultConfiguration.getDefaultBindingsDirectory())
                .setJournalDirectory("target/server01/" + ActiveMQDefaultConfiguration.getDefaultJournalDir())
                .setLargeMessagesDirectory("target/server01/" + ActiveMQDefaultConfiguration.getDefaultLargeMessagesDir())
                .setPagingDirectory("target/server01/" + ActiveMQDefaultConfiguration.getDefaultPagingDir())
                .setSecurityEnabled(false)
                .addAcceptorConfiguration("invm", "vm://0"));
        
        server.start();
        
        server.waitForActivation(1000, TimeUnit.MILLISECONDS);
        
        var queue = server.createQueue(new SimpleString("myAddress"), RoutingType.ANYCAST, new SimpleString("myQueue"), null, false, false);
    }

    public void stop() throws Exception {
        if (server != null && server.isStarted()) {
            server.stop();
        }
    }
}
