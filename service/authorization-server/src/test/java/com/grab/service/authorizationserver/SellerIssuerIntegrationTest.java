package com.grab.service.authorizationserver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.AuthenticationState;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.VerifiedUser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SellerIssuerIntegrationTest {
    private static final String CALLBACK = "http://localhost:9999/login/oauth2/code/seller";
    private static final String CLIENT_SECRET = "local-integration-client-secret";
    private static final String VERIFIER = "long-enough-verifier-for-s256-with-no-padding";
    private static final String PASSWORD = "test-only-key-password";
    private static final Pattern CSRF = Pattern.compile("name=\"_csrf\"[^>]+value=\"([^\"]+)\"");
    private static final PostgreSQLContainer<?> DATABASE = new PostgreSQLContainer<>("postgres:16-alpine");
    private static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);
    private static Path keystore;

    @MockitoBean
    SellerAccountAuthentication account;

    @Autowired
    ObjectMapper json;

    @LocalServerPort
    int port;

    private HttpClient browser;

    @DynamicPropertySource
    static void settings(DynamicPropertyRegistry properties) throws Exception {
        DATABASE.start();
        REDIS.start();
        keystore = createKeyStore();
        String hash = "{bcrypt}" + new BCryptPasswordEncoder().encode(CLIENT_SECRET);
        properties.add("spring.datasource.url", DATABASE::getJdbcUrl);
        properties.add("spring.datasource.username", DATABASE::getUsername);
        properties.add("spring.datasource.password", DATABASE::getPassword);
        properties.add("spring.data.redis.url", () -> "redis://" + REDIS.getHost() + ":" + REDIS.getMappedPort(6379));
        properties.add("grab.seller-issuer.issuer", () -> "http://localhost:9000");
        properties.add("grab.seller-issuer.client-secret-hash", () -> hash);
        properties.add("grab.seller-issuer.redirect-uri", () -> CALLBACK);
        properties.add("grab.seller-issuer.post-logout-redirect-uri", () -> "http://localhost:9999/");
        properties.add("grab.seller-issuer.key-store", () -> keystore.toString());
        properties.add("grab.seller-issuer.key-store-password", () -> PASSWORD);
        properties.add("grab.seller-issuer.retiring-aliases[0]", () -> "seller-previous");
        properties.add("server.servlet.session.cookie.secure", () -> false);
    }

    @AfterAll
    static void stop() throws IOException {
        REDIS.stop();
        DATABASE.stop();
        if (keystore != null) {
            Files.deleteIfExists(keystore);
        }
    }

    @BeforeEach
    void setup() {
        CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        browser = HttpClient.newBuilder().cookieHandler(cookies).build();
        when(account.verify("seller@example.com", "password")).thenReturn(new VerifiedUser("seller-1", 1));
        when(account.resolve("seller-1", null, false)).thenReturn(state(null, null, 0, 1));
        when(account.resolve("seller-1", null, true)).thenReturn(state(null, null, 0, 1));
        when(account.resolve("seller-1", "assignment-a", false)).thenReturn(state("assignment-a", "merchant-a", 2, 1));
        when(account.resolve("seller-1", "assignment-b", false)).thenReturn(state("assignment-b", "merchant-b", 3, 1));
    }

    @Test
    void authorizationCodeSwitchRefreshAndReplay() throws Exception {
        JsonNode login = signIn();
        String initialAccess = login.path("access_token").asText();
        String initialRefresh = login.path("refresh_token").asText();
        assertThat(claims(initialAccess).has("assignment_id")).isFalse();
        assertThat(introspect(initialAccess).path("active").asBoolean()).isTrue();
        HttpResponse<String> userInfo = userInfo(initialAccess);
        assertThat(userInfo.statusCode()).withFailMessage(userInfo.body()).isEqualTo(200);
        assertThat(json.readTree(userInfo.body()).path("sub").asText()).isEqualTo("seller-1");

        HttpResponse<String> denied = select(initialAccess, "foreign-assignment");
        assertThat(json.readTree(denied.body()).path("error").asText()).isEqualTo("access_denied");
        assertThat(introspect(initialAccess).path("active").asBoolean()).isTrue();

        HttpResponse<String> scoped = select(initialAccess, "assignment-a");
        assertThat(scoped.statusCode()).withFailMessage(scoped.body()).isEqualTo(200);
        JsonNode selected = json.readTree(scoped.body());
        String scopedAccess = selected.path("access_token").asText();
        String scopedRefresh = selected.path("refresh_token").asText();
        assertThat(scopedRefresh).isNotEqualTo(initialRefresh);
        assertThat(claims(scopedAccess).path("scope_id").asText()).isEqualTo("merchant-a");
        assertThat(introspect(initialAccess).path("active").asBoolean()).isFalse();
        assertThat(introspect(scopedAccess).path("authorities").get(0).asText()).isEqualTo("CATALOG_READ");

        HttpResponse<String> refresh = refresh(scopedRefresh);
        assertThat(refresh.statusCode()).withFailMessage(refresh.body()).isEqualTo(200);
        String refreshedAccess = json.readTree(refresh.body()).path("access_token").asText();
        assertThat(claims(refreshedAccess).path("assignment_id").asText()).isEqualTo("assignment-a");
        assertThat(introspect(refreshedAccess).path("active").asBoolean()).isTrue();

        HttpResponse<String> replay = refresh(scopedRefresh);
        assertThat(replay.statusCode()).isEqualTo(400);
        assertThat(json.readTree(replay.body()).path("error").asText()).isEqualTo("invalid_grant");
        assertThat(introspect(refreshedAccess).path("active").asBoolean()).isFalse();
        assertThat(userInfo(refreshedAccess).statusCode()).isEqualTo(401);
    }

    @Test
    void liveRevisionAndAccountVersionInvalidateTokensAndRefresh() throws Exception {
        when(account.resolve("seller-1", null, true)).thenReturn(state("assignment-b", "merchant-b", 3, 1));
        JsonNode login = signIn();
        String access = login.path("access_token").asText();
        String refresh = login.path("refresh_token").asText();
        assertThat(claims(access).path("scope_id").asText()).isEqualTo("merchant-b");

        when(account.resolve("seller-1", "assignment-b", false)).thenThrow(
                new SellerAccountAuthentication.AccountUnavailable(new IOException()));
        HttpResponse<String> outage = protocol("/oauth2/introspect", Map.of("token", access));
        assertThat(outage.statusCode()).isEqualTo(503);

        doReturn(state("assignment-b", "merchant-b", 4, 2)).when(account).resolve("seller-1", "assignment-b", false);
        assertThat(introspect(access).path("active").asBoolean()).isFalse();
        assertThat(refresh(refresh).statusCode()).isEqualTo(400);
    }

    @Test
    void logoutAfterRefreshRevokesFamilyAndEndsIssuerSession() throws Exception {
        JsonNode login = signIn();
        HttpResponse<String> refresh = refresh(login.path("refresh_token").asText());
        assertThat(refresh.statusCode()).withFailMessage(refresh.body()).isEqualTo(200);
        JsonNode rotated = json.readTree(refresh.body());
        String accessToken = rotated.path("access_token").asText();
        String refreshToken = rotated.path("refresh_token").asText();
        String idToken = rotated.path("id_token").asText();

        HttpResponse<String> revoked = protocol("/oauth2/revoke", Map.of("token", refreshToken));
        assertThat(revoked.statusCode()).withFailMessage(revoked.body()).isEqualTo(200);
        assertThat(introspect(accessToken).path("active").asBoolean()).isFalse();

        String params = form(Map.of("id_token_hint", idToken, "post_logout_redirect_uri", "http://localhost:9999/"));
        HttpResponse<String> logout = get(local("/connect/logout?" + params));
        assertThat(logout.statusCode()).withFailMessage(logout.body()).isEqualTo(302);
        assertThat(logout.headers().firstValue("Location")).contains("http://localhost:9999/");
        HttpResponse<String> authorization = get(authorizationRequest());
        assertThat(authorization.headers().firstValue("Location").orElseThrow()).endsWith("/login");
    }

    @Test
    void concurrentContextSelectionsProduceOnlyOneActiveReplacement() throws Exception {
        JsonNode login = signIn();
        String initial = login.path("access_token").asText();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<HttpResponse<String>> first = executor.submit(() -> select(initial, "assignment-a"));
            Future<HttpResponse<String>> second = executor.submit(() -> select(initial, "assignment-b"));
            List<HttpResponse<String>> responses = List.of(first.get(), second.get());
            List<Integer> statuses = responses.stream().map(HttpResponse::statusCode).toList();
            assertThat(statuses).containsExactlyInAnyOrder(200, 400);
            HttpResponse<String> success = responses.stream().filter(response -> response.statusCode() == 200)
                    .findFirst().orElseThrow();
            String replacement = json.readTree(success.body()).path("access_token").asText();
            assertThat(introspect(initial).path("active").asBoolean()).isFalse();
            assertThat(introspect(replacement).path("active").asBoolean()).isTrue();
        }
    }

    @Test
    void exposesBothVerificationKeysAndSignsWithTheCurrentKey() throws Exception {
        HttpResponse<String> response = get(local("/oauth2/jwks"));
        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode keys = json.readTree(response.body()).path("keys");
        assertThat(keys).hasSize(2);
        assertThat(keys).extracting(key -> key.path("kid").asText())
                .containsExactlyInAnyOrder("seller-current", "seller-previous");
        assertThat(keys).allMatch(key -> !key.has("d") && key.has("n"));

        JsonNode login = signIn();
        String token = login.path("access_token").asText();
        String header = token.split("\\.")[0];
        byte[] decoded = Base64.getUrlDecoder().decode(header);
        assertThat(json.readTree(decoded).path("kid").asText()).isEqualTo("seller-current");
    }

    private URI authorizationRequest() throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(VERIFIER.getBytes(StandardCharsets.US_ASCII));
        String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        String parameters = form(Map.of("response_type", "code", "client_id", "seller-bff", "scope", "openid",
                "redirect_uri", CALLBACK, "code_challenge", challenge, "code_challenge_method", "S256",
                "state", "fresh-state", "nonce", "fresh-nonce"));
        return local("/oauth2/authorize?" + parameters);
    }

    private JsonNode signIn() throws Exception {
        URI authorize = authorizationRequest();
        HttpResponse<String> start = get(authorize);
        assertThat(start.statusCode()).withFailMessage(start.body()).isEqualTo(302);
        HttpResponse<String> login = get(local("/login"));
        assertThat(login.statusCode()).isEqualTo(200);
        Matcher csrf = CSRF.matcher(login.body());
        assertThat(csrf.find()).withFailMessage(login.body()).isTrue();
        HttpResponse<String> completedLogin = post(local("/login"), Map.of("username", "seller@example.com",
                "password", "password", "_csrf", csrf.group(1)), false);
        assertThat(completedLogin.statusCode()).withFailMessage(completedLogin.body()).isEqualTo(302);
        HttpResponse<String> authorization = get(authorize);
        assertThat(authorization.statusCode()).withFailMessage(authorization.body()).isEqualTo(302);
        String location = authorization.headers().firstValue("Location").orElseThrow();
        assertThat(location).startsWith(CALLBACK).contains("code=");
        String code = URI.create(location).getQuery().split("code=")[1].split("&")[0];
        HttpResponse<String> token = protocol("/oauth2/token", Map.of("grant_type", "authorization_code",
                "redirect_uri", CALLBACK, "code", code, "code_verifier", VERIFIER));
        assertThat(token.statusCode()).withFailMessage(token.body()).isEqualTo(200);
        return json.readTree(token.body());
    }

    private HttpResponse<String> select(String access, String assignment) throws Exception {
        return protocol("/oauth2/token", Map.of("grant_type", SellerContextGrant.TYPE.getValue(),
                "subject_token", access, "assignment_id", assignment));
    }

    private HttpResponse<String> refresh(String refresh) throws Exception {
        return protocol("/oauth2/token", Map.of("grant_type", "refresh_token", "refresh_token", refresh));
    }

    private JsonNode introspect(String access) throws Exception {
        HttpResponse<String> response = protocol("/oauth2/introspect", Map.of("token", access));
        assertThat(response.statusCode()).withFailMessage(response.body()).isEqualTo(200);
        return json.readTree(response.body());
    }

    private HttpResponse<String> userInfo(String accessToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(local("/userinfo"))
                .header("Authorization", "Bearer " + accessToken).GET().build();
        return browser.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> protocol(String path, Map<String, String> parameters) throws Exception {
        return post(local(path), parameters, true);
    }

    private HttpResponse<String> post(URI uri, Map<String, String> parameters, boolean client) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(uri)
                .header("Content-Type", "application/x-www-form-urlencoded");
        if (client) {
            String basic = Base64.getEncoder().encodeToString(("seller-bff:" + CLIENT_SECRET).getBytes(StandardCharsets.UTF_8));
            request.header("Authorization", "Basic " + basic);
        }
        return browser.send(request.POST(HttpRequest.BodyPublishers.ofString(form(parameters))).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(URI uri) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri).header("Accept", "text/html").GET().build();
        return browser.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private URI local(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private JsonNode claims(String token) throws IOException {
        return json.readTree(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
    }

    private static String form(Map<String, String> parameters) {
        return parameters.entrySet().stream().map(entry -> entry.getKey() + "="
                + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)).collect(Collectors.joining("&"));
    }

    private static AuthenticationState state(String assignment, String scopeId, long revision, long version) {
        String key = assignment == null ? null : "merchant.account";
        Set<String> roles = assignment == null ? Set.of() : Set.of("OWNER");
        Set<String> authorities = assignment == null ? Set.of() : Set.of("CATALOG_READ");
        return new AuthenticationState("seller-1", "seller@example.com", "ACTIVE", version, assignment,
                key, scopeId, roles, authorities, revision, 0);
    }

    private static Path createKeyStore() throws Exception {
        Path store = Files.createTempFile("seller-issuer", ".p12");
        Files.delete(store);
        addKey(store, "seller-current");
        addKey(store, "seller-previous");
        return store;
    }

    private static void addKey(Path store, String alias) throws Exception {
        String keytool = Path.of(System.getProperty("java.home"), "bin", "keytool").toString();
        Process generate = new ProcessBuilder(keytool, "-genkeypair", "-alias", alias, "-keyalg", "RSA",
                "-keysize", "2048", "-dname", "CN=Issuer", "-storetype", "PKCS12", "-keystore", store.toString(),
                "-storepass", PASSWORD, "-validity", "7", "-noprompt").redirectErrorStream(true).start();
        String output = new String(generate.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertThat(generate.waitFor()).withFailMessage(output).isZero();
    }
}
