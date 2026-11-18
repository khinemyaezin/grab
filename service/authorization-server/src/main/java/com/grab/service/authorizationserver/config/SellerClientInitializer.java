package com.grab.service.authorizationserver.config;

import com.grab.service.authorizationserver.core.grant.SellerContextGrant;
import com.grab.service.authorizationserver.port.outbound.GrantFamilyStorePort;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

@Component
public class SellerClientInitializer implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final RegisteredClientRepository clients;
    private final GrantFamilyStorePort store;
    private final SellerIssuerProperties properties;

    public SellerClientInitializer(JdbcTemplate jdbc,
                                   RegisteredClientRepository clients,
                                   GrantFamilyStorePort store,
                                   SellerIssuerProperties properties) {
        this.jdbc = jdbc;
        this.clients = clients;
        this.store = store;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.clientSecretHash().startsWith("{bcrypt}")) {
            throw new IllegalStateException("SELLER_OIDC_CLIENT_SECRET_HASH must contain a delegated BCrypt hash");
        }
        store.inTransaction(() -> {
            jdbc.execute("SELECT pg_advisory_xact_lock(" + properties.clientInitAdvisoryLock() + ")");
            RegisteredClient previous = clients.findByClientId(properties.clientId());
            RegisteredClient.Builder registration = previous == null
                    ? RegisteredClient.withId("grab-seller-browser") : RegisteredClient.from(previous);
            RegisteredClient.Builder sellerBuilder = registration.clientId(properties.clientId())
                    .clientSecret(properties.clientSecretHash())
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .authorizationGrantType(SellerContextGrant.TYPE)
                    .redirectUris(uris -> {
                        uris.clear();
                        uris.add(properties.redirectUri());
                    })
                    .postLogoutRedirectUris(uris -> {
                        uris.clear();
                        uris.add(properties.postLogoutRedirectUri());
                    })
                    .scopes(scopes -> {
                        scopes.clear();
                        scopes.addAll(properties.scopes());
                    })
                    .clientSettings(ClientSettings.builder().requireProofKey(true).build())
                    .tokenSettings(TokenSettings.builder().reuseRefreshTokens(false)
                            .accessTokenTimeToLive(properties.accessTokenTtl())
                            .refreshTokenTimeToLive(properties.refreshTokenTtl()).build());
            clients.save(sellerBuilder.build());
            return null;
        });
    }
}
