package com.grab.service.authorizationserver.config;

import com.grab.service.authorizationserver.config.AuthorizationClientProperties.Client;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthorizationClientPoliciesTest {
    static final String HASH = "{bcrypt}" + new BCryptPasswordEncoder(4).encode("test-client-secret");

    @Test
    void validatesTheWholeRegistryAndRedactsCredentials() {
        Client first = client("portal", "portal-registration", "portal-api", "https://portal.example/callback");
        Client second = client("basic", "basic-registration", "basic-api", "https://basic.example/callback");
        AuthorizationClientPolicies policies = policies("prod", first, second);
        assertThat(policies.find("portal")).contains(first);
        assertThat(policies.audiences()).containsExactlyInAnyOrder("portal-api", "basic-api");
        assertThat(first.toString()).doesNotContain(first.clientSecretHash());
        assertThatThrownBy(() -> policies("prod")).hasMessageContaining("at least one");
        Client duplicateClient = client("portal", "another-registration", "basic-api", "https://basic.example/callback");
        Client duplicateRegistration = client("basic", "portal-registration", "basic-api", "https://basic.example/callback");
        for (Client duplicate : List.of(duplicateClient, duplicateRegistration)) {
            assertThatThrownBy(() -> policies("prod", first, duplicate)).hasMessageContaining("must be unique");
        }
        Client sharedAudience = client("second-client", "second-registration", "portal-api", "https://portal.example/alt-callback");
        AuthorizationClientPolicies sharedPolicies = policies("prod", first, sharedAudience);
        assertThat(sharedPolicies.audiences()).containsExactly("portal-api");
        assertThat(sharedPolicies.isActive("second-client")).isTrue();
        Client malformed = new Client("portal", "Portal", "{bcrypt}bad", first.redirectUris(), List.of(),
                List.of("openid"), "portal-api", null, Set.of(), null, null);
        assertThatThrownBy(() -> policies("prod", malformed)).hasMessageContaining("delegated BCrypt")
                .hasMessageNotContaining("{bcrypt}bad");
        Client unsupported = new Client("portal", "Portal", HASH, first.redirectUris(), List.of(),
                List.of("openid"), "portal-api", null, Set.of("unknown"), null, null);
        assertThatThrownBy(() -> policies("prod", unsupported)).hasMessageContaining("Unknown");
        Client invalidTtl = new Client("portal", "Portal", HASH, first.redirectUris(), List.of(),
                List.of("openid"), "portal-api", null, Set.of(), java.time.Duration.ofSeconds(-1), null);
        assertThatThrownBy(() -> policies("prod", invalidTtl)).hasMessageContaining("TTL must be positive");
    }

    @Test
    void acceptsLoopbackHttpOnlyInDevelopment() {
        Client localhost = client("portal", null, "portal-api", "http://localhost:8080/callback");
        assertThat(policies("dev", localhost).isActive("portal")).isTrue();
        assertThatThrownBy(() -> policies("prod", localhost)).hasMessageContaining("HTTPS");
        for (String uri : List.of("http://other.example/callback", "https://portal.example/callback#fragment",
                "https://user@portal.example/callback", "https://portal.example/callback,*")) {
            Client invalid = client("portal", null, "portal-api", uri);
            assertThatThrownBy(() -> policies("dev", invalid)).hasMessageContaining("redirects");
        }
    }

    static Client client(String id, String registration, String audience, String uri) {
        String hash = "{bcrypt}" + new BCryptPasswordEncoder(4).encode("secret-for-" + id);
        return new Client(id, id, hash, List.of(uri), List.of(), List.of("openid"), audience, registration, Set.of(), null, null);
    }

    static AuthorizationClientPolicies policies(String profile, Client... clients) {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles(profile);
        AuthorizationClientProperties properties = new AuthorizationClientProperties(List.of(clients));
        return new AuthorizationClientPolicies(properties, environment);
    }
}
