package com.grab.service.account.identity.internal.config;

import com.grab.framework.id.IdGenerator;
import com.grab.service.account.merchant.port.SellerScopeRevisionQuery;
import com.identity.application.port.inbound.ResolveActiveSubjectUseCase;
import com.identity.application.port.inbound.IdentityLookupUseCase;
import com.identity.application.port.inbound.ResolveSellerAuthenticationUseCase;
import com.identity.application.port.inbound.ResolveSellerAccessUseCase;
import com.identity.application.port.inbound.VerifySellerCredentialsUseCase;
import com.identity.application.port.outbound.IdentityLookupQueryPort;
import com.identity.application.port.outbound.SellerScopeLifecyclePort;
import com.identity.application.port.outbound.SellerIdentityStateReader;
import com.identity.application.port.outbound.SellerScopeLifecyclePort.ScopeRevision;
import com.identity.application.service.ResolveActiveSubjectService;
import com.identity.application.service.ResolveSellerAuthenticationService;
import com.identity.application.service.ResolveSellerAccessService;
import com.identity.application.service.VerifySellerCredentialsService;
import com.identity.domain.service.PasswordHasher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;

@Configuration(proxyBeanMethods = false)
public class SellerAuthenticationUseCaseConfig {
    @Bean
    VerifySellerCredentialsUseCase verifySellerCredentials(IdentityLookupQueryPort identities, PasswordHasher passwords,
                                                          IdGenerator ids) {
        return new VerifySellerCredentialsService(identities, passwords, ids);
    }

    @Bean
    ResolveActiveSubjectUseCase resolveActiveSubject(IdentityLookupQueryPort identities) {
        return new ResolveActiveSubjectService(identities);
    }

    @Bean
    ResolveSellerAuthenticationUseCase resolveSellerAuthentication(SellerIdentityStateReader identities,
                                                                  SellerScopeLifecyclePort scopes, IdGenerator ids) {
        return new ResolveSellerAuthenticationService(identities, scopes, ids);
    }

    @Bean
    ResolveSellerAccessUseCase resolveSellerAccess(IdentityLookupUseCase identities,
                                                   ResolveSellerAuthenticationUseCase sellerAuthentication,
                                                   IdGenerator ids,
                                                   @Value("${grab.seller-authentication.trusted-native-issuer:}")
                                                   String trustedNativeIssuer) {
        return new ResolveSellerAccessService(identities, sellerAuthentication, ids, trustedNativeIssuer);
    }

    @Bean
    SellerScopeLifecyclePort sellerScopeLifecycle(SellerScopeRevisionQuery merchant) {
        return (key, id) -> {
            if (!"merchant.account".equals(key) && !"merchant.storefront".equals(key)) {
                return Optional.of(new ScopeRevision(0, 0));
            }
            SellerScopeRevisionQuery.Revision revision = merchant.find(key, id);
            return Optional.ofNullable(revision)
                    .map(value -> new ScopeRevision(value.merchantRevision(), value.storefrontRevision()));
        };
    }
}
