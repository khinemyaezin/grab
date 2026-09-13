# Product Requirements Document: Platform-Scoped Identity Access

## 1. Summary

This PRD defines the platform-scoped identity access feature. It allows one user account to safely operate across the Customer App, Seller Portal, and Admin Console, with permissions limited to the selected platform and the specific merchant, storefront, or fulfillment location being managed. The core principle is that a user signs in once, but access is always evaluated for the application they are using and the business context they selected.

## 2. Problem

A global role such as `SELLER` cannot describe where or for which business a person may work. It can accidentally grant access to the wrong merchant and can incorrectly block a person's customer access when one merchant relationship is suspended.

## 3. Users

- Customers who also operate or work for a merchant.
- Merchant applicants and owners.
- Merchant administrators and storefront staff.
- Catalog, inventory, and order-management staff.
- Platform administrators and merchant reviewers.

## 4. Goals

- Enable one email account to use all permitted platforms without duplicate user registration.
- Ensure a user signs in once, but access is always evaluated for the application they are using and the business context they selected.
- Provide secure context selection and explicit context switching for multi-merchant users.

## 5. In Scope

- Registered platform definitions and platform-specific roles.
- Merchant-, storefront-, and fulfillment-location-scoped access assignments.
- Seller Portal context selection and context switching.
- Context-bound login, session refresh, logout, and access revocation.
- Merchant applicant and owner access created from Merchant lifecycle events.
- Email invitations for merchant and storefront staff.
- Administrative visibility and management of scoped access.
- Audit records for access grants, invitation actions, context changes, and access denials.
- Migration from global customer, seller, and admin role assignments.

## 6. Out of Scope

- Merchant legal details, approval rules, tax data, addresses, or storefront customization; these belong to the Merchant module.
- Product, inventory, order, and payment business behavior.
- Creating a physical fulfillment location without a real address.
- Social login, MFA, email verification, and password recovery.
- A general-purpose permission editor for end users.
- Allowing clients to define new platforms or resource-scope types.

## 7. Main Features

### 7.1 One Account Across Multiple Platforms

A customer who also works for a merchant uses the same account across the Customer App and Seller Portal so that they do not need duplicate credentials.

It must:
- Maintain one identity record for one unique email address.
- Allow the same valid credentials to authenticate the user for every platform on which they have access.
- Load access only for the platform being used after authentication.
- Deny access to platforms where the user has no assignment, showing a clear access-unavailable message.
- Prevent Admin Console access from being obtained through customer or seller self-registration.

### 7.2 Secure Context Selection

A user who manages several businesses selects the merchant or storefront they are working on so that they do not accidentally change the wrong business.

It must:
- Deny management access if the user has no active Seller Portal assignment.
- Group active assignments by platform and resource scope; roles within the same scope must not create separate contexts.
- Combine all effective roles and authorities in the selected scope without asking the user to choose a role.
- Automatically select the context if exactly one active context is available, displaying it prominently.
- Require selection from the user if more than one active context exists, using recognizable names (not technical IDs as primary labels).
- Verify that the selected context belongs to an active access assignment for the current user and platform.
- Maintain only one active context within a Seller Portal session at a time, preserved on refresh.

### 7.3 Explicit Context Switching

A multi-merchant user intentionally switches businesses so that every subsequent action applies to the correct merchant.

It must:
- Provide a clearly labeled context switcher requiring selection from verified active assignments.
- Create a new active session context and stop using the previous context for new requests.
- Warn the user before switching when the current screen has unsaved changes.
- Reload merchant-specific data instead of retaining information from the previous context.
- Record the context switch with user, previous context, new context, session, and timestamp in audit logs.

### 7.4 Merchant Applicant and Owner Access

A customer starting a seller business completes merchant onboarding without losing normal customer access.

It must:
- Create or reuse an active user identity without duplicating it.
- Grant Merchant Applicant access limited to the newly created merchant account for onboarding.
- Replace applicant access with Merchant Owner access upon merchant approval.
- Preserve Customer App access during merchant rejection, requested changes, or suspension.

### 7.5 Scoped Staff Invitations

A merchant owner or authorized administrator invites staff to a specific business or storefront so that they receive only necessary access.

It must:
- Allow authorized inviters to send email invitations with selected access profiles and permitted scopes.
- Restrict inviters from granting roles or scopes broader than their own authority.
- Issue single-use acceptance links with expiration times and clear statuses.
- Attach access to existing accounts or require registration before attaching.
- Reject reuse, expired, or cancelled invitations.

### 7.6 Scoped Access Administration

An authorized merchant or platform administrator reviews and removes access assignments so that former staff cannot continue managing data.

