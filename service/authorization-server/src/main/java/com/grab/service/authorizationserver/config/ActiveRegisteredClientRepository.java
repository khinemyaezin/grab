package com.grab.service.authorizationserver.config;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

final class ActiveRegisteredClientRepository implements RegisteredClientRepository {
    private final RegisteredClientRepository delegate;
    private final AuthorizationClientPolicies policies;

    ActiveRegisteredClientRepository(RegisteredClientRepository delegate, AuthorizationClientPolicies policies) {
        this.delegate = delegate;
        this.policies = policies;
    }

    @Override
    public void save(RegisteredClient client) {
        delegate.save(client);
    }

    @Override
    public RegisteredClient findById(String id) {
        RegisteredClient client = delegate.findById(id);
        return client != null && policies.isActive(client.getClientId()) ? client : null;
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return policies.isActive(clientId) ? delegate.findByClientId(clientId) : null;
    }
}
