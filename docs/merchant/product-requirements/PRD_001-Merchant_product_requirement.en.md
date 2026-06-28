# Product Requirements Document: Merchant Module

## 1. Summary

This PRD defines the first Merchant module release for the commerce platform.
It allows an authenticated user to apply to become a merchant, complete a
business profile, submit it for review, and follow the application through
approval or rejection. It also allows authorized platform staff to review and
manage an approved merchant's lifecycle.

The Merchant module is the source of truth for the merchant business record
and its operational status. A `MerchantAccount` is not a user account, login
platform, storefront, shop location, or warehouse. Creating a merchant does
not automatically create any of those resources.

## 2. Problem

Identity can confirm who a user is and what access they have, but a role alone
cannot represent a business that sells through the platform. The system needs
an authoritative merchant record with its own profile, review decision, and
lifecycle.

Without this module:

- seller onboarding would be mixed into user registration;
- the platform could not distinguish an applicant from an approved merchant;
- business registration details could be duplicated;
- reviewers would have no consistent application queue or decision history;
- other modules could disagree about whether a merchant is allowed to operate;
- creating a seller account might accidentally create a storefront or physical
  location before real business information is available; and
- one user could potentially view or change another user's merchant
  application.

## 3. Users

- **Merchant applicant:** An authenticated customer who wants to establish a
  merchant business on the platform.
- **Merchant owner:** An applicant whose Merchant Account has been approved.
- **Merchant reviewer:** Authorized platform staff who review submitted
  applications and request corrections, approve, or reject them.
- **Merchant operator:** Authorized platform staff who can suspend, reactivate,
  or close an approved merchant.
- **Support and compliance staff:** Authorized staff who need to view merchant
  names, application status, lifecycle reasons, and review history.

## 4. Goals

- Keep merchant onboarding separate from authentication and user registration.
- Give every merchant business a stable Merchant ID and clear lifecycle.
- Let applicants safely create, update, submit, and view only their own
  applications.
- Give authorized reviewers a consistent review queue and controlled decision
  actions.
- Prevent incomplete, duplicate, or invalid merchant applications from being
  submitted or approved.
- Keep customer access independent from merchant approval or suspension.
- Provide reliable lifecycle events so Identity and future commerce modules can
  react without copying Merchant-owned business data.

## 5. In Scope

- Explicit creation of a draft `MerchantAccount` by an authenticated user.
- Merchant types: `RETAILER`, `THIRD_PARTY`, and `CONSUMER`.
- Business profile information:
  - legal and display names;
  - professional business registration, when applicable;
  - contact email and phone number; and
  - registered address.
- Applicant profile updates and application submission.
- Reviewer requests for changes, approval, and rejection.
- Merchant suspension, reactivation, and permanent closure.
- Applicant, reviewer, operator, reason, and timestamp information needed for
  lifecycle history.
- Applicant views for their applications and approved merchants.
- Merchant detail view containing recognizable names and current status.
- Reviewer queue filtering by Merchant status.
- Merchant lifecycle events delivered through a transactional outbox.
- Merchant-scoped applicant and owner access integration with Identity.
- Validation, ownership enforcement, optimistic concurrency, and RFC 7807
  error responses.
- Additive release control through the `merchant.enabled` feature flag.

## 6. Out of Scope

- Automatic or explicit Storefront creation and Storefront lifecycle.
- Physical shop, warehouse, pickup point, or fulfillment-location creation.
- Product, catalog, inventory, order, payment, settlement, or shipping
  behavior.
- User registration, login, password, token, role, invitation, and session
  ownership; these belong to Identity.
- A separate Onboarding orchestration module.
- Merchant staff management and staff invitations.
- Tax-document upload, document verification, risk scoring, and external
  business-registry verification.
- Merchant branding, public shop pages, storefront slugs, and custom domains.
- Automatically creating a default address, platform, Storefront, or location
  when an application starts or is approved.

## 7. Main Features

### 7.1 Explicit Merchant Application

An authenticated user starts merchant onboarding as a deliberate action. The
system creates a draft Merchant Account associated with that applicant.

It must:

- derive the applicant identity from the authenticated session;
- require the applicant to select a supported Merchant type and provide a
  display name;
