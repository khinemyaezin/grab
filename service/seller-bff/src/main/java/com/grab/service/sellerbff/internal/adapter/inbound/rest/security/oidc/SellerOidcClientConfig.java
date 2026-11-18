package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SellerOidcProperties.class)
class SellerOidcClientConfig {

    @Bean
    DefaultOAuth2AuthorizationRequestResolver sellerAuthorizationRequests(ClientRegistrationRepository registrations) {
        DefaultOAuth2AuthorizationRequestResolver requests =
                new DefaultOAuth2AuthorizationRequestResolver(registrations, SellerOidcRoutes.AUTHORIZATION_BASE);
        requests.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());
        return requests;
    }

    @Bean
    ClientHttpRequestFactory sellerIssuerRequests(SellerOidcProperties properties) {
        HttpClient client = HttpClient.newBuilder().connectTimeout(properties.connectTimeout()).build();
        JdkClientHttpRequestFactory requests = new JdkClientHttpRequestFactory(client);
        requests.setReadTimeout(properties.readTimeout());
        return requests;
    }

    @Bean
    JwtDecoderFactory<ClientRegistration> sellerIdTokenDecoderFactory(SellerOidcProperties properties) {
        OidcIdTokenDecoderFactory factory = new OidcIdTokenDecoderFactory();
        return registration -> {
            // Check the public issuer without asking Spring Boot to discover provider metadata.
            ClientRegistration expectedIssuer = ClientRegistration.withClientRegistration(registration)
                    .issuerUri(properties.issuer())
                    .build();
            return factory.createDecoder(expectedIssuer);
        };
    }

    @Bean
    SellerIssuerClient sellerIssuerClient(SellerOidcProperties properties, MeterRegistry metrics,
                                          ClientHttpRequestFactory issuerRequests,
                                          ClientRegistrationRepository registrations) {
        ClientRegistration registration = registrations.findByRegistrationId(SellerOidcRoutes.REGISTRATION_ID);
        if (registration == null) {
            throw new IllegalArgumentException("The seller OAuth client registration is required");
        }
        ClientRegistration.ProviderDetails provider = registration.getProviderDetails();
        URI tokenUri = providerUri("token-uri", provider.getTokenUri());
        URI jwkSetUri = providerUri("jwk-set-uri", provider.getJwkSetUri());
        RestTemplate jwks = new RestTemplate(issuerRequests);
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri.toString())
                .restOperations(jwks).validateType(false).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(properties.issuer()),
                jwt -> "at+jwt".equals(jwt.getHeaders().get("typ")) && jwt.getExpiresAt() != null
                        && jwt.getAudience() != null && jwt.getAudience().contains(properties.audience())
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"))));
        return new SellerIssuerClient(properties, tokenUri, decoder, metrics, issuerRequests);
    }
    private static URI providerUri(String property, String value) {
        String error = "spring.security.oauth2.client.provider.seller." + property
                + " must be an absolute HTTP(S) URL without credentials or a fragment";
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(error);
        }
        URI uri;
        try {
            uri = URI.create(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(error);
        }
        if (uri.getHost() == null || uri.getUserInfo() != null || uri.getFragment() != null
                || !("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))) {
            throw new IllegalArgumentException(error);
        }
        return uri;
    }
}
