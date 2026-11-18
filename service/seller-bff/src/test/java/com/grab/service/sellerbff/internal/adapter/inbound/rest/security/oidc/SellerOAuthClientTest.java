package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SellerOAuthClientTest {
    private final ApplicationContextRunner propertiesContext = new ApplicationContextRunner()
            .withUserConfiguration(Binding.class)
            .withPropertyValues("grab.seller-oidc.issuer=https://issuer.example",
                    "grab.seller-oidc.audience=grab-seller-api",
                    "grab.seller-oidc.scopes=openid,profile,email",
                    "grab.seller-oidc.client-id=seller-bff",
                    "grab.seller-oidc.client-secret=test-secret",
                    "grab.seller-oidc.introspection-endpoint=https://issuer.example/introspect",
                    "grab.seller-oidc.revocation-endpoint=https://issuer.example/revoke",
                    "grab.seller-oidc.logout-endpoint=https://issuer.example/logout",
                    "grab.seller-oidc.authorization-endpoint=https://issuer.example/authorize",
                    "grab.seller-oidc.token-endpoint=https://issuer.example/token",
                    "grab.seller-oidc.jwk-set-endpoint=https://issuer.example/keys",
                    "grab.seller-oidc.callback=https://bff.example/login/oauth2/code/seller",
                    "grab.seller-oidc.ui=https://ui.example/",
                    "grab.seller-oidc.post-logout=https://ui.example/",
                    "grab.seller-oidc.account-host=account",
                    "grab.seller-oidc.registration-port=1200",
                    "grab.seller-oidc.revocation-retry-delay=30s",
                    "grab.seller-oidc.revocation-token-ttl=7d",
                    "grab.seller-oidc.revocation-claim-lease=60s",
                    "grab.seller-oidc.revocation-batch-size=25");

    @Test
    void refreshUsesSpringOAuthClientAndKeepsRefreshTokenWhenProviderOmitsReplacement() {
        propertiesContext.run(application -> {
            SellerOidcProperties properties = application.getBean(SellerOidcProperties.class);
            AtomicReference<OAuth2RefreshTokenGrantRequest> submitted = new AtomicReference<>();
            OAuth2AccessTokenResponseClient<OAuth2RefreshTokenGrantRequest> refreshClient = request -> {
                submitted.set(request);
                return OAuth2AccessTokenResponse.withToken("replacement-access")
                        .tokenType(OAuth2AccessToken.TokenType.BEARER)
                        .expiresIn(300)
                        .build();
            };
            SellerOAuthClient client = oauth(properties, refreshClient, ignored -> activePrincipal());
            SellerOAuthClient.Tokens current = tokens("opaque-access", "existing-refresh");

            SellerOAuthClient.Tokens refreshed = client.refresh(current);

            assertThat(submitted.get().getRefreshToken().getTokenValue()).isEqualTo("existing-refresh");
            assertThat(refreshed.access()).isEqualTo("replacement-access");
            assertThat(refreshed.refresh()).isEqualTo("existing-refresh");
            assertThat(refreshed.subject()).isEqualTo(current.subject());
        });
    }

    @Test
    void introspectionRequiresExpectedIssuerAudienceAndFutureExpiry() {
        propertiesContext.run(application -> {
            SellerOidcProperties properties = application.getBean(SellerOidcProperties.class);
            SellerOAuthClient valid = oauth(properties, ignored -> null, ignored -> principal(
                    "https://issuer.example", List.of("grab-seller-api"), Instant.now().plusSeconds(60)));
            assertThat(valid.introspect("opaque-access").getName()).isEqualTo("seller-subject");

            SellerOAuthClient wrongAudience = oauth(properties, ignored -> null, ignored -> principal(
                    "https://issuer.example", List.of("other-api"), Instant.now().plusSeconds(60)));
            assertThatThrownBy(() -> wrongAudience.introspect("opaque-access"))
                    .isInstanceOf(SellerOidcFailure.class);

            SellerOAuthClient expired = oauth(properties, ignored -> null, ignored -> principal(
                    "https://issuer.example", List.of("grab-seller-api"), Instant.now().minusSeconds(1)));
            assertThatThrownBy(() -> expired.introspect("opaque-access"))
                    .isInstanceOf(SellerOidcFailure.class);
        });
    }

    private SellerOAuthClient oauth(SellerOidcProperties properties,
                                    OAuth2AccessTokenResponseClient<OAuth2RefreshTokenGrantRequest> refreshClient,
                                    OpaqueTokenIntrospector introspector) {
        ClientRegistration registration = ClientRegistration.withRegistrationId("seller")
                .clientId(properties.clientId()).clientSecret(properties.clientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri(properties.callback()).tokenUri("https://issuer.example/token")
                .authorizationUri("https://issuer.example/authorize").jwkSetUri("https://issuer.example/keys")
                .issuerUri(properties.issuer()).clientName("Seller Central").build();
        SimpleClientHttpRequestFactory requests = new SimpleClientHttpRequestFactory();
        requests.setConnectTimeout(properties.connectTimeout());
        requests.setReadTimeout(properties.readTimeout());
        return new SellerOAuthClient(properties, registration, refreshClient, introspector,
                "https://issuer.example/revoke", "https://issuer.example/logout", requests,
                new SimpleMeterRegistry());
    }

    private SellerOAuthClient.Tokens tokens(String access, String refresh) {
        return new SellerOAuthClient.Tokens(access, refresh, Instant.now().plusSeconds(60),
                "https://issuer.example", "seller-subject", "id-token", null, null, 0, 0, 0);
    }

    private org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal activePrincipal() {
        return principal("https://issuer.example", List.of("grab-seller-api"), Instant.now().plusSeconds(60));
    }

    private org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal principal(
            String issuer, List<String> audience, Instant expiry) {
        return new DefaultOAuth2AuthenticatedPrincipal("seller-subject", Map.of(
                "iss", issuer, "aud", audience, "exp", expiry), List.of());
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(SellerOidcProperties.class)
    static class Binding {}
}
