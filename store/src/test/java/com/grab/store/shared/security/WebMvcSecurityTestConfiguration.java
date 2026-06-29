package com.grab.store.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.framework.security.AccessTokenAuthenticator;
import com.grab.framework.security.AuthenticatedActor;
import com.grab.framework.security.PlatformIdentityResolver;
import com.grab.store.shared.security.expection.ProblemDetailAuthEntryPoint;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
public class WebMvcSecurityTestConfiguration {

    @Bean
    AccessTokenAuthenticator accessTokenAuthenticator() {
        return token -> {
            throw new UnsupportedOperationException("Authentication is disabled in controller tests");
        };
    }

    @Bean
    PlatformIdentityResolver platformIdentityResolver() {
        return new PlatformIdentityResolver() {
            @Override
            public AuthenticatedActor resolve(com.grab.framework.security.ExternalPrincipal principal) {
                throw new UnsupportedOperationException("Authentication is disabled in controller tests");
            }

            @Override
            public String localIssuer() {
                return "test-issuer";
            }
        };
    }

    @Bean
    ProblemDetailAuthEntryPoint problemDetailAuthEntryPoint(ObjectMapper objectMapper) {
        return new ProblemDetailAuthEntryPoint(objectMapper);
    }
}
