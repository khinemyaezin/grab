package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;

final class SellerOAuthClient {
    private static final Logger LOG = Loggers.getLogger(SellerOAuthClient.class);

    private final SellerOidcProperties properties;
    private final org.springframework.security.oauth2.client.registration.ClientRegistration registration;
    private final OAuth2AccessTokenResponseClient<OAuth2RefreshTokenGrantRequest> refreshClient;
    private final OpaqueTokenIntrospector introspector;
    private final RestClient revocationClient;
    private final String revocationUri;
    private final String logoutUri;
    private final MeterRegistry metrics;

    SellerOAuthClient(SellerOidcProperties properties,
                      org.springframework.security.oauth2.client.registration.ClientRegistration registration,
                      OAuth2AccessTokenResponseClient<OAuth2RefreshTokenGrantRequest> refreshClient,
                      OpaqueTokenIntrospector introspector, String revocationUri, String logoutUri,
                      org.springframework.http.client.ClientHttpRequestFactory requestFactory,
                      MeterRegistry metrics) {
        this.properties = properties;
        this.registration = registration;
        this.refreshClient = refreshClient;
        this.introspector = introspector;
        this.revocationUri = revocationUri;
        this.logoutUri = logoutUri;
        this.metrics = metrics;
        revocationClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    Tokens refresh(Tokens current) {
        OAuth2AccessToken access = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                current.access(), Instant.now().minusSeconds(1), current.expiry());
        OAuth2RefreshToken refresh = new OAuth2RefreshToken(current.refresh(), Instant.now().minusSeconds(1));
        OAuth2AccessTokenResponse response;
        try {
            Timer timer = metrics.timer("seller.oauth.refresh");
            OAuth2RefreshTokenGrantRequest request = new OAuth2RefreshTokenGrantRequest(registration, access, refresh);
            response = timer.record(() -> refreshClient.getTokenResponse(request));
        } catch (OAuth2AuthorizationException exception) {
            String error = exception.getError().getErrorCode();
            if ("invalid_grant".equals(error) || "invalid_token".equals(error)) {
                throw SellerOidcFailure.invalid();
            }
            if ("access_denied".equals(error)) {
                throw SellerOidcFailure.denied();
            }
            throw SellerOidcFailure.unavailable();
        } catch (RuntimeException exception) {
            LOG.warn("Seller OAuth refresh failed cause={}", exception.getClass().getSimpleName());
            throw SellerOidcFailure.unavailable();
        }
        OAuth2AccessToken replacement = response.getAccessToken();
        if (replacement == null || replacement.getExpiresAt() == null) {
            throw SellerOidcFailure.unavailable();
        }
        OAuth2RefreshToken replacementRefresh = response.getRefreshToken();
        return current.withTokens(replacement.getTokenValue(),
                replacementRefresh == null ? current.refresh() : replacementRefresh.getTokenValue(),
                replacement.getExpiresAt());
    }

    OAuth2AuthenticatedPrincipal introspect(String accessToken) {
        try {
            Timer timer = metrics.timer("seller.oauth.introspection");
            OAuth2AuthenticatedPrincipal principal = timer.record(() -> introspector.introspect(accessToken));
            Map<String, Object> claims = principal.getAttributes();
            String subject = principal.getName();
            Object issuer = claims.get("iss");
            Object audience = claims.get("aud");
            Object expiry = claims.get("exp");
            if (subject == null || subject.isBlank()
                    || issuer != null && !properties.issuer().equals(issuer.toString())
                    || !containsAudience(audience, properties.audience())
                    || !validExpiry(expiry)) {
                throw SellerOidcFailure.invalid();
            }
            return principal;
        } catch (SellerOidcFailure failure) {
            throw failure;
        } catch (BadOpaqueTokenException exception) {
            metrics.counter("seller.oauth.introspection", "outcome", "invalid").increment();
            throw SellerOidcFailure.invalid();
        } catch (RuntimeException exception) {
            metrics.counter("seller.oauth.introspection", "outcome", "unavailable").increment();
            throw SellerOidcFailure.unavailable();
        }
    }

    void revoke(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("token", refreshToken);
        form.add("token_type_hint", "refresh_token");
        try {
            revocationClient.post().uri(revocationUri).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .headers(headers -> headers.setBasicAuth(properties.clientId(), properties.clientSecret()))
                    .body(form).retrieve().toBodilessEntity();
            metrics.counter("seller.oauth.revocation.success").increment();
        } catch (RestClientResponseException exception) {
            if (!exception.getStatusCode().is5xxServerError()) {
                metrics.counter("seller.oauth.revocation.rejected").increment();
                return;
            }
            throw SellerOidcFailure.unavailable();
        } catch (RestClientException exception) {
            metrics.counter("seller.oauth.revocation.failure").increment();
            throw SellerOidcFailure.unavailable();
        }
    }

    String logoutUri() {
        return logoutUri;
    }

    private boolean containsAudience(Object audience, String expected) {
        if (audience instanceof String value) {
            return expected.equals(value);
        }
        if (audience instanceof Collection<?> values) {
            return values.stream().anyMatch(value -> expected.equals(value));
        }
        return false;
    }

    private boolean validExpiry(Object expiry) {
        if (expiry instanceof Number seconds) {
            return Instant.ofEpochSecond(seconds.longValue()).isAfter(Instant.now());
        }
        if (expiry instanceof Instant instant) {
            return instant.isAfter(Instant.now());
        }
        return false;
    }

    record Tokens(String access, String refresh, Instant expiry, String issuer, String subject, String idToken,
                  String platformUserId, String assignmentId, long authenticationVersion,
                  long merchantRevision, long storefrontRevision) {
        Tokens(String access, String refresh, Instant expiry, String subject, String idToken) {
            this(access, refresh, expiry, null, subject, idToken, null, null, 0, 0, 0);
        }

        Tokens withTokens(String newAccess, String newRefresh, Instant newExpiry) {
            return new Tokens(newAccess, newRefresh, newExpiry, issuer, subject, idToken, platformUserId,
                    assignmentId, authenticationVersion, merchantRevision, storefrontRevision);
        }

        Tokens withAccessState(String userId, String contextAssignmentId, long version,
                               long merchant, long storefront) {
            return new Tokens(access, refresh, expiry, issuer, subject, idToken, userId, contextAssignmentId,
                    version, merchant, storefront);
        }

        Tokens withIssuer(String tokenIssuer) {
            return new Tokens(access, refresh, expiry, tokenIssuer, subject, idToken, platformUserId,
                    assignmentId, authenticationVersion, merchantRevision, storefrontRevision);
        }

        @Override
        public String toString() {
            return "Tokens[redacted]";
        }
    }
}
