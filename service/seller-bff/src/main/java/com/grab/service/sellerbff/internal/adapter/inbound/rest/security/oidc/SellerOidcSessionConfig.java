package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.data.redis.RedisSessionRepository;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration(proxyBeanMethods = false)
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
}
