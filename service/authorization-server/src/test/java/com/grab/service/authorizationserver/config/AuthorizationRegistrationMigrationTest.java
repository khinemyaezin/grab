package com.grab.service.authorizationserver.config;

import com.grab.service.authorizationserver.adapter.inbound.web.AccountFormAuthenticationProvider;
import com.grab.service.authorizationserver.adapter.outbound.jdbc.JdbcGrantFamilyStoreAdapter;
import com.grab.service.authorizationserver.config.AuthorizationClientProperties.Client;
import com.grab.service.authorizationserver.core.grant.AuthorizationTokenSupport;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.testcontainers.containers.PostgreSQLContainer;

import static com.grab.service.authorizationserver.config.AuthorizationClientPoliciesTest.client;
import static com.grab.service.authorizationserver.config.AuthorizationClientPoliciesTest.policies;
import static org.assertj.core.api.Assertions.assertThat;

class AuthorizationRegistrationMigrationTest {
    @Test
    void reconcilesConcurrentStartsAndPreservesLegacyRegistrationAndReplayHistory() throws Exception {
        try (PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:16-alpine")) {
            database.start();
            DriverManagerDataSource dataSource = new DriverManagerDataSource(
                    database.getJdbcUrl(), database.getUsername(), database.getPassword());
            String migrations = "classpath:db/migration/authorization";
            Flyway.configure().dataSource(dataSource).locations(migrations).target("1").load().migrate();
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            JdbcRegisteredClientRepository registrations = new JdbcRegisteredClientRepository(jdbc);
            AuthorizationIssuerSecurityConfig config = new AuthorizationIssuerSecurityConfig();
            OAuth2AuthorizationService authorizations = config.authorizationService(jdbc);
            RegisteredClient legacy = RegisteredClient.withId("legacy-registration")
                    .clientId("portal")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("https://portal.example/old")
                    .build();
            registrations.save(legacy);
            Instant now = Instant.now();
            Instant expiry = now.plusSeconds(3600);
            OAuth2RefreshToken refresh = new OAuth2RefreshToken("current-refresh", now, expiry);
            SimpleGrantedAuthority marker = new SimpleGrantedAuthority("SELLER_LOGIN_VERSION_7");
            UsernamePasswordAuthenticationToken principal =
                    UsernamePasswordAuthenticationToken.authenticated("subject", null, List.of(marker));
            OAuth2AccessToken access = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "current-access", now, expiry);
            Map<String, Object> claims = new HashMap<>();
            claims.put(JwtClaimNames.AUD, List.of("portal-api"));
            OAuth2Authorization original = OAuth2Authorization.withRegisteredClient(legacy)
                    .principalName("subject")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .attribute(Principal.class.getName(), principal)
                    .attribute(AuthorizationTokenSupport.VERSION, 7L)
                    .token(access, metadata -> metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, claims))
                    .refreshToken(refresh)
                    .build();
            authorizations.save(original);
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] bytes = sha256.digest("previous-refresh".getBytes(StandardCharsets.UTF_8));
            String digest = HexFormat.of().formatHex(bytes);
            jdbc.update("INSERT INTO seller_consumed_refresh VALUES (?, ?, CURRENT_TIMESTAMP + interval '1 hour')",
                    digest, original.getId());

            Flyway.configure().dataSource(dataSource).locations(migrations).load().migrate();
            AuthorizationIssuerProperties settings = new AuthorizationIssuerProperties("https://id.example",
                    Duration.ofMinutes(15), Duration.ofDays(7), "localhost", 1199, null, null, null, List.of(),
                    null, null, null, null, 1500, 3000, 30, 60, Duration.ofDays(1), 6021278L);
            JdbcGrantFamilyStoreAdapter store = new JdbcGrantFamilyStoreAdapter(jdbc, dataSource, settings);
            Client portal = client("portal", "configured-registration", "portal-api", "https://portal.example/new");
            Client basic = client("basic", null, "basic-api", "https://basic.example/callback");
            AuthorizationClientPolicies clients = policies("prod", portal, basic);
            AuthorizationClientInitializer first = new AuthorizationClientInitializer(jdbc, store, settings, clients);
            AuthorizationClientInitializer second = new AuthorizationClientInitializer(jdbc, store, settings, clients);

            try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
                Future<?> firstRun = pool.submit(() -> first.run(null));
                Future<?> secondRun = pool.submit(() -> second.run(null));
                firstRun.get(10, TimeUnit.SECONDS);
                secondRun.get(10, TimeUnit.SECONDS);
            }

            RegisteredClient updated = registrations.findByClientId("portal");
            assertThat(updated.getId()).isEqualTo(legacy.getId());
            assertThat(updated.getRedirectUris()).containsExactly("https://portal.example/new");
            assertThat(registrations.findByClientId("basic")).isNotNull();
            OAuth2Authorization migrated = authorizations.findByToken("current-refresh", OAuth2TokenType.REFRESH_TOKEN);
            assertThat(migrated.getRefreshToken().isActive()).isTrue();
            assertThat(AuthorizationTokenSupport.hasAudience(migrated, "portal-api")).isTrue();
            assertThat(migrated.getAttributes()).containsEntry(AuthorizationTokenSupport.VERSION, 7L);
            assertThat(AccountFormAuthenticationProvider.loginVersion(migrated.getAttribute(Principal.class.getName())))
                    .isEqualTo(7L);
            assertThat(store.findConsumedFamily("previous-refresh")).isEqualTo(migrated.getId());
            assertThat(jdbc.queryForObject("SELECT count(*) FROM seller_consumed_refresh", Long.class)).isEqualTo(1L);

            AuthorizationClientPolicies reduced = policies("prod", basic);
            ActiveRegisteredClientRepository active = new ActiveRegisteredClientRepository(registrations, reduced);
            assertThat(active.findById(legacy.getId())).isNull();
            assertThat(active.findByClientId("portal")).isNull();
            assertThat(registrations.findById(legacy.getId())).isNotNull();
        }
    }
}
