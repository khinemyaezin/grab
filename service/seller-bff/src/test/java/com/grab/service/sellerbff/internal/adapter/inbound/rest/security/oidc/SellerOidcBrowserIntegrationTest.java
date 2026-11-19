package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import com.catalog.application.security.CatalogAuthorityManifest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;
import com.grab.service.contract.message.account.scim.GrabOidcClaims;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.principal.SellerSecurityPrincipal;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.service.SellerC2CApplicationQueryService;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.service.SellerCategoryCommandService;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.service.SellerMerchantCommandService;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.service.SellerMerchantQueryService;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.service.SellerSalesChannelQueryService;
import com.grab.service.sellerbff.port.outbound.SellerAuthOutboundPort;
import com.identity.application.security.IdentityAuthorityManifest;
import com.merchant.application.security.MerchantAdminAccessProfile;
import com.merchant.application.security.MerchantAuthorityManifest;
import com.saleschannel.application.security.SalesChannelAuthorityManifest;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
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
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "grab.seller-oidc.issuer=http://localhost:9000",
        "grab.seller-oidc.audience=seller-bff",
        "grab.seller-oidc.client-id=seller",
        "grab.seller-oidc.authorization-endpoint=http://localhost:9000/oauth2/authorize",
        "grab.seller-oidc.token-endpoint=http://issuer:9000/oauth2/token",
        "grab.seller-oidc.jwk-set-endpoint=http://issuer:9000/oauth2/jwks",
        "grab.seller-oidc.introspection-endpoint=http://issuer:9000/oauth2/introspect",
        "grab.seller-oidc.revocation-endpoint=http://issuer:9000/oauth2/revoke",
        "grab.seller-oidc.logout-endpoint=http://issuer:9000/connect/logout",
        "grab.seller-oidc.cookie-name=SELLER_TEST_SESSION",
        "grab.seller-oidc.redis-namespace=grab:seller-test",
        "grab.seller-oidc.session-timeout=45m",
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

    @BeforeEach
    void cleanRedis() {
        redis.getConnectionFactory().getConnection().serverCommands().flushDb();
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

        // GET /register-url returns the authorization server registration URL
        HttpResponse<String> registerUrl = send(browser, "/api/v1/identity/auth/register-url", "GET", null);
        assertThat(registerUrl.statusCode()).isEqualTo(200);
        assertThat(registerUrl.body()).contains("http://localhost:9000/register");

        // POST /register redirects to authorization server registration
        HttpResponse<String> registered = send(browser, "/api/v1/identity/auth/register", "POST",
                null, token.path("token").asText());
        assertThat(registered.statusCode()).isIn(302, 303);
        assertThat(registered.headers().firstValue("Location")).hasValue("http://localhost:9000/register");

        // Request with prompt=select_context forwards prompt query parameter
        HttpResponse<String> contextLogin = send(browser, "/oauth2/authorization/seller?prompt=select_context", "GET", null);
        assertThat(contextLogin.statusCode()).isEqualTo(302);
        String contextRedirect = contextLogin.headers().firstValue("Location").orElseThrow();
        assertThat(contextRedirect).contains("prompt=select_context");
    }

    @Test
    void protectedRoute_requiresContextAndRejectsAnotherMerchant() throws Exception {
        AuthenticatedActor unscopedActor = actor(null, Set.of(), Set.of(MerchantAuthorityManifest.PROFILE_WRITE));
        Session session = authenticatedSession(sessions, unscopedActor);
        assertThat(session.getMaxInactiveInterval()).isEqualTo(Duration.ofMinutes(45));
        String redisKey = "grab:seller-test:sessions:sessions:" + session.getId();
        assertThat(redis.hasKey(redisKey)).isTrue();
        HttpClient browser = browser(session);
        String csrf = csrf(browser);

        String profile = "/api/v1/merchants/other/profile";
        HttpResponse<String> missingContext = send(browser, profile, "PATCH", "{}", csrf);
        assertProblem(missingContext, 409, "idt.service.auth.access_context_required", "Select an access context to continue");

        AccessContext merchant = new AccessContext("assignment", "merchant.account", "own");
        AuthenticatedActor scopedActor = actor(merchant, Set.of(), Set.of(MerchantAuthorityManifest.PROFILE_WRITE));
        Session scopedSession = authenticatedSession(sessions, scopedActor);
        HttpClient scopedBrowser = browser(scopedSession);
        String scopedCsrf = csrf(scopedBrowser);
        HttpResponse<String> wrongTenant = send(scopedBrowser, profile, "PATCH", "{}", scopedCsrf);
        assertProblem(wrongTenant, 403, "idt.service.auth.access_denied", "Access denied");
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class RouteProbeConfiguration {
        @Bean
        @Primary
        ClientRegistrationRepository sellerTestClientRegistration(SellerOidcProperties properties) {
            ClientRegistration registration = ClientRegistration.withRegistrationId("seller")
                    .clientId(properties.clientId())
                    .clientSecret(properties.clientSecret())
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri(properties.callback())
                    .scope("openid", "profile", "email")
                    .authorizationUri("http://localhost:9000/oauth2/authorize")
                    .tokenUri("http://issuer:9000/oauth2/token")
                    .jwkSetUri("http://issuer:9000/oauth2/jwks")
                    .issuerUri(properties.issuer())
                    .clientName("Seller Central")
                    .build();
            return new InMemoryClientRegistrationRepository(registration);
        }

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
        AccessContext defaultContext = new AccessContext("assignment", "merchant.account", "own");
        AuthenticatedActor defaultActor = actor(defaultContext, Set.of(), Set.of());
        Session baseSession = authenticatedSession(sessions, defaultActor);
        HttpClient anonymous = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
        String anonymousCsrf = csrf(anonymous);
        Set<String> tested = new HashSet<>();
        ClassPathResource cases = new ClassPathResource("seller-http-permissions.txt");
        for (String line : cases.getContentAsString(StandardCharsets.UTF_8).lines().skip(1).toList()) {
            String[] fields = line.split(" ");
            String pattern = fields[1];
            String path = pattern.replaceAll("\\{[^}]+}", "own") + "?securityProbe";
            String access = fields[2];
            boolean isPublic = access.equals("public");
            boolean needsAuthority = !isPublic && !access.equals("authenticated");
            AccessContext context = fields[3].equals("none") ? null : new AccessContext("assignment", fields[3], "own");
            AccessContext liveContext = context == null
                    ? new AccessContext("assignment", "merchant.account", "own") : context;
            for (String method : fields[0].split(",")) {
                String route = method + " " + pattern;
                assertThat(tested.add(route)).as("unique case for %s", route).isTrue();
                assertThat(send(anonymous, path, method, null, anonymousCsrf).statusCode())
                        .as("anonymous %s", route).isEqualTo(isPublic ? 204 : 401);
                if (needsAuthority) {
                    Session noAuthSession = authenticatedSession(sessions, actor(liveContext, Set.of(), Set.of()));
                    HttpClient noAuthClient = browser(noAuthSession);
                    String noAuthCsrf = csrf(noAuthClient);
                    assertThat(send(noAuthClient, path, method, null, noAuthCsrf).statusCode())
                            .as("missing authority for %s", route).isEqualTo(403);
                }
                Set<String> granted = needsAuthority ? Set.of(access) : Set.of();
                Session authorizedSession = authenticatedSession(sessions, actor(liveContext, Set.of(), granted));
                HttpClient authorizedClient = browser(authorizedSession);
                String authorizedCsrf = csrf(authorizedClient);
                assertThat(send(authorizedClient, path, method, null, authorizedCsrf).statusCode())
                        .as("permitted %s", route).isEqualTo(204);
            }
        }
        Map<RequestMappingInfo, HandlerMethod> handlers = controllerMappings.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlers.entrySet()) {
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

        AuthenticatedActor unscopedActor = actor(null, Set.of(), Set.of());
        Session session = authenticatedSession(sessions, unscopedActor);
        HttpClient browser = browser(session);
        assertThat(send(browser, catalog, "HEAD", null).statusCode()).isEqualTo(200);
        assertThat(send(browser, "/api/v1/merchants/root", "GET", null).statusCode()).isEqualTo(200);
        String application = "/api/v1/merchants/applications/c2c";
        assertThat(send(browser, application, "GET", null).statusCode()).isEqualTo(404);
        verify(c2cApplications).getC2CApplication("seller");
        assertThat(send(browser, "/api/v1/unconfigured", "GET", null).statusCode()).isEqualTo(403);

        String csrf = csrf(browser);
        // Switch context triggers 302/303 redirect to /oauth2/authorization/seller?prompt=select_context
        String switchContext = "/api/v1/identity/session/contexts/switch";
        HttpResponse<String> switchResponse = send(browser, switchContext, "POST", null, csrf);
        assertThat(switchResponse.statusCode()).isIn(302, 303);
        assertThat(switchResponse.headers().firstValue("Location"))
                .hasValueSatisfying(loc -> assertThat(loc).contains("prompt=select_context"));

        // Select context also redirects to context switch
        String selection = "/api/v1/identity/session/contexts/assignment/select";
        HttpResponse<String> selectResponse = send(browser, selection, "POST", null, csrf);
        assertThat(selectResponse.statusCode()).isIn(302, 303);
        assertThat(selectResponse.headers().firstValue("Location"))
                .hasValueSatisfying(loc -> assertThat(loc).contains("prompt=select_context"));
    }

    @Test
    void merchantMembershipAndStorefronts_requireAuthorityAndMatchingContext() throws Exception {
        AccessContext merchant = new AccessContext("assignment", "merchant.account", "own");
        AccessContext storefront = new AccessContext("assignment", "merchant.storefront", "store");

        AuthenticatedActor member = actor(merchant, Set.of(), Set.of(IdentityAuthorityManifest.ACCESS_ASSIGNMENT_READ));
        AuthenticatedActor admin = actor(merchant, Set.of(MerchantAdminAccessProfile.ADMIN_ROLE_CODE),
                Set.of(MerchantAuthorityManifest.STOREFRONT_WRITE, IdentityAuthorityManifest.ACCESS_ASSIGNMENT_WRITE));
        AuthenticatedActor storeReader = actor(storefront, Set.of(), Set.of(MerchantAuthorityManifest.STOREFRONT_READ));

        HttpClient memberBrowser = browser(authenticatedSession(sessions, member));
        assertThat(send(memberBrowser, "/api/v1/merchants/own/members", "GET", null).statusCode()).isEqualTo(200);

        HttpClient unauthBrowser = browser(authenticatedSession(sessions, actor(merchant, Set.of(), Set.of())));
        assertThat(send(unauthBrowser, "/api/v1/merchants/own/members", "GET", null).statusCode()).isEqualTo(403);
        assertThat(send(memberBrowser, "/api/v1/merchants/other/members", "GET", null).statusCode()).isEqualTo(403);

        HttpClient adminBrowser = browser(authenticatedSession(sessions, admin));
        String adminCsrf = csrf(adminBrowser);
        String ownMember = "/api/v1/merchants/own/members/member";
        assertThat(send(adminBrowser, ownMember, "DELETE", null, adminCsrf).statusCode()).isEqualTo(204);
        verify(merchantCommands).removeMember("own", "member", "seller");

        HttpClient storeBrowser = browser(authenticatedSession(sessions, storeReader));
        assertThat(send(storeBrowser, "/api/v1/merchants/own/storefronts/store", "GET", null).statusCode()).isEqualTo(404);
        assertThat(send(storeBrowser, "/api/v1/merchants/own/storefronts/other", "GET", null).statusCode()).isEqualTo(403);
        verify(merchants).getStorefront("own", "store");
        verify(merchants, never()).getStorefront("own", "other");
    }

    @Test
    void moduleRules_distinguishPlatformWritesAndPublishableKeyAuthorities() throws Exception {
        AccessContext merchant = new AccessContext("assignment", "merchant.account", "own");
        AccessContext platform = new AccessContext("assignment", "platform", "global");

        AuthenticatedActor merchantWriter = actor(merchant, Set.of(), Set.of(CatalogAuthorityManifest.WRITE));
        AuthenticatedActor platformWriter = actor(platform, Set.of(), Set.of(CatalogAuthorityManifest.WRITE));
        AuthenticatedActor channelReader = actor(merchant, Set.of(), Set.of(SalesChannelAuthorityManifest.READ));
        AuthenticatedActor keyReader = actor(merchant, Set.of(), Set.of(SalesChannelAuthorityManifest.KEY_READ));

        String category = "/api/v1/categories/category";
        HttpClient merchantClient = browser(authenticatedSession(sessions, merchantWriter));
        String merchantCsrf = csrf(merchantClient);
        assertThat(send(merchantClient, category, "DELETE", null, merchantCsrf).statusCode()).isEqualTo(403);

        HttpClient platformClient = browser(authenticatedSession(sessions, platformWriter));
        String platformCsrf = csrf(platformClient);
        assertThat(send(platformClient, category, "DELETE", null, platformCsrf).statusCode()).isEqualTo(204);
        verify(categories).deleteCategory("category");

        String keyResolution = "/api/v1/sales-channels/publishable-keys/resolve?token=test";
        HttpClient channelClient = browser(authenticatedSession(sessions, channelReader));
        assertThat(send(channelClient, "/api/v1/sales-channels/root", "GET", null).statusCode()).isEqualTo(200);
        assertThat(send(channelClient, keyResolution, "GET", null).statusCode()).isEqualTo(403);

        HttpClient keyClient = browser(authenticatedSession(sessions, keyReader));
        assertThat(send(keyClient, keyResolution, "GET", null).statusCode()).isEqualTo(404);
        verify(salesChannels).resolveKey("test");
    }

    @Test
    void logout_clearsSession() throws Exception {
        Session session = authenticatedSession(sessions);
        String sessionId = session.getId();
        HttpClient browser = browser(session);
        String csrf = csrf(browser);

        HttpResponse<String> logout = send(browser, "/api/v1/identity/auth/logout", "POST", null, csrf);
        assertThat(logout.statusCode()).isIn(200, 204, 302, 303);
        assertThat(sessions.findById(sessionId)).isNull();
    }

    private <S extends Session> Session authenticatedSession(SessionRepository<S> repository) {
        return authenticatedSession(repository, actor(null, Set.of(), Set.of()));
    }

    private <S extends Session> Session authenticatedSession(SessionRepository<S> repository, AuthenticatedActor actor) {
        Instant issued = Instant.now();
        Instant expires = issued.plusSeconds(600);
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", actor.subject());
        claims.put(GrabOidcClaims.PLATFORM_USER_ID, actor.platformUserId());
        claims.put(GrabOidcClaims.EMAIL, actor.email());
        if (actor.accessContext() != null) {
            claims.put(GrabOidcClaims.ASSIGNMENT_ID, actor.accessContext().assignmentId());
            claims.put(GrabOidcClaims.SCOPE_KEY, actor.accessContext().scopeKey());
            claims.put(GrabOidcClaims.SCOPE_ID, actor.accessContext().scopeId());
        }
        claims.put(GrabOidcClaims.ROLE, actor.roles().isEmpty() ? null : actor.roles().iterator().next());
        claims.put(GrabOidcClaims.AUTHORITIES, List.copyOf(actor.authorities()));

        OidcIdToken token = new OidcIdToken("id", issued, expires, claims);
        SellerSecurityPrincipal principal = new SellerSecurityPrincipal(actor, new DefaultOidcUser(List.of(), token));
        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(principal, principal.getAuthorities(), "seller");
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
