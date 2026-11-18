package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import com.grab.framework.security.AuthenticatedActor;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerIssuerClient.Tokens;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.JwtValidationException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SellerOidcClientConfigTest {
    private static final String PUBLIC_ISSUER = "https://public-issuer.example";
    private static final String PROVIDER_PREFIX = "spring.security.oauth2.client.provider.seller.";

    private final ApplicationContextRunner context = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withUserConfiguration(SellerOidcClientConfig.class, ClientTestConfiguration.class)
            .withBean(MeterRegistry.class, SimpleMeterRegistry::new)
            .withPropertyValues("grab.seller-oidc.issuer=" + PUBLIC_ISSUER,
                    "grab.seller-oidc.client-id=seller-bff",
                    "grab.seller-oidc.client-secret=test-secret",
                    "grab.seller-oidc.callback=https://bff.example/login/oauth2/code/seller",
                    "grab.seller-oidc.ui=https://ui.example/",
                    "grab.seller-oidc.post-logout=https://ui.example/");

    @Test
    void usesRegisteredTokenAndJwkUrlsForLiveSessionOperations() throws Exception {
        RSAKey key = new RSAKeyGenerator(2048).keyID("seller-config-test-key").generate();
        JWKSet jwks = new JWKSet(key.toPublicJWK());
        Queue<String> grants = new ConcurrentLinkedQueue<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/configured/token", exchange -> {
            byte[] body = exchange.getRequestBody().readAllBytes();
            grants.add(new String(body, StandardCharsets.UTF_8));
            respond(exchange, """
                    {"access_token":"new-access","refresh_token":"new-refresh","expires_in":60}
                    """);
        });
        server.createContext("/configured/keys", exchange -> respond(exchange, jwks.toString()));
        server.createContext("/oauth2/introspect", exchange -> respond(exchange, """
                {"active":true,"sub":"seller","email":"seller@example.com",
                 "roles":[],"authorities":[],"authentication_version":1}
                """));
        server.start();
        try {
            String backchannel = "http://127.0.0.1:" + server.getAddress().getPort();
            String accessToken = token(key, PUBLIC_ISSUER, "grab-seller-api", "at+jwt");
            String wrongIssuerAccess = token(key, backchannel, "grab-seller-api", "at+jwt");
            String idToken = token(key, PUBLIC_ISSUER, "seller-bff", "JWT");
            String wrongIssuerId = token(key, backchannel, "seller-bff", "JWT");
            Instant expiry = Instant.now().plusSeconds(60);
            Tokens current = new Tokens(accessToken, "refresh", expiry, "seller", idToken);
            Tokens wrongIssuer = new Tokens(wrongIssuerAccess, "refresh", expiry, "seller", idToken);

            context.withPropertyValues("grab.seller-oidc.backchannel=" + backchannel,
                    PROVIDER_PREFIX + "token-uri=" + backchannel + "/configured/token",
                    PROVIDER_PREFIX + "jwk-set-uri=" + backchannel + "/configured/keys").run(application -> {
                assertThat(application).hasNotFailed();
                SellerIssuerClient issuer = application.getBean(SellerIssuerClient.class);
                assertThat(issuer.refresh(current).refresh()).isEqualTo("new-refresh");
                assertThat(issuer.select(current, "own").refresh()).isEqualTo("new-refresh");
                assertThat(grants).hasSize(2);
                assertThat(grants.remove()).contains("grant_type=refresh_token", "refresh_token=refresh");
                assertThat(grants.remove()).contains("grant_type=urn%3Agrab%3Aoauth%3Agrant-type%3Aseller-context",
                        "assignment_id=own");

                AuthenticatedActor actor = issuer.actor(current);
                assertThat(actor.platformUserId()).isEqualTo("seller");
                assertThatThrownBy(() -> issuer.actor(wrongIssuer)).isInstanceOfSatisfying(SellerOidcFailure.class,
                        failure -> assertThat(failure.getStatusCode().value()).isEqualTo(401));

                ClientRegistrationRepository registrations = application.getBean(ClientRegistrationRepository.class);
                ClientRegistration seller = registrations.findByRegistrationId("seller");
                IdTokenDecoders decoders = application.getBean(IdTokenDecoders.class);
                JwtDecoder idTokens = decoders.factory().createDecoder(seller);
                assertThat(idTokens.decode(idToken).getSubject()).isEqualTo("seller");
                assertThatThrownBy(() -> idTokens.decode(wrongIssuerId)).isInstanceOf(JwtValidationException.class);
            });
        } finally {
            server.stop(0);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "token-uri, /relative/token",
            "token-uri, https://some-user@example.com/token",
            "jwk-set-uri, https://issuer.example/keys#fragment",
            "jwk-set-uri, ftp://issuer.example/keys"
    })
    void rejectsInvalidRegisteredUrlsAtStartup(String property, String value) {
        String key = PROVIDER_PREFIX + property;
        context.withPropertyValues(key + "=" + value).run(application -> {
            assertThat(application).hasFailed();
            assertThat(application.getStartupFailure()).hasStackTraceContaining(key + " must be an absolute HTTP(S) URL");
        });
    }

    private static void respond(HttpExchange exchange, String json) throws IOException {
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        try (exchange) {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
        }
    }

    private static String token(RSAKey key, String issuer, String audience, String type) throws JOSEException {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject("seller")
                .audience(audience)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(60)))
                .claim("authentication_version", 1)
                .build();
        JOSEObjectType tokenType = new JOSEObjectType(type);
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(tokenType)
                .keyID(key.getKeyID())
                .build();
        SignedJWT token = new SignedJWT(header, claims);
        RSASSASigner signer = new RSASSASigner(key);
        token.sign(signer);
        return token.serialize();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(OAuth2ClientProperties.class)
    static class ClientTestConfiguration {
        @Bean
        ClientRegistrationRepository clientRegistrationRepository(OAuth2ClientProperties properties) {
            OAuth2ClientPropertiesMapper mapper = new OAuth2ClientPropertiesMapper(properties);
            Map<String, ClientRegistration> registrations = mapper.asClientRegistrations();
            return new InMemoryClientRegistrationRepository(new ArrayList<>(registrations.values()));
        }

        @Bean
        IdTokenDecoders idTokenDecoders(JwtDecoderFactory<ClientRegistration> factory) {
            return new IdTokenDecoders(factory);
        }
    }

    private record IdTokenDecoders(JwtDecoderFactory<ClientRegistration> factory) {}
}
