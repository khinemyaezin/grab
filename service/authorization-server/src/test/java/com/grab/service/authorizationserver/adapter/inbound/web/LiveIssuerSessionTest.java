package com.grab.service.authorizationserver.adapter.inbound.web;

import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort;
import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort.Subject;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.session.Session;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.data.redis.RedisIndexedSessionRepository.RedisSession;
import org.testcontainers.containers.GenericContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LiveIssuerSessionTest {
    @Test
    void reloadsAndRevalidatesALegacyIssuerSessionFromRedis() throws Exception {
        try (GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379)) {
            redis.start();
            String host = redis.getHost();
            int port = redis.getMappedPort(6379);
            RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(host, port);
            LettuceConnectionFactory connection = new LettuceConnectionFactory(configuration);
            connection.afterPropertiesSet();
            try {
                RedisIndexedSessionRepository oldRepository = repository(connection);
                RedisIndexedSessionRepository newRepository = repository(connection);
                String contextKey = HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;
                SimpleGrantedAuthority legacy = new SimpleGrantedAuthority("SELLER_LOGIN_VERSION_4");
                UsernamePasswordAuthenticationToken principal =
                        UsernamePasswordAuthenticationToken.authenticated("subject", null, List.of(legacy));
                SecurityContext original = SecurityContextHolder.createEmptyContext();
                original.setAuthentication(principal);
                RedisSession session = oldRepository.createSession();
                session.setAttribute(contextKey, original);
                oldRepository.save(session);

                Session restored = newRepository.findById(session.getId());
                SecurityContext context = restored.getAttribute(contextKey);
                SecurityContextHolder.setContext(context);
                AccountIdentityPort account = mock(AccountIdentityPort.class);
                when(account.resolve("subject")).thenReturn(new Subject("subject", 4L));
                LiveIssuerSessionFilter filter = new LiveIssuerSessionFilter(account);
                MockHttpServletRequest current = authorizationRequest();
                filter.doFilter(current, new MockHttpServletResponse(), new MockFilterChain());
                assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(principal);

                when(account.resolve("subject")).thenReturn(new Subject("subject", 5L));
                MockHttpServletRequest stale = authorizationRequest();
                MockHttpSession staleSession = (MockHttpSession) stale.getSession(false);
                filter.doFilter(stale, new MockHttpServletResponse(), new MockFilterChain());
                assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
                assertThat(staleSession.isInvalid()).isTrue();
            } finally {
                SecurityContextHolder.clearContext();
                connection.destroy();
            }
        }
    }

    private static RedisIndexedSessionRepository repository(LettuceConnectionFactory connection) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connection);
        template.afterPropertiesSet();
        RedisIndexedSessionRepository repository = new RedisIndexedSessionRepository(template);
        repository.setRedisKeyNamespace("grab:issuer:sessions");
        return repository;
    }

    private static MockHttpServletRequest authorizationRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorize");
        request.setServletPath("/oauth2/authorize");
        request.setSession(new MockHttpSession());
        return request;
    }
}
