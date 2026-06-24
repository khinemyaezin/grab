# ADR 013: MFE Communication and Authentication

**Status:** Proposed  
**Date:** 2026-06-21

---

## 1. The Problem

**What's not working?**  
Storing JSON Web Tokens (JWTs) in `localStorage` or `sessionStorage` exposes them to client-side JavaScript. Additionally, independent Micro Frontends (MFEs) currently lack a secure, standardized pattern for handling shared global concerns like authentication without tightly coupling to one another.

**What's at stake?**  
If we continue exposing raw JWTs to the client, an attacker injecting malicious code into any MFE could easily extract the tokens via a Cross-Site Scripting (XSS) attack, leading to complete account compromise. Furthermore, explicitly passing tokens between the Shell and multiple MFEs significantly increases the surface area for accidental token leakage.

---

## 2. What We Decided

**The core approach:**  
Use shell orchestration, framework-neutral platform contracts, event-driven communication, and BFF-managed `HttpOnly` cookie authentication.

**Key changes:**
- The **Shell** manages routing, layout, authentication state, and global concerns.
- **Independent MFEs** (e.g., Product, Inventory) focus purely on business capabilities.
- A **framework-neutral Platform API** handles navigation, session information, and typed cross-MFE events.
- A **BFF/Gateway** manages an `HttpOnly` cookie for authentication instead of exposing raw JWTs to the frontend.
- **Web Components or mount/unmount adapters** are used for non-React MFEs.

**What stays the same:**  
Backend APIs remain the absolute source of truth for business data.

---

## 3. Why This Approach

**Primary reasons:**
1. **XSS Mitigation:** The `HttpOnly` flag prevents client-side scripts from accessing the cookie (`document.cookie`), completely protecting the token from theft even if an MFE has an XSS vulnerability.
2. **Simplified MFE Architecture:** The browser natively attaches the authentication cookie to outgoing API requests. Individual MFEs no longer need complex logic to store, retrieve, or manually inject tokens into `Authorization` headers.
3. **Centralized Security Perimeter:** The Backend-For-Frontend (BFF) strictly dictates the cookie lifecycle (`Secure`, `SameSite=Strict`, `max-age`), cleanly decoupling the frontend from sensitive credential handling.

---

## 4. Trade-offs

| Pros | Cons |
|-------|-------|
| Eliminates token theft vulnerabilities via XSS. | Requires strict CORS (`withCredentials: true`) configuration across the platform. |
| Radically simplifies frontend architecture and state management by removing token passing. | Frontend cannot decode the JWT payload to read user data. |
| Centralizes security management at the BFF layer. | Shell must rely on a dedicated network request (e.g., `/api/v1/identity/auth/me`) to determine authentication state. |

---

## 5. What Needs to Change

**New components/modules to build:**
- The Shell must implement logic to fetch user profile details and current authentication state from a dedicated BFF endpoint (e.g., `/me`).
- BFF endpoints must be refactored to set and clear `HttpOnly`, `Secure`, and `SameSite` cookies upon login, refresh, and logout.

**Changes to existing systems:**
- Frontend HTTP clients (like `fetch` or `axios`) must be updated globally to include credentials (`withCredentials: true`) so cookies are transmitted.

**Team impact:**
- Frontend developers will no longer handle raw JWTs or authorization headers and must instead rely on the Shell's provided authentication state context.

---

## 6. Migration Plan

- **Phase 1:** Implement cookie generation, parsing, and clearing on the Backend/BFF. Ensure endpoints gracefully fall back to JSON body tokens if cookies are missing.
- **Phase 2:** Update the Shell and MFE HTTP clients to send credentials. Implement a `/me` endpoint for the Shell to load initial authentication state securely.
- **Phase 3:** Fully deprecate the return of raw JWTs in JSON bodies and remove any token-passing mechanisms between the Shell and MFEs.

**Rollback strategy:**  
Retain tokens in the JSON response payload alongside the new cookies during Phase 1 and Phase 2. If cookie delivery fails due to unforeseen CORS or domain issues, frontend clients can temporarily revert to manually extracting the tokens from the JSON body and appending `Authorization` headers.

---

## 7. Related Documents

- [FAT-0007_identity-cookie-generation.md](../../features/FAT-0007_identity-cookie-generation.md)
