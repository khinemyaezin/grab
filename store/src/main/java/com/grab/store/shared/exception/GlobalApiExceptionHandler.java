package com.grab.store.shared.exception;

import com.grab.framework.exception.DomainException;
import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageResolver;
import com.grab.framework.exception.MessageSource;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.command.handler.CreateProductSetCommandHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class GlobalApiExceptionHandler {
    private static final Logger log = Loggers.getLogger(GlobalApiExceptionHandler.class);

    private static final String TRACE_ID_KEY = "traceId";

    private final MessageResolver messageResolver;

    public GlobalApiExceptionHandler(ObjectProvider<MessageResolver> messageResolverProvider) {
        this.messageResolver = messageResolverProvider.getIfAvailable(DefaultMessageResolver::new);
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomainException(DomainException exception, HttpServletRequest request) {
        log.error(exception.getMessage(), exception);
        return toProblem(
                statusFromCategory(exception.getMessageSource().kind()),
                exception.getMessageSource(),
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        log.error(exception.getMessage(), exception);
        List<Map<String, Object>> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldError)
                .toList();

        return handleDomainException(SharedErrors.requestValidation(errors), request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException exception, HttpServletRequest request) {
        log.error(exception.getMessage(), exception);
        List<Map<String, Object>> errors = exception.getConstraintViolations()
                .stream()
                .map(violation -> Map.<String, Object>of(
                        "path", String.valueOf(violation.getPropertyPath()),
                        "message", violation.getMessage(),
                        "invalidValue", violation.getInvalidValue()
                ))
                .toList();

        return handleDomainException(SharedErrors.requestValidation(errors), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMalformedBody(HttpMessageNotReadableException exception, HttpServletRequest request) {
        log.error(exception.getMessage(), exception);
        exception.getMostSpecificCause();
        String reason = Objects.toString(
                exception.getMostSpecificCause().getMessage(),
                "unknown"
        );
        return handleDomainException(SharedErrors.malformedJson(reason), request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(Exception exception, HttpServletRequest request) {
        log.error(exception.getMessage(), exception);
        return handleDomainException(SharedErrors.internalUnexpected(), request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleDomainException(MissingServletRequestParameterException exception, HttpServletRequest request) {
        log.error(exception.getMessage(), exception);
        return toProblem(
                statusFromCategory(ErrorCategory.BAD_REQUEST),
                new SharedError.RequestMalformedJson("Bad Request"),
                exception.getMessage(),
                request
        );
    }


    private ProblemDetail toProblem(
            HttpStatus status,
            MessageSource messageSource,
            String fallbackMessage,
            HttpServletRequest request
    ) {
        String locale = request.getLocale() == null ? "en" : request.getLocale().toLanguageTag();
        String detail = messageResolver.resolve(messageSource.code(), messageSource.args(), locale);

        if (detail == null || detail.equals(messageSource.code())) {
            detail = fallbackMessage;
        }

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setProperty("code", messageSource.code());
        problemDetail.setProperty("args", messageSource.args());
        problemDetail.setProperty("traceId", resolveTraceId());
        problemDetail.setProperty("path", request.getRequestURI());
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("module", inferModule(messageSource.code()));
        problemDetail.setProperty("retryable", isRetryable(messageSource));
        Long retryAfterMs = retryAfterMs(messageSource);
        if (retryAfterMs != null) {
            problemDetail.setProperty("retryAfterMs", retryAfterMs);
        }

        return problemDetail;
    }

    private HttpStatus statusFromCategory(ErrorCategory category) {
        return switch (category) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case BUSINESS_RULE -> HttpStatus.UNPROCESSABLE_ENTITY;
            case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            case INTERNAL -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private Map<String, Object> toFieldError(FieldError fieldError) {
        return Map.of(
                "field", fieldError.getField(),
                "message", Objects.toString(fieldError.getDefaultMessage(), "invalid"),
                "rejectedValue", Objects.toString(fieldError.getRejectedValue(), "")
        );
    }

    private String resolveTraceId() {
        String traceId = MDC.get(TRACE_ID_KEY);
        if (traceId != null && !traceId.isBlank()) {
            return traceId;
        }

        String b3TraceId = MDC.get("X-B3-TraceId");
        if (b3TraceId != null && !b3TraceId.isBlank()) {
            return b3TraceId;
        }

        String traceIdAlt = MDC.get("trace_id");
        if (traceIdAlt != null && !traceIdAlt.isBlank()) {
            return traceIdAlt;
        }

        return null;
    }

    private String inferModule(String code) {
        if (code == null || code.isBlank()) {
            return "shared";
        }
        if (code.startsWith("inv.")) {
            return "inventory";
        }
        if (code.startsWith("cat.")) {
            return "catalog";
        }
        return "shared";
    }

    private boolean isRetryable(MessageSource messageSource) {
        String code = messageSource.code();
        if ("inv.infra.persistence.internal".equals(code)) {
            return true;
        }
        return "shr.internal.unexpected".equals(code);
    }

    private Long retryAfterMs(MessageSource messageSource) {
        if ("inv.infra.persistence.internal".equals(messageSource.code())) {
            return 2_000L;
        }
        return null;
    }

    private static final class DefaultMessageResolver implements MessageResolver {
        @Override
        public String resolve(String code, Map<String, Object> args, String locale) {
            if (code == null) {
                return null;
            }
            if (args == null || args.isEmpty()) {
                return code;
            }
            String resolved = code;
            for (Map.Entry<String, Object> entry : args.entrySet()) {
                resolved = resolved.replace(
                        "{" + entry.getKey() + "}",
                        String.valueOf(entry.getValue())
                );
            }
            return resolved;
        }
    }
}
