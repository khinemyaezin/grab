package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerIssuerClient.Tokens;
import com.grab.service.sellerbff.port.outbound.SellerAuthOutboundPort;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "grab.seller-oidc.enabled=true",
        "grab.seller-oidc.legacy-bearer=false",
        "grab.seller-oidc.issuer=http://localhost:9000",
        "grab.seller-oidc.client-secret=local-test-only-secret",
        "grab.seller-oidc.callback=http://localhost:8080/login/oauth2/code/seller",
        "grab.seller-oidc.ui=http://localhost:3000/",
        "grab.seller-oidc.post-logout=http://localhost:3000/",
        "grab.seller-oidc.secure-cookies=false",
        "security.api.cors=http://localhost:3000"
})
class SellerOidcBrowserIntegrationTest {
    private static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @MockitoBean
    SellerAuthOutboundPort registrations;

    @Autowired
    ObjectMapper json;

    @Autowired
    StringRedisTemplate redis;

    @LocalServerPort
    int port;

    @DynamicPropertySource
    static void redis(DynamicPropertyRegistry properties) {
        REDIS.start();
        properties.add("spring.data.redis.url", () -> "redis://" + REDIS.getHost() + ":" + REDIS.getMappedPort(6379));
    }

    @AfterAll
    static void stop() {
        REDIS.stop();
    }

    @Test
    void startsOidcWithPkceAndProtectsRegistrationWithCsrf() throws Exception {
        CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        HttpClient browser = HttpClient.newBuilder().cookieHandler(cookies).build();
        HttpResponse<String> login = send(browser, "/oauth2/authorization/seller", "GET", null);
        assertThat(login.statusCode()).isEqualTo(302);
        String redirect = login.headers().firstValue("Location").orElseThrow();
        assertThat(redirect).startsWith("http://localhost:9000/oauth2/authorize?")
                .contains("response_type=code", "code_challenge_method=S256", "nonce=", "state=");

        HttpResponse<String> csrf = send(browser, "/api/v1/identity/auth/csrf", "GET", null);
        assertThat(csrf.statusCode()).withFailMessage(csrf.body()).isEqualTo(200);
        JsonNode token = json.readTree(csrf.body());
        assertThat(token.path("headerName").asText()).isEqualTo("X-CSRF-TOKEN");
        assertThat(token.path("token").asText()).isNotBlank();
        assertThat(cookies.getCookieStore().getCookies())
                .anySatisfy(cookie -> assertThat(cookie.getName()).isEqualTo("GRAB_SELLER_SESSION"));

        HttpResponse<String> missingCsrf = send(browser, "/api/v1/identity/auth/register", "POST", "{}");
        assertThat(missingCsrf.statusCode()).isEqualTo(403);
    }

    @Test
    void tokenSlotIsSharedAndSerializedAcrossInstances() {
        SellerOidcTokenSlots first = new SellerOidcTokenSlots(redis, json);
        SellerOidcTokenSlots second = new SellerOidcTokenSlots(redis, json);
        Instant expiration = Instant.now().plusSeconds(60);
        Tokens old = new Tokens("a", "r", expiration, "seller", "id");
        Tokens updated = new Tokens("b", "s", expiration, "seller", "id");
        first.start("test-session", old);
        first.locked("test-session", slot -> {
            assertThat(second.read("test-session")).isEqualTo(old);
            assertThatThrownBy(() -> second.locked("test-session", locked -> null))
                    .isInstanceOfSatisfying(SellerOidcFailure.class,
                            failure -> assertThat(failure.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
            slot.write(updated);
            return null;
        });
        assertThat(second.read("test-session")).isEqualTo(updated);
    }

    @Test
    void routeGuardRequiresContextAndRejectsAnotherMerchant() {
        MockHttpServletRequest request = new MockHttpServletRequest("PATCH", "/api/v1/merchants/other/profile");
        request.setServletPath("/api/v1/merchants/other/profile");
        AuthenticatedActor unscoped = actor(null);
        assertThatThrownBy(() -> SellerOidcRouteAccess.require(request, unscoped))
                .isInstanceOfSatisfying(SellerOidcFailure.class,
                        failure -> assertThat(failure.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
        AccessContext context = new AccessContext("assignment", "merchant.account", "own");
        AuthenticatedActor scoped = actor(context);
        assertThatThrownBy(() -> SellerOidcRouteAccess.require(request, scoped))
                .isInstanceOfSatisfying(SellerOidcFailure.class,
                        failure -> assertThat(failure.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    private AuthenticatedActor actor(AccessContext context) {
        return new AuthenticatedActor("seller", "http://issuer", "seller", "seller@example.com",
                Set.of(), Set.of("MERCHANT_PROFILE_WRITE"), context);
    }

    private HttpResponse<String> send(HttpClient client, String path, String method, String body) throws Exception {
        URI url = URI.create("http://localhost:" + port + path);
        HttpRequest.BodyPublisher payload = body == null
                ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body);
        HttpRequest request = HttpRequest.newBuilder(url).header("Content-Type", "application/json")
                .method(method, payload).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
