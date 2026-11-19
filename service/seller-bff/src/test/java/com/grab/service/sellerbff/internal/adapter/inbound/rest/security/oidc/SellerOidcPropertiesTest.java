package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerOidcSecurityConfig.CorsProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SellerOidcPropertiesTest {
    private final ApplicationContextRunner context = new ApplicationContextRunner()
            .withUserConfiguration(Binding.class)
            .withPropertyValues("grab.seller-oidc.issuer=https://issuer.example",
                    "grab.seller-oidc.audience=grab-seller-api",
                    "grab.seller-oidc.scopes=openid,profile,email",
                    "grab.seller-oidc.client-id=seller-bff",
                    "grab.seller-oidc.client-secret=test-secret",
                    "grab.seller-oidc.introspection-endpoint=https://issuer.example/introspect",
                    "grab.seller-oidc.revocation-endpoint=https://issuer.example/revoke",
                    "grab.seller-oidc.logout-endpoint=https://issuer.example/logout",
                    "grab.seller-oidc.metadata-uri=https://issuer.example/.well-known/openid-configuration",
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
    void binding_usesCompatibleDefaults() {
        context.run(application -> {
            SellerOidcProperties properties = application.getBean(SellerOidcProperties.class);
            assertThat(properties.cookieName()).isEqualTo("GRAB_SELLER_SESSION");
            assertThat(properties.redisNamespace()).isEqualTo("grab:seller");
            assertThat(properties.sessionTimeout()).isEqualTo(Duration.ofDays(7));
            assertThat(properties.connectTimeout()).isEqualTo(Duration.ofSeconds(2));
            assertThat(properties.readTimeout()).isEqualTo(Duration.ofSeconds(6));
            assertThat(properties.revocationRetryDelay()).isEqualTo(Duration.ofSeconds(30));
            assertThat(properties.revocationTokenTtl()).isEqualTo(Duration.ofDays(7));
            assertThat(properties.revocationClaimLease()).isEqualTo(Duration.ofSeconds(60));
            assertThat(properties.revocationBatchSize()).isEqualTo(25);
        });
    }

    @Test
    void binding_missingOrMalformedSettings_failsAtStartup() {
        rejects("client-secret=", "client-secret is required");
        rejects("ui=", "ui must be an absolute");
        rejects("post-logout=/relative", "post-logout must be an absolute");
        rejects("callback=relative/path", "callback must be an absolute");
        rejects("callback=https://bff.example/login/oauth2/code/other", "callback must end in");
        rejects("issuer=ftp://issuer.example", "issuer must be an absolute");
        rejects("session-timeout=0s", "session-timeout must be a positive");
        rejects("read-timeout=11s", "connect-timeout and read-timeout must total at most");
        rejects("revocation-retry-delay=0s", "revocation-retry-delay must be at least 1ms");
        rejects("revocation-token-ttl=500ms", "revocation-token-ttl must be a whole number of seconds greater than 0");
        rejects("revocation-claim-lease=30s", "revocation-claim-lease must be a whole number of seconds greater than 30");
        rejects("revocation-batch-size=0", "revocation-batch-size must be positive");
    }

    @Test
    void binding_acceptsIssuerTimeoutOverrides() {
        context.withPropertyValues("grab.seller-oidc.connect-timeout=750ms",
                "grab.seller-oidc.read-timeout=4s").run(application -> {
            SellerOidcProperties properties = application.getBean(SellerOidcProperties.class);
            assertThat(properties.connectTimeout()).isEqualTo(Duration.ofMillis(750));
            assertThat(properties.readTimeout()).isEqualTo(Duration.ofSeconds(4));
        });
    }

    @Test
    void productionConfigurationRejectsMissingDeploymentIssuer() {
        context.run(application -> {
            SellerProductionConfigurationValidator validator = new SellerProductionConfigurationValidator(
                    application.getBean(SellerOidcProperties.class),
                    application.getBean(CorsProperties.class), new RedisProperties());
            validator.setEnvironment(new MockEnvironment());

            assertThatThrownBy(validator::validate)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("SELLER_OIDC_ISSUER is required")
                    .hasMessageNotContaining("test-secret");
        });
    }

    @Test
    void corsPreflight_usesBoundOverridesAndRequiresExplicitOrigin() {
        context.withPropertyValues("security.api.cors=https://seller.example,*",
                "security.api.cors-allowed-methods[0]=PATCH",
                "security.api.cors-allowed-headers[0]=X-Seller-Trace").run(application -> {
            CorsProperties properties = application.getBean(CorsProperties.class);
            UrlBasedCorsConfigurationSource source = SellerOidcSecurityConfig.sellerCors(properties);
            CorsFilter filter = new CorsFilter(source);

            MockHttpServletResponse allowed = preflight(filter, "https://seller.example", "PATCH", "X-Seller-Trace");
            assertThat(allowed.getStatus()).isEqualTo(200);
            assertThat(allowed.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)).isEqualTo("https://seller.example");
            assertThat(allowed.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS)).isEqualTo("PATCH");
            assertThat(allowed.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS)).isEqualTo("X-Seller-Trace");
            assertThat(allowed.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS)).isEqualTo("true");

            MockHttpServletResponse badMethod = preflight(filter, "https://seller.example", "POST", "X-Seller-Trace");
            MockHttpServletResponse badHeader = preflight(filter, "https://seller.example", "PATCH", "Authorization");
            MockHttpServletResponse badOrigin = preflight(filter, "https://other.example", "PATCH", "X-Seller-Trace");
            for (MockHttpServletResponse rejected : new MockHttpServletResponse[]{badMethod, badHeader, badOrigin}) {
                assertThat(rejected.getStatus()).isEqualTo(403);
                assertThat(rejected.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)).isNull();
            }
        });
    }

    private static MockHttpServletResponse preflight(CorsFilter filter, String origin, String method, String header)
            throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/v1");
        request.addHeader(HttpHeaders.ORIGIN, origin);
        request.addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, method);
        request.addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, header);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> {});
        return response;
    }

    private void rejects(String property, String failure) {
        context.withPropertyValues("grab.seller-oidc." + property).run(application -> {
            assertThat(application).hasFailed();
            assertThat(application.getStartupFailure()).hasStackTraceContaining(failure);
        });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties({SellerOidcProperties.class, CorsProperties.class})
    static class Binding {}
}
