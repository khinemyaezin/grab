package com.grab.service.authorizationserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties("grab.authorization-server")
public record AuthorizationClientProperties(List<Client> clients) {
    public AuthorizationClientProperties {
        clients = clients == null ? List.of() : List.copyOf(clients);
    }

    public record Client(String clientId, String clientName, String clientSecretHash, List<String> redirectUris,
                         List<String> postLogoutRedirectUris, List<String> scopes, String audience,
                         String registrationId,
                         Duration accessTokenTtl, Duration refreshTokenTtl) {
        public Client {
            redirectUris = redirectUris == null ? List.of() : List.copyOf(redirectUris);
            postLogoutRedirectUris = postLogoutRedirectUris == null ? List.of() : List.copyOf(postLogoutRedirectUris);
            scopes = scopes == null ? List.of() : List.copyOf(scopes);
        }

        @Override
        public String toString() {
            return "AuthorizationClient[redacted]";
        }
    }
}
