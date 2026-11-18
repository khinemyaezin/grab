package com.grab.service.authorizationserver.config;

import com.grab.service.authorizationserver.config.AuthorizationClientProperties.Client;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public final class AuthorizationClientPolicies {
    public static final String ASSIGNMENT_CONTEXT = "seller-context";

    private static final Pattern BCRYPT = Pattern.compile("\\{bcrypt}\\$2[aby]\\$(?:0[4-9]|[12][0-9]|3[01])\\$[./A-Za-z0-9]{53}");
    private static final Pattern SCOPE = Pattern.compile("[!#-+\\--\\[\\]-~]+");

    private final Map<String, Client> clients;
    private final Set<String> audiences;

    public AuthorizationClientPolicies(AuthorizationClientProperties configured, Environment environment) {
        boolean allowLoopback = environment.acceptsProfiles(Profiles.of("dev & !prod"));
        if (configured.clients().isEmpty()) {
            throw new IllegalStateException("Configure at least one authorization client");
        }
        Map<String, Client> validated = new HashMap<>();
        Set<String> audiences = new HashSet<>();
        Set<String> ids = new HashSet<>();
        Set<String> hashes = new HashSet<>();
        for (Client client : configured.clients()) {
            validate(client, allowLoopback);
            if (validated.putIfAbsent(client.clientId(), client) != null
                    || (client.registrationId() != null && !ids.add(client.registrationId()))) {
                throw new IllegalStateException("Authorization client IDs and registration IDs must be unique");
            }
            audiences.add(client.audience());
            if (!hashes.add(client.clientSecretHash())) {
                throw new IllegalStateException("Each authorization client requires a distinct secret hash");
            }
        }
        clients = Map.copyOf(validated);
        this.audiences = Set.copyOf(audiences);
    }

    private static void validate(Client client, boolean allowLoopback) {
        if (!validText(client.clientId(), 100) || !validText(client.clientName(), 200)
                || !validText(client.audience(), 200)
                || (client.registrationId() != null && !validText(client.registrationId(), 100))) {
            throw new IllegalStateException("Invalid authorization client ID, display name, registration ID or audience");
        }
        if (client.clientSecretHash() == null || !BCRYPT.matcher(client.clientSecretHash()).matches()) {
            throw new IllegalStateException("Authorization client secrets require a delegated BCrypt hash");
        }
        if (!Set.of(ASSIGNMENT_CONTEXT).containsAll(client.capabilities())) {
            throw new IllegalStateException("Unknown authorization client capability");
        }
        if (!client.scopes().contains(OidcScopes.OPENID) || String.join(",", client.scopes()).length() > 1000
                || client.scopes().stream().anyMatch(scope -> !SCOPE.matcher(scope).matches())) {
            throw new IllegalStateException("Authorization client scopes must be valid and include openid");
        }
        if (client.redirectUris().isEmpty() || String.join(",", client.redirectUris()).length() > 1000
                || String.join(",", client.postLogoutRedirectUris()).length() > 1000
                || Stream.concat(client.redirectUris().stream(), client.postLogoutRedirectUris().stream())
                        .anyMatch(value -> !validUri(value, allowLoopback))) {
            throw new IllegalStateException("Authorization client redirects require absolute HTTPS URIs or development loopback HTTP");
        }
        if (client.accessTokenTtl() != null && (client.accessTokenTtl().isNegative() || client.accessTokenTtl().isZero())) {
            throw new IllegalStateException("Authorization client access token TTL must be positive");
        }
        if (client.refreshTokenTtl() != null && (client.refreshTokenTtl().isNegative() || client.refreshTokenTtl().isZero())) {
            throw new IllegalStateException("Authorization client refresh token TTL must be positive");
        }
    }

    private static boolean validText(String value, int maxLength) {
        return value != null && !value.isBlank() && value.length() <= maxLength && value.equals(value.strip());
    }

    static boolean validUri(String value, boolean allowLoopback) {
        if (value.contains(",")) {
            return false;
        }
        try {
            URI uri = URI.create(value);
            String host = uri.getHost();
            if (host == null || uri.getUserInfo() != null || uri.getFragment() != null || uri.getPort() > 65535) {
                return false;
            }
            boolean loopback = "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host) || "[::1]".equals(host);
            return "https".equalsIgnoreCase(uri.getScheme())
                    || (allowLoopback && loopback && "http".equalsIgnoreCase(uri.getScheme()));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public List<Client> all() {
        return List.copyOf(clients.values());
    }

    public Optional<Client> find(String clientId) {
        return Optional.ofNullable(clients.get(clientId));
    }

    public boolean isActive(String clientId) {
        return clients.containsKey(clientId);
    }

    public Set<String> audiences() {
        return audiences;
    }

    public boolean hasAssignmentContext(String clientId) {
        return find(clientId).map(client -> client.capabilities().contains(ASSIGNMENT_CONTEXT)).orElse(false);
    }
}
