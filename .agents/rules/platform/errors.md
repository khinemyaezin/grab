# R15. Errors

Load when adding module errors, exception types, or API error responses.

## MUST

- Module-specific `sealed interface {Module}ServiceError extends MessageSource` with error record subtypes.
- Each error record has `kind()` -> `ErrorCategory`, `code()` -> i18n key, `args()`.
- Error code: `{module-prefix}.{layer}.{entity}.{error_type}`, for example `inv.service.location.not_found`.
- Module exception extends `DomainException`, caught by `GlobalApiExceptionHandler` (`@RestControllerAdvice`).
- Error responses: RFC 7807 `ProblemDetail` with custom fields `code`, `args`, `traceId`, `module`, `retryable`, `retryAfterMs`.
