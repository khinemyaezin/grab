package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.endpoint.DefaultRefreshTokenTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.AuthenticationMethod;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.SpringOpaqueTokenIntrospector;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;

import static com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerOidcRoutes.AUTHORIZATION_BASE;
import static com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerOidcRoutes.REGISTRATION_ID;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SellerOidcProperties.class)
class SellerOidcClientConfig {

    @Bean
    DefaultOAuth2AuthorizationRequestResolver sellerAuthorizationRequests(ClientRegistrationRepository registrations) {
        DefaultOAuth2AuthorizationRequestResolver requests =
                new DefaultOAuth2AuthorizationRequestResolver(registrations, AUTHORIZATION_BASE);
        requests.setAuthorizationRequestCustomizer(builder -> {
            OAuth2AuthorizationRequestCustomizers.withPkce().accept(builder);
            org.springframework.web.context.request.RequestAttributes attributes =
                    org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attributes instanceof org.springframework.web.context.request.ServletRequestAttributes servletAttrs) {
                String prompt = servletAttrs.getRequest().getParameter("prompt");
                if (prompt != null && !prompt.isBlank()) {
                    builder.additionalParameters(params -> params.put("prompt", prompt));
                }
            }
        });
        return requests;
    }

    @Bean
    public org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository authorizedClientRepository) {
        org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider provider =
                org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder.builder()
                        .authorizationCode()
                        .refreshToken()
                        .build();
        org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager manager =
                new org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientRepository);
        manager.setAuthorizedClientProvider(provider);
        return manager;
    }

    @Bean
    ClientHttpRequestFactory sellerIssuerRequests(SellerOidcProperties properties) {
        HttpClient client = HttpClient.newBuilder().connectTimeout(properties.connectTimeout()).build();
        JdkClientHttpRequestFactory requests = new JdkClientHttpRequestFactory(client);
        requests.setReadTimeout(properties.readTimeout());
        return requests;
    }

    @Bean
    @ConditionalOnMissingBean(ClientRegistrationRepository.class)
    ClientRegistrationRepository sellerClientRegistrations(SellerOidcProperties properties,
                                                             ClientHttpRequestFactory sellerIssuerRequests) {
        String discoveryUri = properties.metadataUri() == null || properties.metadataUri().isBlank()
                ? properties.issuer() + "/.well-known/openid-configuration" : properties.metadataUri();
        Map<String, Object> metadata;
        try {
            RestTemplate rest = new RestTemplate(sellerIssuerRequests);
            ResponseEntity<Map<String, Object>> responseEntity = rest.exchange(discoveryUri,
                    org.springframework.http.HttpMethod.GET, null,
                    new ParameterizedTypeReference<>() {
                    });
            Map<String, Object> response = responseEntity.getBody();
            metadata = response == null ? Map.of() : response;
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Unable to load trusted Seller OIDC provider metadata", exception);
        }
        Object discoveredIssuer = metadata.get("issuer");
        if (discoveredIssuer == null || !properties.issuer().equals(discoveredIssuer.toString())) {
            throw new IllegalArgumentException("Seller OIDC metadata issuer must match grab.seller-oidc.issuer");
        }
        String authorization = endpoint(properties, properties.authorizationEndpoint(), metadata,
                "authorization_endpoint", "authorization-endpoint");
        String token = endpoint(properties, properties.tokenEndpoint(), metadata, "token_endpoint", "token-endpoint");
        String jwks = endpoint(properties, properties.jwkSetEndpoint(), metadata, "jwks_uri", "jwk-set-endpoint");
        String userInfo = optionalEndpoint(properties, properties.userInfoEndpoint(), metadata.get("userinfo_endpoint"));
        ClientRegistration.Builder registration = ClientRegistration.withRegistrationId(REGISTRATION_ID)
                .clientId(properties.clientId())
                .clientSecret(properties.clientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(properties.callback())
                .scope(properties.scopes())
                .authorizationUri(authorization)
                .tokenUri(token)
                .jwkSetUri(jwks)
                .issuerUri(properties.issuer())
                .providerConfigurationMetadata(metadata)
                .clientName("Seller Central");
        if (userInfo != null) {
            registration.userInfoUri(userInfo)
                    .userInfoAuthenticationMethod(AuthenticationMethod.HEADER)
                    .userNameAttributeName("sub");
        }
        return new InMemoryClientRegistrationRepository(registration.build());
    }

    @Bean
    JwtDecoderFactory<ClientRegistration> sellerIdTokenDecoderFactory(SellerOidcProperties properties,
                                                                      ClientHttpRequestFactory sellerIssuerRequests) {
        return registration -> {
            RestTemplate jwks = new RestTemplate(sellerIssuerRequests);
            NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(registration.getProviderDetails().getJwkSetUri())
                    .jwsAlgorithm(org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256)
                    .restOperations(jwks).build();
            OAuth2TokenValidator<Jwt> issuer = JwtValidators.createDefaultWithIssuer(properties.issuer());
            OAuth2TokenValidator<Jwt> audience = new JwtClaimValidator<>("aud", values ->
                    values instanceof List<?> audiences && audiences.contains(registration.getClientId()));
            decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(issuer, audience));
            return decoder;
        };
    }

    @Bean
    OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> sellerAuthorizationCodeClient(
            ClientHttpRequestFactory sellerIssuerRequests) {
        RestTemplate rest = new RestTemplate(sellerIssuerRequests);
        rest.getMessageConverters().add(0, new OAuth2AccessTokenResponseHttpMessageConverter());
        rest.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        DefaultAuthorizationCodeTokenResponseClient client = new DefaultAuthorizationCodeTokenResponseClient();
        client.setRestOperations(rest);
        return client;
    }

    @Bean
    OAuth2AccessTokenResponseClient<OAuth2RefreshTokenGrantRequest> sellerRefreshTokenClient(
            ClientHttpRequestFactory sellerIssuerRequests) {
        RestTemplate rest = new RestTemplate(sellerIssuerRequests);
        rest.getMessageConverters().add(0, new OAuth2AccessTokenResponseHttpMessageConverter());
        rest.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        DefaultRefreshTokenTokenResponseClient client = new DefaultRefreshTokenTokenResponseClient();
        client.setRestOperations(rest);
        return client;
    }

    @Bean
    OpaqueTokenIntrospector sellerOpaqueTokenIntrospector(SellerOidcProperties properties,
                                                         ClientRegistrationRepository registrations,
                                                         ClientHttpRequestFactory sellerIssuerRequests) {
        ClientRegistration registration = requiredRegistration(registrations);
        String uri = configuredOrDiscovered(properties, properties.introspectionEndpoint(), registration,
                "introspection_endpoint", "introspection-uri");
        RestTemplate rest = new RestTemplate(sellerIssuerRequests);
        rest.getInterceptors().add(new BasicAuthenticationInterceptor(
                properties.resourceClientId(), properties.resourceClientSecret()));
        rest.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        return new SpringOpaqueTokenIntrospector(uri, rest);
    }

    @Bean
    SellerOAuthClient sellerOAuthClient(SellerOidcProperties properties,
                                        OAuth2AccessTokenResponseClient<OAuth2RefreshTokenGrantRequest> refreshClient,
                                        OpaqueTokenIntrospector introspector,
                                        ClientRegistrationRepository registrations,
                                        ClientHttpRequestFactory sellerIssuerRequests,
                                        MeterRegistry metrics) {
        ClientRegistration registration = requiredRegistration(registrations);
        String revocation = configuredOrDiscovered(properties, properties.revocationEndpoint(), registration,
                "revocation_endpoint", "revocation-uri");
        String logout = configuredOrDiscovered(properties, properties.logoutEndpoint(), registration,
                "end_session_endpoint", "logout-uri");
        return new SellerOAuthClient(properties, registration, refreshClient, introspector,
                revocation, logout, sellerIssuerRequests, metrics);
    }

    private static ClientRegistration requiredRegistration(ClientRegistrationRepository registrations) {
        ClientRegistration registration = registrations.findByRegistrationId(REGISTRATION_ID);
        if (registration == null) {
            throw new IllegalArgumentException("The seller OAuth client registration is required");
        }
        return registration;
    }

    private static String configuredOrDiscovered(SellerOidcProperties properties, String configured, ClientRegistration registration,
                                                 String metadataKey, String propertyKey) {
        String endpoint = configured;
        if (endpoint == null || endpoint.isBlank()) {
            ClientRegistration.ProviderDetails providerDetails = registration.getProviderDetails();
            Map<String, Object> metadata = providerDetails.getConfigurationMetadata();
            Object discovered = metadata.get(metadataKey);
            endpoint = discovered == null ? null : discovered.toString();
        }
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalArgumentException("grab.seller-oidc." + propertyKey
                    + " or trusted issuer metadata " + metadataKey + " is required");
        }
        return validateEndpoint(properties, endpoint, propertyKey);
    }

    private static String endpoint(SellerOidcProperties properties, String configured,
                                   Map<String, Object> metadata, String metadataKey, String propertyKey) {
        String endpoint = configured;
        if (endpoint == null || endpoint.isBlank()) {
            Object discovered = metadata.get(metadataKey);
            endpoint = discovered == null ? null : discovered.toString();
        }
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalArgumentException("grab.seller-oidc." + propertyKey
                    + " or trusted issuer metadata " + metadataKey + " is required");
        }
        return validateEndpoint(properties, endpoint, propertyKey);
    }

    private static String optionalEndpoint(SellerOidcProperties properties, String configured, Object discovered) {
        String endpoint = configured;
        if (endpoint == null || endpoint.isBlank()) {
            endpoint = discovered == null ? null : discovered.toString();
        }
        return endpoint == null || endpoint.isBlank()
                ? null : validateEndpoint(properties, endpoint, "user-info-endpoint");
    }

    private static String validateEndpoint(SellerOidcProperties properties, String endpoint, String propertyKey) {
        URI uri;
        try {
            uri = URI.create(endpoint);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("grab.seller-oidc." + propertyKey
                    + " must be an absolute HTTP(S) URL", exception);
        }
        URI issuerUri = URI.create(properties.issuer());
        boolean secureIssuer = "https".equalsIgnoreCase(issuerUri.getScheme());
        if (uri.getHost() == null || uri.getUserInfo() != null || uri.getFragment() != null
                || secureIssuer && !"https".equalsIgnoreCase(uri.getScheme())
                || !("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))) {
            throw new IllegalArgumentException("grab.seller-oidc." + propertyKey
                    + " must be an absolute HTTP(S) URL without credentials or fragments");
        }
        return uri.toASCIIString();
    }
}
