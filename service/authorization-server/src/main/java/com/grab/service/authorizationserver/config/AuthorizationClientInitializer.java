package com.grab.service.authorizationserver.config;

import com.grab.service.authorizationserver.config.AuthorizationClientProperties.Client;
import com.grab.service.authorizationserver.port.outbound.GrantFamilyStorePort;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class AuthorizationClientInitializer implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final JdbcRegisteredClientRepository clients;
    private final GrantFamilyStorePort store;
    private final AuthorizationIssuerProperties properties;
    private final AuthorizationClientPolicies policies;

    public AuthorizationClientInitializer(JdbcTemplate jdbc, GrantFamilyStorePort store,
                                          AuthorizationIssuerProperties properties, AuthorizationClientPolicies policies) {
        this.jdbc = jdbc;
        this.clients = new JdbcRegisteredClientRepository(jdbc);
        this.store = store;
        this.properties = properties;
        this.policies = policies;
    }

    @Override
    public void run(ApplicationArguments args) {
        store.inTransaction(() -> {
            jdbc.query("SELECT pg_advisory_xact_lock(?)", rows -> {}, properties.clientInitAdvisoryLock());
            List<RegisteredClient> registrations = new ArrayList<>();
            Set<String> ids = new HashSet<>();
            for (Client client : policies.all()) {
                RegisteredClient registration = registration(client);
                String id = registration.getId();
                RegisteredClient stored = clients.findById(id);
                if (!ids.add(id) || (stored != null && !stored.getClientId().equals(client.clientId()))) {
                    throw new IllegalStateException("Authorization registration ID is already in use");
                }
                registrations.add(registration);
            }
            registrations.forEach(clients::save);
            return null;
        });
    }

    private RegisteredClient registration(Client client) {
        RegisteredClient previous = clients.findByClientId(client.clientId());
        String id = previous == null ? registrationId(client) : previous.getId();
        ClientSettings clientSettings = ClientSettings.builder().requireProofKey(true).build();
        java.time.Duration accessTtl = client.accessTokenTtl() != null ? client.accessTokenTtl() : properties.accessTokenTtl();
        java.time.Duration refreshTtl = client.refreshTokenTtl() != null ? client.refreshTokenTtl() : properties.refreshTokenTtl();
        TokenSettings tokenSettings = TokenSettings.builder().reuseRefreshTokens(false)
                .accessTokenTimeToLive(accessTtl)
                .refreshTokenTimeToLive(refreshTtl).build();
        RegisteredClient.Builder builder = RegisteredClient.withId(id)
                .clientId(client.clientId())
                .clientName(client.clientName())
                .clientSecret(client.clientSecretHash())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUris(uris -> uris.addAll(client.redirectUris()))
                .postLogoutRedirectUris(uris -> uris.addAll(client.postLogoutRedirectUris()))
                .scopes(scopes -> scopes.addAll(client.scopes()))
                .clientSettings(clientSettings)
                .tokenSettings(tokenSettings);
        if (previous != null) {
            builder.clientIdIssuedAt(previous.getClientIdIssuedAt());
        }
        return builder.build();
    }

    private static String registrationId(Client client) {
        if (client.registrationId() != null) {
            return client.registrationId();
        }
        String key = "grab-authorization-client:" + client.clientId();
        byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
        UUID uuid = UUID.nameUUIDFromBytes(bytes);
        return uuid.toString();
    }
}
