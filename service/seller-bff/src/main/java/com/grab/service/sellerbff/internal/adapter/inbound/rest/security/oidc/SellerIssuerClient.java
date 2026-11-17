package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

final class SellerIssuerClient {
    private final SellerOidcProperties properties;
    private final RestClient http;
    private final JwtDecoder decoder;
    private final MeterRegistry metrics;

    SellerIssuerClient(SellerOidcProperties properties, JwtDecoder decoder, MeterRegistry metrics) {
        this.properties = properties;
        this.decoder = decoder;
        this.metrics = metrics;
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        JdkClientHttpRequestFactory requests = new JdkClientHttpRequestFactory(client);
        requests.setReadTimeout(Duration.ofSeconds(6));
        http = RestClient.builder().requestFactory(requests).build();
    }

    Tokens refresh(Tokens previous) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", previous.refresh());
        return grant(form, previous);
    }

    Tokens select(Tokens previous, String assignmentId) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "urn:grab:oauth:grant-type:seller-context");
        form.add("subject_token", previous.access());
        form.add("assignment_id", assignmentId);
        return grant(form, previous);
    }

    AuthenticatedActor actor(Tokens tokens) {
        Jwt jwt;
        try {
            jwt = decoder.decode(tokens.access());
        } catch (BadJwtException exception) {
            throw SellerOidcFailure.invalid();
        } catch (JwtException exception) {
            throw SellerOidcFailure.unavailable();
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("token", tokens.access());
        form.add("token_type_hint", "access_token");
        Introspection state = metrics.timer("seller.oidc.introspection")
                .record(() -> request("/oauth2/introspect", form, Introspection.class));
        if (state == null || !state.active() || !Objects.equals(jwt.getSubject(), state.sub())
                || !Objects.equals(tokens.subject(), state.sub()) || state.email() == null
                || state.roles() == null || state.authorities() == null
                || !versionMatches(jwt, state.authenticationVersion())
                || !Objects.equals(jwt.getClaimAsString("assignment_id"), state.assignmentId())
                || !Objects.equals(jwt.getClaimAsString("scope_key"), state.scopeKey())
                || !Objects.equals(jwt.getClaimAsString("scope_id"), state.scopeId())) {
            throw SellerOidcFailure.invalid();
        }
        AccessContext context = state.assignmentId() == null ? null
                : new AccessContext(state.assignmentId(), state.scopeKey(), state.scopeId());
        return new AuthenticatedActor(state.sub(), properties.issuer(), state.sub(), state.email(),
                state.roles(), state.authorities(), context, state.authenticationVersion());
    }

    private boolean versionMatches(Jwt jwt, long current) {
        Object claim = jwt.getClaim("authentication_version");
        return claim instanceof Number version && version.longValue() == current;
    }

    void revoke(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("token", refreshToken);
        form.add("token_type_hint", "refresh_token");
        request("/oauth2/revoke", form, Void.class);
    }

    private Tokens grant(MultiValueMap<String, String> form, Tokens previous) {
        Grant response = request("/oauth2/token", form, Grant.class);
        if (response == null || response.access() == null || response.refresh() == null || response.expiresIn() <= 0) {
            throw SellerOidcFailure.unavailable();
        }
        Instant expiry = Instant.now().plusSeconds(response.expiresIn());
        String idToken = response.idToken() == null ? previous.idToken() : response.idToken();
        return new Tokens(response.access(), response.refresh(), expiry, previous.subject(), idToken);
    }

    private <T> T request(String path, MultiValueMap<String, String> form, Class<T> type) {
        try {
            URI endpoint = URI.create(properties.endpoint(path));
            String clientId = URLEncoder.encode(properties.clientId(), StandardCharsets.UTF_8);
            String secret = URLEncoder.encode(properties.clientSecret(), StandardCharsets.UTF_8);
            return http.post().uri(endpoint).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .headers(headers -> headers.setBasicAuth(clientId, secret))
                    .body(form).retrieve().body(type);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is5xxServerError()) {
                throw SellerOidcFailure.unavailable();
            }
            OAuthError error;
            try {
                error = exception.getResponseBodyAs(OAuthError.class);
            } catch (RestClientException invalidResponse) {
                throw SellerOidcFailure.unavailable();
            }
            if (error != null && "access_denied".equals(error.error())) {
                throw SellerOidcFailure.denied();
            }
            if (error != null && "invalid_grant".equals(error.error())) {
                throw SellerOidcFailure.invalid();
            }
            throw SellerOidcFailure.unavailable();
        } catch (RestClientException exception) {
            throw SellerOidcFailure.unavailable();
        }
    }

    record Tokens(String access, String refresh, Instant expiry, String subject, String idToken) {
        @Override
        public String toString() {
            return "Tokens[redacted]";
        }
    }

    private record Grant(@JsonProperty("access_token") String access, @JsonProperty("refresh_token") String refresh,
                         @JsonProperty("expires_in") long expiresIn, @JsonProperty("id_token") String idToken) {}

    private record OAuthError(String error) {}

    private record Introspection(boolean active, String sub, String email, Set<String> roles, Set<String> authorities,
                                 @JsonProperty("assignment_id") String assignmentId,
                                 @JsonProperty("scope_key") String scopeKey,
                                 @JsonProperty("scope_id") String scopeId,
                                 @JsonProperty("authentication_version") long authenticationVersion) {}
}
