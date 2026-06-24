# Feature: Identity Cookie Generation

## 1. Objective

Enhance the security of the Backend-For-Frontend (BFF) architecture by transitioning from returning JWT tokens solely in the JSON response body to securely delivering them via HTTP-only cookies. This mitigates Cross-Site Scripting (XSS) risks and ensures secure, stateless session management for frontend applications.

---

## 2. Scope

### In scope

- Generate `HttpOnly`, `Secure`, and `SameSite` restricted cookies for both `accessToken` and `refreshToken`.
- Automatically attach generated cookies to the HTTP response headers upon successful user login, registration, and token refresh.
- Refactor the `/refresh` and `/logout` endpoints to seamlessly read the refresh token from incoming cookies via Spring's `@CookieValue`, while maintaining a graceful fallback to `@RequestBody` payloads for backward compatibility.
- Clear authentication cookies on logout by setting their `Max-Age` to 0.
- Extract cookie generation logic into a dedicated, testable `AuthCookieHelper` component to uphold the Single Responsibility Principle (SOLID).
- Expose cookie configurations (Secure flag, SameSite policy, path, and max age) to Spring's environment properties under the `security.api.cookie.*` namespace.
- Define local development overrides in `application-dev.yml`, `dev.env`, and `docker-compose.yml` to allow local cross-port development (e.g., `Secure=false`, `SameSite=Lax`).
- Provide comprehensive unit test coverage for both the `AuthController` and the `AuthCookieHelper`.

### Out of scope

- Removing token fields from the existing `AuthResponse` JSON payload (tokens are currently retained in the JSON body alongside the cookies to ensure existing clients do not break).
- Changes to the underlying JWT issuance, validation, or signature logic within the `AuthCommandService`.

---

## 3. Implementation Details

### Configuration Properties

The following configurable properties have been introduced to dictate cookie behavior dynamically per environment:

| Property | Default Value | Description |
| :--- | :--- | :--- |
| `security.api.cookie.secure` | `true` | Ensures cookies are only transmitted over HTTPS (overridden to `false` in `dev`). |
| `security.api.cookie.same-site` | `Strict` | Enforces the SameSite policy to prevent CSRF attacks (overridden to `Lax` in `dev`). |
| `security.api.cookie.refresh-token-max-age-days` | `7` | Time-to-live for the refresh token cookie. |
| `security.api.cookie.refresh-token-path` | `/api/v1/identity/auth/refresh` | Restricts the path where the refresh token cookie is sent to minimize exposure. |

### Component Overview

- **`AuthCookieHelper`**: A new Spring `@Component` responsible for creating and clearing `ResponseCookie` objects based on the injected configuration properties.
- **`AuthController`**: Updated to inject the `AuthCookieHelper`. Uses its methods to append the `Set-Cookie` headers to the `ResponseEntity`. Uses `@CookieValue` to seamlessly extract the token for refresh and logout operations.

---

## 4. Testing

- **`AuthCookieHelperTest`**: Validates the correct application of configuration properties and the accurate construction of `Set-Cookie` headers.
- **`AuthControllerTest`**: Uses `@WebMvcTest` and `MockMvc` to verify the presence of `Set-Cookie` headers in endpoint responses and the correct extraction of `@CookieValue` inputs during refresh and logout scenarios.