- create the Merchant Account in `DRAFT` status;
- return the Merchant ID, display name, type, and status;
- prevent another nonterminal Merchant Account for the same applicant and
  Merchant type; and
- create no Storefront, platform, address, or fulfillment location.

### 7.2 Profile Completion and Submission

The applicant completes the information required for review and explicitly
submits the application when ready.

It must:

- allow only the original applicant to update or submit the application;
- allow profile changes while the application is `DRAFT` or
  `CHANGES_REQUESTED`;
- require legal and display names, valid contact information, and a registered
  address before submission;
- require a business registration for `RETAILER` and `THIRD_PARTY` merchants;
- allow business registration to remain optional for a `CONSUMER` merchant;
- reject a supplied business registration already used by another merchant;
- move a complete application to `PENDING_REVIEW`; and
- return understandable validation or lifecycle errors when submission is not
  allowed.

### 7.3 Merchant Review

Authorized reviewers evaluate submitted applications using a queue that shows
recognizable merchant information and lifecycle status.

It must:

- allow filtering the review queue by Merchant status;
- allow review decisions only for a `PENDING_REVIEW` application;
- allow a reviewer to request changes with a clear reason;
- return an application to `CHANGES_REQUESTED` when corrections are needed;
- allow a corrected application to be resubmitted;
- activate the Merchant Account when approved;
- permanently reject the application with a recorded reason; and
- derive the reviewer identity from the authenticated session rather than a
  request field.

### 7.4 Operational Merchant Lifecycle

Authorized platform operators control whether an approved merchant may
operate.

It must:

- treat only an `ACTIVE` merchant as operational;
- suspend an `ACTIVE` merchant with a recorded reason;
- reactivate a `SUSPENDED` merchant;
- close an `ACTIVE` or `SUSPENDED` merchant with a recorded reason;
- treat `REJECTED` and `CLOSED` as terminal statuses; and
- continue to deny Merchant operations while the Merchant is inactive, even
  if a downstream access update has not yet completed.

### 7.5 Merchant Access Integration

Merchant lifecycle changes inform Identity which Merchant-scoped access should
be available, while Merchant remains responsible for business status.

It must:

- request Merchant Applicant access when an application starts;
- replace applicant access with Merchant Owner access after approval;
- remove applicant access after rejection;
- suspend Merchant-scoped access and associated sessions after suspension;
- restore only access suspended by the Merchant lifecycle after reactivation;
- permanently revoke Merchant-scoped access and associated sessions after
  closure; and
- preserve the user's unrelated Customer App access throughout the Merchant
  lifecycle.

### 7.6 Merchant Visibility and Status

Applicants and authorized staff need a clear view of a Merchant Account without
using technical IDs as the only label.

It must:

- show the Merchant ID together with legal name, display name, type, and
  lifecycle status;
- allow an applicant to list their own applications and merchants;
- allow an applicant or authorized reviewer to view Merchant details;
- deny cross-applicant access without revealing protected Merchant data; and
- show the latest lifecycle reason and reviewer information when applicable.

### 7.7 Reliable Lifecycle Notifications

Other modules receive Merchant lifecycle changes without being given sensitive
business profile data.

It must publish:

- `MerchantApplicationStarted`;
- `MerchantApplicationSubmitted`;
- `MerchantChangesRequested`;
- `MerchantApproved`;
- `MerchantRejected`;
- `MerchantSuspended`;
- `MerchantReactivated`; and
- `MerchantClosed`.

Each event must contain an event ID, Merchant ID, applicant ID, status, actor
ID, aggregate version, and timestamp. It must not contain complete legal,
registration, contact, tax, or address data.

## 8. Business Rules

- Starting Merchant onboarding is an explicit action and is never a side
  effect of user registration or login.
- One applicant may have at most one nonterminal Merchant Account for each
  Merchant type.
- Every supplied business registration is normalized and must be unique.
- `RETAILER` and `THIRD_PARTY` applications require business registration
  before submission; `CONSUMER` registration is optional.
- Only the original applicant may update or submit their application.
- Only authorized reviewers and operators may perform administrative lifecycle
  actions.
- Caller-provided applicant, reviewer, or operator IDs are never accepted as
  proof of identity or authorization.
