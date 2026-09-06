package com.grab.store.shared.security.expection;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ProblemDetailSecurityHandlersTest {

    private ProblemDetailAccessDeniedHandler accessDeniedHandler;
    private ProblemDetailAuthEntryPoint authEntryPoint;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        accessDeniedHandler = new ProblemDetailAccessDeniedHandler(objectMapper);
        authEntryPoint = new ProblemDetailAuthEntryPoint(objectMapper);
    }

    @Test
    void accessDeniedHandler_whenResponseCommitted_shouldNotWrite() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/events/stream");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setCommitted(true);

        assertThatCode(() -> accessDeniedHandler.handle(
                request,
                response,
                new AccessDeniedException("Access Denied")
        )).doesNotThrowAnyException();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsByteArray()).isEmpty();
    }

    @Test
    void accessDeniedHandler_whenResponseOpen_shouldWriteForbidden() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/catalog");
        MockHttpServletResponse response = new MockHttpServletResponse();

        accessDeniedHandler.handle(request, response, new AccessDeniedException("Access Denied"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("idt.service.auth.access_denied");
    }

    @Test
    void authEntryPoint_whenResponseCommitted_shouldNotWrite() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/events/stream");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setCommitted(true);

        assertThatCode(() -> authEntryPoint.commence(
                request,
                response,
                new AuthenticationCredentialsNotFoundException("Authentication required")
        )).doesNotThrowAnyException();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsByteArray()).isEmpty();
    }

    @Test
    void authEntryPoint_whenResponseOpen_shouldWriteUnauthorized() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/catalog");
        MockHttpServletResponse response = new MockHttpServletResponse();

        authEntryPoint.commence(
                request,
                response,
                new AuthenticationCredentialsNotFoundException("Authentication required")
        );

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Authentication required");
    }
}