It must:
- Allow authorized users to view active, suspended, expired, and revoked access assignments.
- Permit granting, suspending, reactivating, revoking, or setting an expiry for allowed access.
- Stop revoked or suspended access from contributing permissions immediately on the next protected request.
- Prevent users from approving or broadening their own assignment.

### 7.7 Resource Isolation

The platform prevents other merchants from accessing private business data.

It must:
- Apply merchant-level access only to resources verified as belonging to that merchant.
- Restrict storefront-level access from granting access to sibling storefronts.
- Verify both the user's permission and the requested resource's ownership for Catalog, Inventory, Merchant, and Order operations.
- Return a forbidden response on scope mismatch without revealing protected resource details.

### 7.8 Context-Bound Sessions

A session remains tied to the selected business so refreshing does not silently move the user.

It must:
- Identify the platform and active context in a protected session.
- Preserve the same platform and scope upon session refresh and re-resolve the currently effective combined roles.
- Revoke the current refresh session upon logging out.
- Removing one assignment must remove that role immediately without invalidating the context when another effective assignment remains in the same scope.
- Reject or revoke the scoped session when no effective assignment remains for its platform and scope, without affecting unrelated contexts.

## 8. Business Rules

- A valid identity assignment does not override an inactive merchant, storefront, or location; the owning module still enforces business status.
- Platform and scope values are verified by the server; client-provided values are requests to select a context, not trusted authorization facts.
- A context is distinct by `platformCode + scopeKey + scopeId`; every effective assignment in that context contributes its role and authorities.
- Users select business scopes such as a merchant or storefront. They never select which of their effective roles to activate.
- Provider roles or token claims cannot directly grant an unverified merchant, storefront, or fulfillment-location scope.
- Existing RFC 7807 error responses remain the standard for unauthorized and forbidden requests.
- Access checks must not materially degrade the platform's authentication response-time objective.
- Caller-provided actor or ownership identifiers are treated only as resource input, not proof of identity or authorization.
- The system never silently falls back to a different merchant if the selected context becomes unavailable.

## 9. Success Criteria

The module is successful when:
- Customer, Seller Portal, and Admin Console roles resolve only on their configured platforms.
- Automated tests prove that a Merchant A user cannot read or mutate Merchant B resources.
- Merchant-, storefront-, and fulfillment-location-scoped denial tests pass for Catalog, Inventory, Merchant, and available Order operations.
- Login, refresh, context switch, logout, assignment revocation, and session expiry preserve context behavior.
- Automated tests prove that roles in one scope are combined while assignments in different scopes remain separate selectable contexts.
- Existing Customer and Admin assignments are reconciled successfully.
- Protected APIs no longer rely on `X-Actor-Id`, caller-selected `sellerId`, or equivalent values for authorization.
- Confirmed cross-merchant data exposure incidents is `0`.
- Seller Portal protected requests with a verified context is `100%`.
- Accepted staff invitations that create a duplicate merchant or storefront is `0`.
- Merchant suspensions that disable unrelated Customer App access is `0`.

## 10. Technical Considerations & Constraints

- **Source of Truth:** Identity is the source of truth for users, platforms, roles, access assignments, invitations, and session context. Merchant is the source of truth for Merchant Account and Storefront. Inventory is the source of truth for Fulfillment Location.
- **Provider Compatibility:** Access resolution must continue to work with local authentication and future OAuth2/OIDC providers.
- **Tracking Events:** Must emit events for key actions including `platform_access_resolved`, `platform_access_denied`, `context_selected`, `context_switch_succeeded`, `scope_access_denied`, `access_assignment_granted`, `access_invitation_sent`, and `access_invitation_accepted`.
- **Migration constraints:** Legacy and new assignments may be read in parallel, but the system must never combine them to broaden access. Old roles are removed only after assignment counts and ownerships reconcile.
- **Platform Support:** Version 1 supports Customer App, Seller Portal, and Admin Console web/mobile clients via the shared Identity APIs.

## 11. Dependencies

- The Merchant module for merchant and storefront existence, ownership, and lifecycle.
- The Inventory module for fulfillment location ownership and lifecycle.
- Framework primitives and shared Identity APIs for multi-platform delivery.

## 12. Related Documents

- [Business Requirements: Platform-Scoped Identity](../business-requirements/BRD-002_platform-scopes.en.md)
- [ADR-003: Platform-Scoped Identity Access](../architecture/ADR_003-Platform_scopes_architecture.md)
- [Platform-Scope Domain Aggregates](../architecture/DGR_003-Platform_scopes_bounded_context_architecture.md)