- Valid transitions are:
  - `DRAFT` to `PENDING_REVIEW`;
  - `PENDING_REVIEW` to `CHANGES_REQUESTED`, `ACTIVE`, or `REJECTED`;
  - `CHANGES_REQUESTED` to `PENDING_REVIEW`;
  - `ACTIVE` to `SUSPENDED` or `CLOSED`; and
  - `SUSPENDED` to `ACTIVE` or `CLOSED`.
- `REJECTED` and `CLOSED` cannot be reopened.
- Only `ACTIVE` Merchant Accounts may operate commerce resources.
- Merchant rejection, suspension, or closure must not suspend or remove the
  user's unrelated customer identity or access.
- Review changes, rejection, suspension, and closure require a reason.
- The Merchant module remains authoritative during event-delivery delays; an
  inactive Merchant fails closed.
- Event consumers must tolerate repeated delivery without applying the same
  lifecycle change twice.

## 9. Success Criteria

The module is successful when:

- an authenticated user can start, update, submit, and view their own Merchant
  application;
- automated tests cover every allowed and forbidden lifecycle transition;
- incomplete professional applications cannot be submitted;
- Consumer applications can be submitted without business registration when
  all other required information is complete;
- duplicate nonterminal applications and duplicate registrations are rejected;
- Merchant A applicants cannot read or modify Merchant B applications;
- unauthorized users cannot perform reviewer or operator actions;
- approval changes applicant access to owner access without affecting customer
  access;
- suspension, reactivation, rejection, and closure produce the required access
  and session outcomes;
- repeated event delivery does not duplicate Identity access changes;
- Merchant persistence and lifecycle events commit atomically; and
- no Storefront, platform, default address, or fulfillment location is created
  automatically.

## 10. Technical Considerations & Constraints

- Merchant is a separate bounded context with `merchant-domain`,
  `merchant-infrastructure`, and Store application/API composition packages.
- Merchant uses a dedicated datasource, transaction manager, Flyway migration
  location, and transactional outbox.
- Merchant stores Identity user IDs as external strings and creates no
  cross-database foreign keys.
- Merchant records use optimistic versioning to detect conflicting updates.
- Database constraints support normalized registration uniqueness and one
  nonterminal Merchant Account per applicant and Merchant type.
- Hibernate validates the Flyway-managed schema rather than creating it.
- Applicant, reviewer, and operator identities are derived from
  `SecurityPrincipal`.
- APIs use CQRS command/query handlers and the existing RFC 7807 error contract.
- Lifecycle events are delivered at least once; consumers are responsible for
  inbox-based idempotency.
- Sensitive profile information stays in Merchant persistence and is excluded
  from tokens, Identity assignments, and lifecycle events.
- The module is introduced additively behind `merchant.enabled`.

## 11. Dependencies

- **Identity module:** Authenticated users, trusted principals,
  Merchant-scoped access assignments, and session revocation.
- **Framework module:** IDs, aggregate roots, CQRS buses, domain errors, and
  shared security contracts.
- **Outbox infrastructure:** Durable, at-least-once publication of Merchant
  lifecycle events.
- **PostgreSQL and Flyway:** Merchant-owned persistence and schema migration.
- **Store application:** REST delivery, security configuration, transaction
  composition, and RFC 7807 error handling.

Catalog, Inventory, Storefront, and the future Onboarding module are not
dependencies of this first Merchant release.

## 12. Related Documents

- [ADR-001: Merchant Bounded Context Architecture](../architecture/ADR_001-Merchant_module_architecture.md)
- [ADR-002: Merchant Domain Aggregate Design](../architecture/ADR_002-Merchant_bounded_context_architecture.md)
- [Identity PRD-002: Platform-Scoped Identity Access](../../identity/product-requirements/PRD-002_Platform-scope.en.md)
- [Identity ADR-003: Platform-Scoped Identity Access](../../identity/architecture/ADR_003-Platform_scopes_architecture.md)
- [System ADR-001: Current System Architecture](../../system/ADR-001-system-architecture.md)
- [System ADR-002: Module-Scoped Transactional Outbox](../../system/ADR-002-module-scoped-outbox.md)
- [PRD Writing Guideline](../../PRD_SKILLS.md)
