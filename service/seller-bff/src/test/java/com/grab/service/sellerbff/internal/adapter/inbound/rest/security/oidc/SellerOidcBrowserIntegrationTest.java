package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import com.catalog.application.security.CatalogAuthorityManifest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerIssuerClient.Tokens;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.service.SellerC2CApplicationQueryService;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.service.SellerCategoryCommandService;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.service.SellerMerchantCommandService;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.service.SellerMerchantQueryService;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.service.SellerSalesChannelQueryService;
import com.grab.service.sellerbff.port.outbound.SellerAuthOutboundPort.SellerUserRegistrationResult;
import com.grab.service.sellerbff.port.outbound.SellerAuthOutboundPort;
import com.identity.application.security.IdentityAuthorityManifest;
import com.merchant.application.security.MerchantAdminAccessProfile;
import com.merchant.application.security.MerchantAuthorityManifest;
import com.saleschannel.application.security.SalesChannelAuthorityManifest;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "grab.seller-oidc.issuer=http://localhost:9000",
        "grab.seller-oidc.backchannel=http://issuer:9000",
        "grab.seller-oidc.cookie-name=SELLER_TEST_SESSION",
        "grab.seller-oidc.redis-namespace=grab:seller-test",
        "grab.seller-oidc.session-timeout=45m",
        "grab.seller-oidc.revocation-retry-delay=PT1H",
        "grab.seller-oidc.revocation-token-ttl=10m",
        "grab.seller-oidc.revocation-claim-lease=90s",
        "grab.seller-oidc.revocation-batch-size=1",
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

    @MockitoBean
    SellerIssuerClient issuer;

    @MockitoBean
    SellerMerchantQueryService merchants;

    @MockitoBean
    SellerC2CApplicationQueryService c2cApplications;

    @MockitoBean
    SellerCategoryCommandService categories;

    @MockitoBean
    SellerSalesChannelQueryService salesChannels;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    RequestMappingHandlerMapping controllerMappings;

    @MockitoBean
    SellerMerchantCommandService merchantCommands;

    @Autowired
    SellerOidcProperties properties;

    @Autowired
    SessionRepository<?> sessions;

    @Autowired
    SellerOidcTokenSlots slots;

    @Autowired
    SellerPendingRevocations pending;

    @Autowired
    ClientRegistrationRepository clients;

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
    void corsPreflight_usesDefaultMethodsAndHeaders() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/identity/auth/register"))
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type, X-CSRF-TOKEN")
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build();
        HttpClient browser = HttpClient.newHttpClient();
        HttpResponse<String> response = browser.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
                .hasValue("http://localhost:3000");
        assertThat(response.headers().firstValue(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
                .hasValue("GET,HEAD,POST,PUT,PATCH,DELETE,OPTIONS");
        assertThat(response.headers().firstValue(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS))
                .hasValue("Content-Type, X-CSRF-TOKEN");
    }

    @Test
    void startsOidcWithPkceAndProtectsRegistrationWithCsrf() throws Exception {
        CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        HttpClient browser = HttpClient.newBuilder().cookieHandler(cookies).build();
        assertThat(send(browser, "/api/v1", "GET", null).statusCode()).isEqualTo(200);
        HttpResponse<String> identity = send(browser, "/api/v1/identity", "GET", null);
        assertThat(identity.statusCode()).isEqualTo(200);
        String publicLogin = "http://localhost:8080/oauth2/authorization/seller";
        JsonNode discovery = json.readTree(identity.body());
        assertThat(discovery.at("/_links/login/href").asText()).isEqualTo(publicLogin);
        HttpResponse<String> login = send(browser, "/oauth2/authorization/seller", "GET", null);
        assertThat(login.statusCode()).isEqualTo(302);
        String redirect = login.headers().firstValue("Location").orElseThrow();
        assertThat(redirect).startsWith("http://localhost:9000/oauth2/authorize?")
                .contains("response_type=code", "code_challenge_method=S256", "nonce=", "state=",
                        "redirect_uri=http://localhost:8080/login/oauth2/code/seller");
        ClientRegistration registration = clients.findByRegistrationId("seller");
        assertThat(registration.getProviderDetails().getAuthorizationUri())
                .isEqualTo("http://localhost:9000/oauth2/authorize");
        assertThat(registration.getProviderDetails().getTokenUri()).isEqualTo("http://issuer:9000/oauth2/token");
        assertThat(registration.getProviderDetails().getJwkSetUri()).isEqualTo("http://issuer:9000/oauth2/jwks");

        HttpResponse<String> csrf = send(browser, "/api/v1/identity/auth/csrf", "GET", null);
        assertThat(csrf.statusCode()).withFailMessage(csrf.body()).isEqualTo(200);
        JsonNode token = json.readTree(csrf.body());
        assertThat(token.path("headerName").asText()).isEqualTo("X-CSRF-TOKEN");
        assertThat(token.path("token").asText()).isNotBlank();
        assertThat(cookies.getCookieStore().getCookies())
                .anySatisfy(cookie -> assertThat(cookie.getName()).isEqualTo("SELLER_TEST_SESSION"));

        HttpResponse<String> missingCsrf = send(browser, "/api/v1/identity/auth/register", "POST", "{}");
        assertThat(missingCsrf.statusCode()).isEqualTo(403);

        when(registrations.registerUser("new@example.com", "safe-password"))
                .thenReturn(new SellerUserRegistrationResult("user-1", "new@example.com", "ACTIVE", "now"));
        HttpResponse<String> registered = send(browser, "/api/v1/identity/auth/register", "POST",
                "{\"email\":\"new@example.com\",\"password\":\"safe-password\"}",
                token.path("token").asText());
        assertThat(registered.statusCode()).isEqualTo(201);
        assertThat(registered.body()).contains("new@example.com").doesNotContain("safe-password");
        JsonNode registrationBody = json.readTree(registered.body());
        assertThat(registrationBody.at("/_links/login/href").asText()).isEqualTo(publicLogin);

        HttpResponse<String> anonymousLogout = send(browser, "/api/v1/identity/auth/logout", "POST",
                null, token.path("token").asText());
        assertThat(anonymousLogout.statusCode()).isEqualTo(401);

        HttpRequest bearerRequest = HttpRequest.newBuilder(URI.create("http://localhost:" + port
                        + "/api/v1/identity/session"))
                .header("Authorization", "Bearer not-a-valid-token").GET().build();
        HttpResponse<String> bearerResponse = HttpClient.newHttpClient().send(bearerRequest,
                HttpResponse.BodyHandlers.ofString());
        assertThat(bearerResponse.statusCode()).isNotEqualTo(200);
    }

    @Test
    void tokenSlotIsSharedAndSerializedAcrossInstances() {
        SellerOidcTokenSlots first = new SellerOidcTokenSlots(redis, json, properties);
        SellerOidcTokenSlots second = new SellerOidcTokenSlots(redis, json, properties);
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
    void protectedRoute_requiresContextAndRejectsAnotherMerchant() throws Exception {
        Session session = authenticatedSession(sessions);
        Tokens tokens = new Tokens("actor-token", "actor-refresh", Instant.now().plusSeconds(60), "seller", "id");
        slots.start(session.getId(), tokens);
        assertThat(session.getMaxInactiveInterval()).isEqualTo(Duration.ofMinutes(45));
        String redisKey = "grab:seller-test:sessions:sessions:" + session.getId();
        assertThat(redis.hasKey(redisKey)).isTrue();
        HttpClient browser = browser(session);
        String csrf = csrf(browser);
        AuthenticatedActor unscoped = actor(null, Set.of(), Set.of());
        AccessContext merchant = new AccessContext("assignment", "merchant.account", "own");
        when(issuer.actor(tokens)).thenReturn(unscoped, actor(merchant));

        String profile = "/api/v1/merchants/other/profile";
        HttpResponse<String> missingContext = send(browser, profile, "PATCH", "{}", csrf);
        assertProblem(missingContext, 409, "idt.service.auth.access_context_required", "Select an access context to continue");
        HttpResponse<String> wrongTenant = send(browser, profile, "PATCH", "{}", csrf);
        assertProblem(wrongTenant, 403, "idt.service.auth.access_denied", "Access denied");
    }

    // End probe requests after the security chain, before controller side effects.
    @TestConfiguration(proxyBeanMethods = false)
    static class RouteProbeConfiguration {
        @Bean
        FilterRegistrationBean<Filter> routeProbe() {
            FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
            registration.setFilter((request, response, chain) -> {
                if ("securityProbe".equals(((HttpServletRequest) request).getQueryString())) {
                    ((HttpServletResponse) response).setStatus(204);
                    return;
                }
                chain.doFilter(request, response);
            });
            return registration;
        }
    }

    @Test
    void controllerMappings_haveTestedHttpPermissions() throws Exception {
        Session session = authenticatedSession(sessions);
        Tokens tokens = startTokens(session);
        HttpClient seller = browser(session);
        HttpClient anonymous = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
        String sellerCsrf = csrf(seller);
        String anonymousCsrf = csrf(anonymous);
        Set<String> tested = new HashSet<>();
        var cases = new ClassPathResource("seller-http-permissions.txt");
        for (String line : cases.getContentAsString(StandardCharsets.UTF_8).lines().skip(1).toList()) {
            String[] fields = line.split(" ");
            String pattern = fields[1];
            String path = pattern.replaceAll("\\{[^}]+}", "own") + "?securityProbe";
            String access = fields[2];
            boolean isPublic = access.equals("public");
            boolean needsAuthority = !isPublic && !access.equals("authenticated");
            AccessContext context = fields[3].equals("none") ? null : new AccessContext("assignment", fields[3], "own");
            for (String method : fields[0].split(",")) {
                String route = method + " " + pattern;
                assertThat(tested.add(route)).as("unique case for %s", route).isTrue();
                assertThat(send(anonymous, path, method, null, anonymousCsrf).statusCode())
                        .as("anonymous %s", route).isEqualTo(isPublic ? 204 : 401);
                if (needsAuthority) {
                    when(issuer.actor(tokens)).thenReturn(actor(null, Set.of(), Set.of(access)));
                    assertThat(send(seller, path, method, null, sellerCsrf).statusCode())
                            .as("missing context for %s", route).isEqualTo(409);
                    when(issuer.actor(tokens)).thenReturn(actor(context, Set.of(), Set.of()));
                    assertThat(send(seller, path, method, null, sellerCsrf).statusCode())
                            .as("missing authority for %s", route).isEqualTo(403);
                }
                Set<String> granted = needsAuthority ? Set.of(access) : Set.of();
                when(issuer.actor(tokens)).thenReturn(actor(context, Set.of(), granted));
                assertThat(send(seller, path, method, null, sellerCsrf).statusCode())
                        .as("permitted %s", route).isEqualTo(204);
            }
        }
        Map<RequestMappingInfo, HandlerMethod> handlers = controllerMappings.getHandlerMethods();
        for (var entry : handlers.entrySet()) {
            if (entry.getValue().getBeanType().getPackageName().startsWith("com.grab.service.sellerbff.")) {
                RequestMappingInfo mapping = entry.getKey();
                assertThat(mapping.getMethodsCondition().getMethods()).as("explicit methods for %s", mapping).isNotEmpty();
                for (RequestMethod method : mapping.getMethodsCondition().getMethods()) {
                    for (String path : mapping.getPatternValues()) {
                        assertThat(tested).contains(method.name() + " " + path);
                    }
                }
            }
        }
    }

    @Test
    void discoveryAndContextSelection_allowLiveUnscopedSeller() throws Exception {
        String catalog = "/api/v1/catalog";
        HttpResponse<String> anonymous = send(HttpClient.newHttpClient(), catalog, "GET", null);
        assertThat(anonymous.statusCode()).isEqualTo(401);

        Session session = authenticatedSession(sessions);
        Tokens tokens = startTokens(session);
        when(issuer.actor(tokens)).thenReturn(actor(null, Set.of(), Set.of()));
        HttpClient browser = browser(session);
        assertThat(send(browser, catalog, "HEAD", null).statusCode()).isEqualTo(200);
        assertThat(send(browser, "/api/v1/merchants/root", "GET", null).statusCode()).isEqualTo(200);
        String application = "/api/v1/merchants/applications/c2c";
        assertThat(send(browser, application, "GET", null).statusCode()).isEqualTo(404);
        verify(c2cApplications).getC2CApplication("seller");
        assertThat(send(browser, "/api/v1/unconfigured", "GET", null).statusCode()).isEqualTo(403);

        String csrf = csrf(browser);
        Tokens selected = new Tokens("selected", "refresh", Instant.now().plusSeconds(60), "seller", "id");
        AccessContext merchant = new AccessContext("assignment", "merchant.account", "own");
        when(issuer.select(tokens, "assignment")).thenReturn(selected);
        when(issuer.actor(selected)).thenReturn(actor(merchant));
        assertThat(send(browser, "/api/v1/catalog", "PUT", null, csrf).statusCode()).isEqualTo(403);
        String selection = "/api/v1/identity/session/contexts/assignment/select";
        HttpResponse<String> response = send(browser, selection, "POST", null, csrf);
        assertThat(response.statusCode()).withFailMessage(response.body()).isEqualTo(200);
        assertThat(response.body()).contains("assignment", "merchant.account");
    }

    @Test
    void merchantMembershipAndStorefronts_requireAuthorityAndMatchingContext() throws Exception {
        Session session = authenticatedSession(sessions);
        Tokens tokens = startTokens(session);
        HttpClient browser = browser(session);
        AccessContext merchant = new AccessContext("assignment", "merchant.account", "own");
        AccessContext storefront = new AccessContext("assignment", "merchant.storefront", "store");
        AuthenticatedActor member = actor(merchant, Set.of(), Set.of(IdentityAuthorityManifest.ACCESS_ASSIGNMENT_READ));
        AuthenticatedActor admin = actor(merchant, Set.of(MerchantAdminAccessProfile.ADMIN_ROLE_CODE), Set.of());
        AuthenticatedActor storeReader = actor(storefront, Set.of(), Set.of(MerchantAuthorityManifest.STOREFRONT_READ));
        AuthenticatedActor writer = actor(merchant, Set.of(), Set.of(IdentityAuthorityManifest.ACCESS_ASSIGNMENT_WRITE));
        when(issuer.actor(tokens)).thenReturn(member, admin, member, member, writer, writer, storeReader, storeReader, storeReader);

        assertThat(send(browser, "/api/v1/merchants/own/members", "GET", null).statusCode()).isEqualTo(200);
        assertThat(send(browser, "/api/v1/merchants/own/members", "GET", null).statusCode()).isEqualTo(403);
        assertThat(send(browser, "/api/v1/merchants/other/members", "GET", null).statusCode()).isEqualTo(403);
        String csrf = csrf(browser);
        String ownMember = "/api/v1/merchants/own/members/member";
        assertThat(send(browser, ownMember, "DELETE", null, csrf).statusCode()).isEqualTo(403);
        assertThat(send(browser, "/api/v1/merchants/other/members/member", "DELETE", null, csrf).statusCode()).isEqualTo(403);
        assertThat(send(browser, ownMember, "DELETE", null, csrf).statusCode()).isEqualTo(204);
        verify(merchantCommands).removeMember("own", "member", "seller");
        assertThat(send(browser, "/api/v1/merchants/own/storefronts/store", "GET", null).statusCode()).isEqualTo(404);
        assertThat(send(browser, "/api/v1/merchants/own/storefronts/other", "GET", null).statusCode()).isEqualTo(403);
        assertThat(send(browser, "/api/v1/merchants/own/storefronts", "GET", null).statusCode()).isEqualTo(403);
        verify(merchants).getStorefront("own", "store");
        verify(merchants, never()).getStorefront("own", "other");
    }

    @Test
    void moduleRules_distinguishPlatformWritesAndPublishableKeyAuthorities() throws Exception {
        Session session = authenticatedSession(sessions);
        Tokens tokens = startTokens(session);
        HttpClient browser = browser(session);
        String csrf = csrf(browser);
        AccessContext merchant = new AccessContext("assignment", "merchant.account", "own");
        AccessContext platform = new AccessContext("assignment", "platform", "global");
        AuthenticatedActor merchantWriter = actor(merchant, Set.of(), Set.of(CatalogAuthorityManifest.WRITE));
        AuthenticatedActor platformWriter = actor(platform, Set.of(), Set.of(CatalogAuthorityManifest.WRITE));
        AuthenticatedActor channelReader = actor(merchant, Set.of(), Set.of(SalesChannelAuthorityManifest.READ));
        AuthenticatedActor keyReader = actor(merchant, Set.of(), Set.of(SalesChannelAuthorityManifest.KEY_READ));
        when(issuer.actor(tokens)).thenReturn(merchantWriter, platformWriter, channelReader, channelReader, keyReader, keyReader);

        String category = "/api/v1/categories/category";
        assertThat(send(browser, category, "DELETE", null, csrf).statusCode()).isEqualTo(403);
        assertThat(send(browser, category, "DELETE", null, csrf).statusCode()).isEqualTo(204);
        verify(categories).deleteCategory("category");

        String keyResolution = "/api/v1/sales-channels/publishable-keys/resolve?token=test";
        assertThat(send(browser, "/api/v1/sales-channels/root", "GET", null).statusCode()).isEqualTo(200);
        assertThat(send(browser, keyResolution, "GET", null).statusCode()).isEqualTo(403);
        assertThat(send(browser, keyResolution, "GET", null).statusCode()).isEqualTo(404);
        assertThat(send(browser, "/api/v1/sales-channels/root", "GET", null).statusCode()).isEqualTo(403);
        verify(salesChannels).resolveKey("test");
    }

    @Test
    void logout_duringIssuerOutage_skipsLiveIdentityResolution() throws Exception {
        Session session = authenticatedSession(sessions);
        Tokens tokens = startTokens(session);
        when(issuer.actor(tokens)).thenThrow(SellerOidcFailure.unavailable());
        HttpClient browser = browser(session);
        String csrf = csrf(browser);
        assertThat(send(browser, "/api/v1/identity/session", "GET", null).statusCode()).isEqualTo(503);
        assertThat(send(browser, "/api/v1/identity/auth/logout", "POST", null, csrf).statusCode()).isEqualTo(303);
        assertThat(sessions.findById(session.getId())).isNull();
        verify(issuer).actor(tokens);
    }

    @Test
    void logout_duringRotation_revokesLatestTokenAfterTransientFailure() throws Exception {
        Session session = authenticatedSession(sessions);
        String sessionId = session.getId();
        Tokens expired = new Tokens("old-access", "old-refresh", Instant.now(), "seller", "id");
        Tokens rotated = new Tokens("new-access", "new-refresh", Instant.now().plusSeconds(60), "seller", "id");
        slots.start(sessionId, expired);
        CountDownLatch requested = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(issuer.refresh(expired)).thenAnswer(invocation -> {
            requested.countDown();
            assertThat(release.await(10, TimeUnit.SECONDS)).isTrue();
            return rotated;
        });
        doThrow(SellerOidcFailure.unavailable()).doNothing().when(issuer).revoke(rotated.refresh());
        HttpClient browser = browser(session);
        String csrf = csrf(browser);
        assertThat(send(browser, "/api/v1/identity/auth/logout", "POST", null).statusCode()).isEqualTo(403);

        HttpRequest request = request("/api/v1/identity/session", "GET", null, null);
        CompletableFuture<HttpResponse<String>> rotation = browser.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        String queue = "grab:seller-test:{revocations}:logout";
        try {
            assertThat(requested.await(10, TimeUnit.SECONDS)).isTrue();
            HttpResponse<String> logout = send(browser, "/api/v1/identity/auth/logout", "POST", null, csrf);
            assertThat(logout.statusCode()).isEqualTo(303);
            assertThat(logout.headers().firstValue("Location")).hasValueSatisfying(location ->
                    assertThat(location).startsWith("http://localhost:9000/connect/logout?")
                            .contains("id_token_hint=id"));
            assertThat(sessions.findById(sessionId)).isNull();

            pending.retry();
            assertThat(redis.opsForZSet().score(queue, sessionId)).isNotNull();
            verify(issuer, never()).revoke(expired.refresh());
        } finally {
            release.countDown();
        }
        HttpResponse<String> inFlight = rotation.get(10, TimeUnit.SECONDS);
        assertThat(inFlight.statusCode()).isEqualTo(401);
        verify(issuer, never()).actor(rotated);
        assertThat(slots.read(sessionId)).isEqualTo(rotated);

        redis.opsForZSet().add(queue, sessionId, 0);
        pending.retry();
        assertThat(slots.read(sessionId)).isEqualTo(rotated);
        assertThat(redis.opsForZSet().score(queue, sessionId)).isNotNull();

        redis.opsForZSet().add(queue, sessionId, 0);
        pending.retry();
        assertThat(slots.read(sessionId)).isNull();
        assertThat(redis.opsForZSet().score(queue, sessionId)).isNull();
        assertThat(sessions.findById(sessionId)).isNull();
        verify(issuer, never()).revoke(expired.refresh());
    }

    @Test
    void retry_revokesAnExistingTokenQueueEntry() {
        String queue = "grab:seller-test:{revocations}";
        String item = "legacy-session";
        String key = queue + ":" + item;
        redis.opsForValue().set(key, "legacy-refresh");
        redis.opsForZSet().add(queue, item, 0);

        pending.retry();

        verify(issuer).revoke("legacy-refresh");
        assertThat(redis.hasKey(key)).isFalse();
        assertThat(redis.opsForZSet().score(queue, item)).isNull();
    }

    @Test
    void revocationOverrides_applyRetentionAndClaimOneEntryPerQueue() {
        String tokenQueue = properties.redisNamespace() + ":{revocations}";
        String logoutQueue = tokenQueue + ":logout";
        String firstSession = UUID.randomUUID().toString();
        String secondSession = UUID.randomUUID().toString();
        Tokens logoutTokens = new Tokens("access", "logout-refresh", Instant.now(), "seller", "id");
        slots.start(firstSession, logoutTokens);
        slots.start(secondSession, logoutTokens);
        pending.enqueueLogout(firstSession);
        pending.enqueueLogout(secondSession);
        pending.enqueueToken("first-refresh");
        pending.enqueueToken("second-refresh");

        Set<String> items = redis.opsForZSet().range(tokenQueue, 0, -1);
        assertThat(items).hasSize(2);
        List<String> keys = items.stream().map(item -> tokenQueue + ":" + item).toList();
        try {
            for (String key : keys) {
                assertThat(redis.getExpire(key)).isBetween(590L, 600L);
            }
            doThrow(SellerOidcFailure.unavailable()).when(issuer).revoke(anyString());
            long before = Instant.now().getEpochSecond();
            pending.retry();
            long after = Instant.now().getEpochSecond();

            assertThat(redis.opsForZSet().count(tokenQueue, before + 90, after + 90)).isEqualTo(1L);
            assertThat(redis.opsForZSet().count(logoutQueue, before + 90, after + 90)).isEqualTo(1L);
            assertThat(redis.opsForZSet().count(tokenQueue, 0, after)).isEqualTo(1L);
            assertThat(redis.opsForZSet().count(logoutQueue, 0, after)).isEqualTo(1L);
        } finally {
            redis.delete(keys);
            redis.delete(List.of(tokenQueue, logoutQueue));
            for (String sessionId : List.of(firstSession, secondSession)) {
                slots.locked(sessionId, slot -> {
                    slot.write(null);
                    return null;
                });
                String marker = properties.redisNamespace() + ":tokens:{session:" + sessionId + "}:logout";
                redis.delete(marker);
            }
        }
    }

    private <S extends Session> Session authenticatedSession(SessionRepository<S> repository) {
        Instant issued = Instant.now();
        Instant expires = issued.plusSeconds(600);
        OidcIdToken token = new OidcIdToken("id", issued, expires, Map.of("sub", "seller"));
        DefaultOidcUser user = new DefaultOidcUser(List.of(), token);
        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(user, List.of(), "seller");
        SecurityContextImpl context = new SecurityContextImpl(authentication);
        S session = repository.createSession();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        repository.save(session);
        return session;
    }

    private HttpClient browser(Session session) {
        String id = session.getId();
        byte[] bytes = id.getBytes(StandardCharsets.UTF_8);
        String encoded = Base64.getEncoder().encodeToString(bytes);
        HttpCookie cookie = new HttpCookie(properties.cookieName(), encoded);
        cookie.setVersion(0);
        cookie.setPath("/");
        URI origin = URI.create("http://localhost:" + port);
        CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cookies.getCookieStore().add(origin, cookie);
        return HttpClient.newBuilder().cookieHandler(cookies).build();
    }

    private String csrf(HttpClient browser) throws Exception {
        HttpResponse<String> response = send(browser, "/api/v1/identity/auth/csrf", "GET", null);
        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode token = json.readTree(response.body());
        return token.path("token").asText();
    }

    private Tokens startTokens(Session session) {
        Instant expiry = Instant.now().plusSeconds(60);
        Tokens tokens = new Tokens("access", "refresh", expiry, "seller", "id");
        slots.start(session.getId(), tokens);
        return tokens;
    }

    private void assertProblem(HttpResponse<String> response, int status, String code, String detail) throws Exception {
        assertThat(response.statusCode()).isEqualTo(status);
        assertThat(response.headers().firstValue("Content-Type")).hasValueSatisfying(value ->
                assertThat(value).startsWith("application/problem+json"));
        JsonNode problem = json.readTree(response.body());
        assertThat(problem.path("status").asInt()).isEqualTo(status);
        assertThat(problem.path("code").asText()).isEqualTo(code);
        assertThat(problem.path("detail").asText()).isEqualTo(detail);
    }

    private AuthenticatedActor actor(AccessContext context) {
        return actor(context, Set.of(), Set.of(MerchantAuthorityManifest.PROFILE_WRITE));
    }

    private AuthenticatedActor actor(AccessContext context, Set<String> roles, Set<String> authorities) {
        return new AuthenticatedActor("seller", "http://issuer", "seller", "seller@example.com", roles, authorities, context);
    }

    private HttpResponse<String> send(HttpClient client, String path, String method, String body) throws Exception {
        return send(client, path, method, body, null);
    }

    private HttpResponse<String> send(HttpClient client, String path, String method, String body, String csrf) throws Exception {
        HttpRequest request = request(path, method, body, csrf);
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest request(String path, String method, String body, String csrf) {
        URI url = URI.create("http://localhost:" + port + path);
        HttpRequest.BodyPublisher payload = body == null
                ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body);
        HttpRequest.Builder builder = HttpRequest.newBuilder(url).header("Content-Type", "application/json");
        if (csrf != null) {
            builder.header("X-CSRF-TOKEN", csrf);
        }
        return builder.method(method, payload).build();
    }
}
