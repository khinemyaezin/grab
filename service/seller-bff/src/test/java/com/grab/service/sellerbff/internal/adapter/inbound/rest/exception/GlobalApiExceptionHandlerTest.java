package com.grab.service.sellerbff.internal.adapter.inbound.rest.exception;

import com.grab.framework.exception.DomainException;
import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageResolver;
import com.grab.framework.exception.MessageSource;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalApiExceptionHandlerTest {

    @Mock
    private ObjectProvider<MessageResolver> resolverProvider;

    @Mock
    private HttpServletRequest request;

    private GlobalApiExceptionHandler handler;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/api/seller/test");
        when(resolverProvider.getIfAvailable(any())).thenReturn((code, args, locale) -> code);
        handler = new GlobalApiExceptionHandler(resolverProvider);
    }

    @Test
    void handleDomainException_clientError_returnsProblemDetailWithCategoryStatus() {
        MDC.put("traceId", "test-trace-123");
        try {
            DomainException domainException = new TestDomainException(
                    new TestMessageSource(ErrorCategory.NOT_FOUND, "test.not_found"),
                    "Entity was not found"
            );

            ProblemDetail detail = handler.handleDomainException(domainException, request);

            assertThat(detail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
            assertThat(detail.getProperties()).containsEntry("code", "test.not_found");
            assertThat(detail.getProperties()).containsEntry("traceId", "test-trace-123");
        } finally {
            MDC.clear();
        }
    }

    @Test
    void handleDomainException_internalError_returnsInternalServerError() {
        DomainException domainException = new TestDomainException(
                new TestMessageSource(ErrorCategory.INTERNAL, "test.internal_err"),
                "Database crash"
        );

        ProblemDetail detail = handler.handleDomainException(domainException, request);

        assertThat(detail.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(detail.getProperties()).containsEntry("code", "test.internal_err");
    }

    @Test
    void handleRemoteException_unwrapsNestedDomainException() {
        DomainException nestedDomainException = new TestDomainException(
                new TestMessageSource(ErrorCategory.CONFLICT, "cat.product.sku_conflict"),
                "SKU already exists"
        );
        ServerException serverException = new ServerException("Server error", nestedDomainException);
        RemoteException remoteException = new RemoteException("RMI failed", serverException);

        ProblemDetail detail = handler.handleRemoteException(remoteException, request);

        assertThat(detail.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(detail.getProperties()).containsEntry("code", "cat.product.sku_conflict");
    }

    @Test
    void handleRemoteException_withoutDomainException_returnsInternalServerError() {
        RemoteException remoteException = new RemoteException("Connection refused");

        ProblemDetail detail = handler.handleRemoteException(remoteException, request);

        assertThat(detail.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(detail.getProperties()).containsEntry("code", "shr.internal.unexpected");
    }

    @Test
    void handleUnexpectedException_unwrapsNestedDomainException() {
        DomainException nestedDomainException = new TestDomainException(
                new TestMessageSource(ErrorCategory.BAD_REQUEST, "cat.product.invalid_state"),
                "Invalid state"
        );
        RuntimeException runtimeException = new RuntimeException("Wrapped", nestedDomainException);

        ProblemDetail detail = handler.handleUnexpectedException(runtimeException, request);

        assertThat(detail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(detail.getProperties()).containsEntry("code", "cat.product.invalid_state");
    }

    @Test
    void handleAccessDenied_returnsForbidden() {
        AccessDeniedException exception = new AccessDeniedException("Access is denied");

        ProblemDetail detail = handler.handleAccessDenied(exception, request);

        assertThat(detail.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(detail.getProperties()).containsEntry("code", "idt.service.auth.access_denied");
    }

    private static class TestDomainException extends DomainException {
        TestDomainException(MessageSource messageSource, String defaultMessage) {
            super(messageSource, defaultMessage);
        }
    }

    private record TestMessageSource(ErrorCategory kind, String code) implements MessageSource {
        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }
}
