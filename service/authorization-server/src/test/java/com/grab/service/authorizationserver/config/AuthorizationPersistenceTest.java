package com.grab.service.authorizationserver.config;

import com.grab.service.authorizationserver.core.grant.AuthorizationTokenSupport;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AuthorizationPersistenceTest {
    @Test
    void reloadsAuthorizationCodeAfterSavingSellerRevisions() {
        try (PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:16-alpine")) {
            database.start();
            DriverManagerDataSource dataSource = new DriverManagerDataSource(
                    database.getJdbcUrl(), database.getUsername(), database.getPassword());
            Flyway flyway = Flyway.configure().dataSource(dataSource).locations("classpath:db/migration/authorization").load();
            flyway.migrate();

            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            AuthorizationIssuerSecurityConfig config = new AuthorizationIssuerSecurityConfig();
            RegisteredClientRepository clients = new JdbcRegisteredClientRepository(jdbc);
            OAuth2AuthorizationService authorizations = config.authorizationService(jdbc);
            RegisteredClient client = RegisteredClient.withId("seller-client")
                    .clientId("seller-bff")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("http://localhost:8080/login/oauth2/code/seller")
                    .build();
            clients.save(client);

            Instant issuedAt = Instant.now();
            Instant expiresAt = issuedAt.plusSeconds(60);
            OAuth2AuthorizationCode code = new OAuth2AuthorizationCode("test-code", issuedAt, expiresAt);
            OAuth2Authorization initial = OAuth2Authorization.withRegisteredClient(client)
                    .principalName("seller")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .token(code)
                    .build();
            authorizations.save(initial);

            OAuth2Authorization contextual = OAuth2Authorization.from(initial)
                    .attribute(AuthorizationTokenSupport.VERSION, 3L)
                    .attribute(AuthorizationTokenSupport.MERCHANT_REVISION, 4L)
                    .attribute(AuthorizationTokenSupport.STOREFRONT_REVISION, 5L)
                    .build();
            authorizations.save(contextual);

            OAuth2TokenType codeType = new OAuth2TokenType("code");
            OAuth2Authorization restored = authorizations.findByToken("test-code", codeType);
            assertThat(restored).isNotNull();
            assertThat(restored.getAttributes()).containsEntry(AuthorizationTokenSupport.VERSION, 3L)
                    .containsEntry(AuthorizationTokenSupport.MERCHANT_REVISION, 4L)
                    .containsEntry(AuthorizationTokenSupport.STOREFRONT_REVISION, 5L);
        }
    }
}
