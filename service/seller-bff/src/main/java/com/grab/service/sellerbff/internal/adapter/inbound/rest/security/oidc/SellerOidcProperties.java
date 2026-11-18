package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

@ConfigurationProperties("grab.seller-oidc")
public record SellerOidcProperties(String issuer, String audience, List<String> scopes, String clientId,
                                   String clientSecret, String resourceClientId, String resourceClientSecret,
                                   String callback, URI ui, URI postLogout,
                                   String metadataUri, String authorizationEndpoint,
                                   String tokenEndpoint, String jwkSetEndpoint,
                                   String introspectionEndpoint, String revocationEndpoint, String logoutEndpoint,
                                   String accountHost, int registrationPort,
                                   Path accountKeyStore, String accountKeyPassword,
                                   Path accountTrustStore, String accountTrustPassword,
                                   @DefaultValue("2s") Duration accountConnectTimeout,
                                   @DefaultValue("6s") Duration accountReadTimeout,
                                   @DefaultValue("true") boolean secureCookies,
                                   @DefaultValue("GRAB_SELLER_SESSION") String cookieName,
                                   @DefaultValue("grab:seller") String redisNamespace,
                                   @DefaultValue("7d") Duration sessionTimeout,
                                   @DefaultValue("2s") Duration connectTimeout,
                                   @DefaultValue("6s") Duration readTimeout,
                                   @DefaultValue("5s") Duration refreshSkew,
                                   @DefaultValue("30s") Duration lockTtl,
                                   Duration revocationRetryDelay, Duration revocationTokenTtl,
                                   Duration revocationClaimLease, int revocationBatchSize) {

    public SellerOidcProperties {
        scopes = scopes == null ? List.of() : scopes.stream().filter(scope -> scope != null && !scope.isBlank())
                .map(String::trim).distinct().toList();
        metadataUri = optionalValue(metadataUri);
        authorizationEndpoint = optionalValue(authorizationEndpoint);
        tokenEndpoint = optionalValue(tokenEndpoint);
        jwkSetEndpoint = optionalValue(jwkSetEndpoint);
        introspectionEndpoint = optionalValue(introspectionEndpoint);
        revocationEndpoint = optionalValue(revocationEndpoint);
        logoutEndpoint = optionalValue(logoutEndpoint);
        validateBase("issuer", issuer);
        requireText("audience", audience);
        if (scopes.isEmpty() || !scopes.contains("openid")) {
            throw invalid("scopes", "must include openid and at least one scope");
        }
        requireText("client-id", clientId);
        requireText("client-secret", clientSecret);
        if (resourceClientId == null || resourceClientId.isBlank()) {
            resourceClientId = clientId;
        }
        if (resourceClientSecret == null || resourceClientSecret.isBlank()) {
            resourceClientSecret = clientSecret;
        }
        URI callbackUri = parse("callback", callback);
        validateUrl("callback", callbackUri);
        String callbackPath = callbackUri.getPath();
        if (callbackUri.getFragment() != null || !callbackPath.endsWith(SellerOidcRoutes.CALLBACK)) {
            throw invalid("callback", "must end in " + SellerOidcRoutes.CALLBACK + " and have no fragment");
        }
        validateUrl("ui", ui);
        validateUrl("post-logout", postLogout);
        validateOptionalEndpoint("introspection-endpoint", introspectionEndpoint);
        validateOptionalEndpoint("revocation-endpoint", revocationEndpoint);
        validateOptionalEndpoint("logout-endpoint", logoutEndpoint);
        validateOptionalEndpoint("metadata-uri", metadataUri);
        validateOptionalEndpoint("authorization-endpoint", authorizationEndpoint);
        validateOptionalEndpoint("token-endpoint", tokenEndpoint);
        validateOptionalEndpoint("jwk-set-endpoint", jwkSetEndpoint);
        requireText("account-host", accountHost);
        if (registrationPort < 1 || registrationPort > 65535) {
            throw invalid("registration-port", "must be a valid TCP port");
        }
        if (!cookieName.matches("[A-Za-z0-9_-]+")) {
            throw invalid("cookie-name", "must contain only letters, digits, hyphens or underscores");
        }
        if (!redisNamespace.matches("[A-Za-z0-9_-]+(:[A-Za-z0-9_-]+)*")) {
            throw invalid("redis-namespace", "must use colon-separated alphanumeric, hyphen or underscore segments");
        }
        if (sessionTimeout.compareTo(Duration.ofSeconds(1)) < 0 || sessionTimeout.getNano() != 0
                || sessionTimeout.getSeconds() > Integer.MAX_VALUE) {
            throw invalid("session-timeout", "must be a positive whole number of seconds within the supported range");
        }
        validateHttpTimeout("connect-timeout", connectTimeout);
        validateHttpTimeout("read-timeout", readTimeout);
        if (refreshSkew.compareTo(Duration.ZERO) < 0) {
            throw invalid("refresh-skew", "must not be negative");
        }
        validateHttpTimeout("account-connect-timeout", accountConnectTimeout);
        validateHttpTimeout("account-read-timeout", accountReadTimeout);
        Duration maxCallBudget = lockTtl.minusSeconds(6).dividedBy(2);
        if (lockTtl.compareTo(Duration.ofSeconds(10)) < 0 || maxCallBudget.isNegative()) {
            throw invalid("lock-ttl", "must allow the configured dependency timeout budget and lease renewal margin");
        }
        Duration callBudget = connectTimeout.plus(readTimeout);
        if (callBudget.compareTo(maxCallBudget) > 0) {
            throw invalid("connect-timeout and read-timeout", "must total at most " + maxCallBudget);
        }
        if (accountConnectTimeout.plus(accountReadTimeout).compareTo(maxCallBudget) > 0) {
            throw invalid("account-connect-timeout and account-read-timeout", "must total at most " + maxCallBudget);
        }
        if (revocationRetryDelay == null || revocationRetryDelay.compareTo(Duration.ofMillis(1)) < 0) {
            throw invalid("revocation-retry-delay", "must be at least 1ms");
        }
        validateWholeSeconds("revocation-token-ttl", revocationTokenTtl, Duration.ZERO);
        validateWholeSeconds("revocation-claim-lease", revocationClaimLease, lockTtl);
        if (revocationBatchSize <= 0) {
            throw invalid("revocation-batch-size", "must be positive");
        }
    }

    public String browserLoginUrl() {
        URI callbackUri = URI.create(callback);
        String callbackPath = callbackUri.getPath();
        int contextLength = callbackPath.length() - SellerOidcRoutes.CALLBACK.length();
        String contextPath = callbackPath.substring(0, contextLength);
        return UriComponentsBuilder.newInstance()
                .scheme(callbackUri.getScheme()).host(callbackUri.getHost()).port(callbackUri.getPort())
                .path(contextPath + SellerOidcRoutes.LOGIN).build().encode().toUriString();
    }

    private static void validateBase(String name, String value) {
        URI base = parse(name, value);
        validateUrl(name, base);
        if (base.getFragment() != null || base.getQuery() != null || value.endsWith("/")) {
            throw invalid(name, "must have no query, fragment or trailing slash");
        }
    }

    private static URI parse(String name, String value) {
        requireText(name, value);
        try {
            return URI.create(value);
        } catch (IllegalArgumentException exception) {
            throw invalid(name, "must be a valid URI");
        }
    }

    private static void validateOptionalEndpoint(String name, String value) {
        if (value != null && !value.isBlank()) {
            validateUrl(name, parse(name, value));
        }
    }

    private static String optionalValue(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static void validateUrl(String name, URI uri) {
        if (uri == null || uri.getHost() == null || uri.getUserInfo() != null
                || !("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))) {
            throw invalid(name, "must be an absolute HTTP(S) URL without credentials");
        }
    }

    private static void validateWholeSeconds(String name, Duration duration, Duration minimum) {
        if (duration == null || duration.compareTo(minimum) <= 0 || duration.getNano() != 0) {
            throw invalid(name, "must be a whole number of seconds greater than " + minimum.toSeconds());
        }
    }

    private static void validateHttpTimeout(String name, Duration duration) {
        if (duration.compareTo(Duration.ofMillis(1)) < 0) {
            throw invalid(name, "must be at least 1ms");
        }
    }

    private static void requireText(String name, String value) {
        if (value == null || value.isBlank()) {
            throw invalid(name, "is required");
        }
    }

    private static IllegalArgumentException invalid(String name, String message) {
        return new IllegalArgumentException("grab.seller-oidc." + name + " " + message);
    }

    @Override
    public String toString() {
        return "SellerOidcProperties[redacted]";
    }
}
