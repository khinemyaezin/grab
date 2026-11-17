package com.grab.service.sellerbff.internal.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;
import com.grab.framework.security.ExternalPrincipal;
import com.grab.framework.security.PlatformIdentityResolver;
import com.grab.service.contract.message.account.identity.IdentityLookupPort;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;

@Configuration(proxyBeanMethods = false)
@ConditionalOnExpression("!${grab.seller-oidc.enabled:false} || ${grab.seller-oidc.legacy-bearer:true}")
public class SellerIdentityConfig {
    @Bean
    public PublicKey localJwtPublicKey(@Value("${security.local-jwt.public-key-path}") String keyPath) {
        if (keyPath == null || keyPath.isBlank()) throw new IllegalStateException("JWT_PUBLIC_KEY_PATH is required");
        try (InputStream input = Files.newInputStream(Path.of(keyPath))) {
            return CertificateFactory.getInstance("X.509").generateCertificate(input).getPublicKey();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load Account JWT public certificate", exception);
        }
    }

    @Bean
    public PlatformIdentityResolver platformIdentityResolver(
            IdentityLookupPort port,
            @Value("${security.local-jwt.issuer}") String issuer) {
        return new PlatformIdentityResolver() {
            @Override @SneakyThrows
            public AuthenticatedActor resolve(ExternalPrincipal principal) {
                IdentityLookupPort.AccessContextMessage context = principal.accessContext() == null ? null
                        : new IdentityLookupPort.AccessContextMessage(principal.accessContext().assignmentId(), principal.accessContext().scopeKey(), principal.accessContext().scopeId());
                IdentityLookupPort.AuthenticatedActorResult result = issuer.equals(principal.issuer())
                        ? port.resolveByPlatformUserId(principal.issuer(), principal.subject(), context)
                        : port.resolveByExternalIdentity(principal.issuer(), principal.subject(), principal.entitlements(), context);
                if (result == null) throw new IllegalArgumentException("Identity is not linked");
                AccessContext active = result.accessContext() == null ? null : new AccessContext(result.accessContext().assignmentId(), result.accessContext().scopeKey(), result.accessContext().scopeId());
                return new AuthenticatedActor(result.platformUserId(), result.issuer(), result.subject(), result.email(), result.roles(), result.authorities(), active, result.authenticationVersion());
            }
            @Override public String localIssuer() { return issuer; }
        };
    }
}
