package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.file.Files;

@Component
@Profile("prod")
final class SellerProductionConfigurationValidator implements EnvironmentAware {
    private final SellerOidcProperties seller;
    private final SellerOidcSecurityConfig.CorsProperties cors;
    private final RedisProperties redis;
    private Environment environment;

    SellerProductionConfigurationValidator(SellerOidcProperties seller,
                                           SellerOidcSecurityConfig.CorsProperties cors,
                                           RedisProperties redis) {
        this.seller = seller;
        this.cors = cors;
        this.redis = redis;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    void validate() {
        requireConfigured("SELLER_OIDC_ISSUER");
        requireConfigured("SELLER_OIDC_AUDIENCE");
        requireConfigured("SELLER_OIDC_SCOPES");
        requireConfigured("SELLER_OIDC_CLIENT_ID");
        requireConfigured("SELLER_OIDC_RESOURCE_CLIENT_ID");
        requireConfigured("SELLER_OIDC_CALLBACK");
        requireConfigured("SELLER_OIDC_UI");
        requireConfigured("SELLER_OIDC_POST_LOGOUT_URI");
        requireConfigured("SELLER_AUTH_ACCOUNT_HOST");
        requireConfigured("SELLER_AUTH_REGISTRATION_PORT");
        requireConfigured("SELLER_OIDC_REDIS_NAMESPACE");
        requireConfigured("SELLER_OIDC_REDIS_URL");
        requireHttps("issuer", seller.issuer());
        requireHttps("callback", seller.callback());
        requireHttps("ui", seller.ui().toString());
        requireHttps("post-logout", seller.postLogout().toString());
        if (!seller.secureCookies()) {
            throw invalid("SELLER_OIDC_SECURE_COOKIES must be true in production");
        }
        if (seller.clientSecret() == null || seller.clientSecret().isBlank()
                || seller.resourceClientSecret() == null || seller.resourceClientSecret().isBlank()) {
            throw invalid("OAuth client and introspection secrets must be supplied by a deployment secret source");
        }
        if (cors.cors() == null || cors.cors().isEmpty() || cors.cors().stream().anyMatch("*"::equals)) {
            throw invalid("SECURITY_API_CORS must contain explicit origins and no wildcard");
        }
        if (!redis.getSsl().isEnabled() || redis.getPassword() == null || redis.getPassword().isBlank()) {
            throw invalid("Redis TLS and authentication must be enabled in production");
        }
        if (seller.accountKeyPassword() == null || seller.accountKeyPassword().isBlank()
                || seller.accountTrustPassword() == null || seller.accountTrustPassword().isBlank()
                || !Files.isReadable(seller.accountKeyStore()) || !Files.isReadable(seller.accountTrustStore())) {
            throw invalid("Seller Account mTLS secret files and passwords must be configured and readable");
        }
    }

    private void requireConfigured(String name) {
        requireText(name, environment.getProperty(name));
    }

    private void requireHttps(String name, String value) {
        URI uri;
        try {
            uri = URI.create(value);
        } catch (IllegalArgumentException exception) {
            throw invalid(name + " must be an absolute HTTPS URL");
        }
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null
                || uri.getFragment() != null) {
            throw invalid(name + " must be an absolute HTTPS URL without credentials or fragments");
        }
    }

    private void requireText(String name, String value) {
        if (value == null || value.isBlank()) {
            throw invalid(name + " is required");
        }
    }

    private IllegalStateException invalid(String message) {
        return new IllegalStateException("Invalid production Seller BFF configuration: " + message);
    }
}
