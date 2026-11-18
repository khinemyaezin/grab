package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.data.redis.RedisSessionRepository;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
@EnableRedisHttpSession
class SellerOidcSessionConfig {
    @Bean
    SessionRepositoryCustomizer<RedisSessionRepository> sellerSessionRepository(SellerOidcProperties properties) {
        return repository -> {
            repository.setRedisKeyNamespace(properties.redisNamespace() + ":sessions");
            repository.setDefaultMaxInactiveInterval(properties.sessionTimeout());
        };
    }

    @Bean
    CookieSerializer sellerOidcCookie(SellerOidcProperties properties) {
        DefaultCookieSerializer cookie = new DefaultCookieSerializer();
        cookie.setCookieName(properties.cookieName());
        cookie.setCookiePath("/");
        cookie.setUseSecureCookie(properties.secureCookies());
        cookie.setUseHttpOnlyCookie(true);
        cookie.setSameSite("Lax");
        return cookie;
    }

    @Bean
    SellerOidcTokenSlots sellerTokenSlots(StringRedisTemplate redis, ObjectMapper json, SellerOidcProperties properties) {
        return new SellerOidcTokenSlots(redis, json, properties);
    }

    @Bean
    SellerPendingRevocations sellerPendingRevocations(StringRedisTemplate redis, SellerIssuerClient issuer,
                                                       MeterRegistry metrics, SellerOidcTokenSlots slots,
                                                       SellerOidcProperties properties) {
        return new SellerPendingRevocations(redis, issuer, metrics, slots, properties);
    }

    @Bean
    SellerOidcBrowserSession sellerBrowserSession(SellerOidcTokenSlots slots, SellerIssuerClient issuer,
                                                  SellerPendingRevocations pending, SellerOidcProperties properties) {
        return new SellerOidcBrowserSession(issuer, slots, pending, properties);
    }
}
