package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;

@ConfigurationProperties("grab.seller-oidc")
public record SellerOidcProperties(String issuer, String backchannel, String audience, String clientId,
                                   String clientSecret, String callback, URI ui, URI postLogout,
                                   String accountHost, int registrationPort,
                                   @DefaultValue("true") boolean secureCookies,
                                   @DefaultValue("GRAB_SELLER_SESSION") String cookieName,
                                   @DefaultValue("grab:seller") String redisNamespace,
                                   @DefaultValue("7d") Duration sessionTimeout,
                                   @DefaultValue("2s") Duration connectTimeout,
                                   @DefaultValue("6s") Duration readTimeout,
                                   Duration revocationRetryDelay, Duration revocationTokenTtl,
                                   Duration revocationClaimLease, int revocationBatchSize) {

    static final Duration LOCK_TTL = Duration.ofSeconds(30);
    private static final Duration MAX_CALL_BUDGET = LOCK_TTL.minusSeconds(6).dividedBy(2);

    public SellerOidcProperties {
        validateBase("issuer", issuer);
        if (backchannel != null) {
            validateBase("backchannel", backchannel);
        }
        requireText("audience", audience);
        requireText("client-id", clientId);
        requireText("client-secret", clientSecret);
        URI callbackUri = parse("callback", callback);
        validateUrl("callback", callbackUri);
        String callbackPath = callbackUri.getPath();
        if (callbackUri.getFragment() != null || !callbackPath.endsWith(SellerOidcRoutes.CALLBACK)) {
            throw invalid("callback", "must end in " + SellerOidcRoutes.CALLBACK + " and have no fragment");
        }
        validateUrl("ui", ui);
        validateUrl("post-logout", postLogout);
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
        Duration callBudget = connectTimeout.plus(readTimeout);
        if (callBudget.compareTo(MAX_CALL_BUDGET) > 0) {
            throw invalid("connect-timeout and read-timeout", "must total at most " + MAX_CALL_BUDGET);
        }
        if (revocationRetryDelay == null || revocationRetryDelay.compareTo(Duration.ofMillis(1)) < 0) {
            throw invalid("revocation-retry-delay", "must be at least 1ms");
        }
        validateWholeSeconds("revocation-token-ttl", revocationTokenTtl, Duration.ZERO);
        validateWholeSeconds("revocation-claim-lease", revocationClaimLease, LOCK_TTL);
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

    String endpoint(String path) {
        String base = backchannel == null ? issuer : backchannel;
        return base + path;
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
        if (duration.compareTo(Duration.ofMillis(1)) < 0 || duration.compareTo(MAX_CALL_BUDGET) > 0) {
            throw invalid(name, "must be between 1ms and " + MAX_CALL_BUDGET);
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
