/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.apache.activemq.artemis.core.security.CheckType;
import org.apache.activemq.artemis.core.security.Role;
import org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager;

/**
 *
 * @author Erik
 */
public class InMemorySecurityManager implements ActiveMQSecurityManager {

    private final Map<InMemoryUser, EnumSet<CheckType>> users;

    public InMemorySecurityManager(Map<InMemoryUser, EnumSet<CheckType>> users) {
        this.users = users;
    }

    @Override
    public boolean validateUser(String user, String password) {
        return users.keySet().stream().anyMatch(u -> u.getUsername().equals(user) && u.getPassword().equals(password));
    }

    @Override
    public boolean validateUserAndRole(String user, String password, Set<Role> roles, CheckType checkType) {
        return users.entrySet().stream().anyMatch(
                e -> e.getKey().getUsername().equals(user)
                && e.getKey().getPassword().equals(password)
                && e.getValue().contains(checkType));
    }
}