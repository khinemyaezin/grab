# Account identity management and replaceable OAuth issuer

**Implementation plan.** Account owns the stable platform user ID and all application role bindings, including merchant and storefront assignments. The selected issuer authenticates the user and issues access tokens for resource servers. Deployments can use the bundled Spring Authorization Server or trust an external OIDC provider directly.

## 1. Ownership and deployment choices

| Deployment | Sign-in and access tokens | User management | Authorization |
| --- | --- | --- | --- |
| Spring + local Account (default) | Spring signs users in through Account’s private credential verifier and issues tokens. | Account handles registration, profiles, credentials, and status. | Account checks platform status and scoped role assignments. |
| External OIDC + IAM sync adapter | The external issuer handles sign-in and issues tokens directly. | IAM administrators manage profiles and credentials. Account serves a synchronized copy of profile and provider status. | Account checks platform status, effective assignments, and freshness of synchronized provider status. |
| External OIDC + IAM proxy adapter | The external issuer handles sign-in and issues tokens directly. | Account reads profiles and provider status on demand. The IAM owns profile and credential changes. | Account combines live provider status with local platform controls and bindings. |

In every mode, the selected issuer owns client registrations, signing keys, grants, and issuer sessions. Seller BFF owns its browser sessions and keeps OAuth tokens in Redis. Resource servers validate tokens for their audience and enforce endpoint permissions using the actor resolved by Account.

The external modes must operate with the Spring issuer stopped. For example, a deployment can select an external OIDC provider. Directory reads and synchronization use the Account `ExternalIdentityProviderPort`; a provider adapter owns its protocol, paging model, credentials, and resource-to-subject mapping. Account does not depend on a particular IAM protocol.

## 2. Current implementation and required changes

| Repository finding | Implementation consequence |
| --- | --- |
| Account persists users, roles, scoped assignments, and unique external issuer/subject links. Its external lookup can derive roles from IAM entitlements when a context is absent. [Identity lookup adapter](../identity-adapter-persistence/src/main/java/com/identity/adapter/persistence/adapter/IdentityLookupQueryAdapter.java) | Reuse platform IDs and bindings. Resolve access in the new flows solely from effective Account assignments. |
| The user aggregate requires an email and its local creation path requires a password. The identity schema requires a unique email. [User aggregate](../identity-domain/src/main/java/com/identity/domain/aggregate/User.java) · [Identity schema](../service/account/account-service/src/main/resources/db/migration/identity/V1__initial_schema.sql) | Add an external identity lifecycle while preserving local registration constraints. |
| The issuer already uses private Account operations for credential verification and active-subject resolution. [Issuer Account adapter](../service/authorization-server/src/main/java/com/grab/service/authorizationserver/adapter/outbound/rpc/RmiAccountIdentityAdapter.java) | Keep these live checks. Add optional subject replication separately. |
| Seller BFF previously coupled context selection to a private issuer grant and custom introspection fields. | Track issuer subject separately from platform ID, resolve actors through Account, validate assignments through Account, and store the selected assignment in the server-side session. |
| Two bearer entry points validate with locally configured keys. [App authenticator](../app/src/main/java/com/grab/app/platform/seller/internal/adapter/inbound/rest/security/authenticator/LocalJwtAccessTokenAuthenticator.java) · [Store authenticator](../store/src/main/java/com/grab/store/shared/security/LocalJwtAccessTokenAuthenticator.java) | Migrate and test the active bearer consumers individually. Passing a Seller BFF browser test does not exercise these paths. |
| Account currently dispatches outbox events in process. The repository also provides a dispatcher with broker acknowledgement. [Account dispatcher configuration](../service/account/account-service/src/main/java/com/grab/service/account/internal/config/ServiceSharedConfiguration.java) · [Redpanda dispatcher](../redpanda-adapter/src/main/java/com/grab/redpanda/adapter/dispatcher/RedpandaOutboxEventDispatcher.java) | Route the new issuer projection events through durable cross-process delivery with a restricted topic. |

## 3. Phase 1 — Establish Account identities and access contracts

1. **Migrate existing identities.** Add explicit local or external management to Account’s identity model. Backfill existing records as local. Preserve platform IDs, access assignments, invitations, and authentication versions. Enforce the existing email and credential requirements for local registration. Allow a proxy user to hold assignments before Account has a local profile copy.
2. **Link by stable identifiers.** Store the trusted OIDC issuer and subject, IAM provider and resource identifiers, and platform user ID as distinct values. Validate the provider adapter’s subject mapping against the trusted issuer before enabling external sign-in. Require an explicit, authorized link for existing users. Email collisions enter reconciliation and cannot automatically link two accounts.
3. **Separate writes and reads.** Add idempotent private Account commands to provision an external identity and link it to an existing platform user. New users start with zero assignments. Enforce uniqueness transactionally so concurrent requests cannot bind the same external identity to different users. Record who established or changed a link.
4. **Resolve application access.** Add a private Account query with a verified issuer, subject, and optional assignment ID. For the bundled issuer, resolve its established platform subject. For an external issuer, resolve its registered link. Return the platform ID, effective context, current roles and authorities, and invalidation revisions. Derive scope from the assignment and check ownership, status, expiry, and scope lifecycle.
5. **Keep platform suspension independent.** Effective access requires an active platform user and an enabled external identity where applicable. Preserve user authentication versions and merchant/storefront lifecycle revisions. A directory import cannot clear an administrator’s platform suspension.
6. **Preserve service boundaries.** Publish additive Account message contracts with serializable records. Use private authenticated transport, separate Account command and query handlers, application read projections, and Account-scoped transactions. Keep Account headless. Cross-domain lifecycle reads use published ports.

**Phase gate:** An external subject that differs from its platform ID resolves to the intended user. Granting or revoking a scoped role through Account changes access on the next authorization check. Roles supplied only by the IAM confer no application permission.

## 4. Phase 2 — Implement provider sync and proxy modes

| Operation | Sync mode | Proxy mode |
| --- | --- | --- |
| Find or list user profiles | Read the last imported Account profile projection. | Read the configured IAM through its provider adapter with bounded timeouts and adapter-owned pagination. Listing users is read-only. |
| Prepare a user for a role binding | Use the provisioned platform identity or explicitly link an existing user. | Resolve the IAM resource and run an explicit provisioning or linking command for its local identity. |
| Update external email or provider status | Apply the change through the Account import command and advance the recorded provider revision or observation. | Read the current value from the IAM when needed. |
| Suspend platform access or change a role | Apply the Account command immediately. | Apply the same Account command immediately. |
| Directory unavailable | Serve synchronized authorization data within the configured maximum age. Return an availability error once freshness expires. | Return an availability error when current directory status cannot be confirmed. |

Implement the read-only `ExternalIdentityProviderPort` and provider-specific outbound adapters as deployable plugins. Configure the selected adapter, protected service credentials, subject mapping, request timeout, sync schedule, and maximum authorization-data age per external deployment. Keep IAM network calls outside Account database write transactions.

In sync mode, retrieve provider pages, apply idempotent writes, and checkpoint only committed progress. Apply an explicit disabled status when observed. Treat a missing user as a removal candidate only after a successful full reconciliation and confirmation by provider resource ID. A partial or failed scan must not mass-disable accounts or advance the full-reconciliation freshness timestamp.

In proxy mode, retain the local identity link, platform controls, and role bindings. Administrative reads and authorization must use the selected directory mode consistently. Direct users to the configured IAM for external registration, profile changes, and credential recovery. Disable local password authentication for externally managed records, including linked records that previously had a local password.

**Phase gate:** Verify rename, disablement, deletion, duplicate email, directory outage, and interrupted pagination. Restarting the sync must preserve user IDs and bindings. Proxy mode must support both profile reads and role administration without importing the full directory.

## 5. Phase 3 — Make browser and resource-server authentication portable

1. **Select one trusted issuer per deployment.** Keep the bundled Spring issuer as the local default. Use authorization code with PKCE for browser clients. Configure the expected issuer, API audience, credentials, callback, logout destination, and trusted discovery metadata. Preserve explicit private backchannel endpoint overrides. [Bundled issuer deployment](../service/authorization-server/README.md)
2. **Validate API credentials at the perimeter.** Validate the access token’s issuer, audience, signature, and lifetime for the supported JWT profile. Enforce access-token purpose and reject an ID token presented to an API. Document the required token profile for external providers. Resolve the verified subject through Account before applying business permissions.
3. **Bind the request to its target.** For each scoped bearer route, derive the target from its resource or validated assignment input and check that it matches the actor’s effective Account scope. A browser session’s selected assignment applies only to that session. A separate bearer request must independently establish its target.
4. **Preserve browser behavior.** Store verified issuer, external subject, platform ID, selected assignment, and invalidation revisions in the server-side session. Select the sole eligible context automatically. With zero or multiple eligible contexts, allow an authenticated session with no scoped permissions. Preserve the secure cookie, CSRF protection, and server-side token storage. [Seller session contract](spec/system/seller-oidc-tech-spec.md)
5. **Move context switching to the BFF.** Keep the existing session, context-list, and selection URLs. Validate the target using Account, then atomically update the session using the existing Redis locking mechanism. Serialize selection with refresh and logout. A rejected target preserves the previous valid selection.
6. **Maintain live invalidation.** Recheck current user and assignment eligibility on protected requests. Preserve revision checks so suspension followed by reactivation cannot revive an old seller session. Preserve token revocation checks where the existing client requires live issuer state. Support advertised provider endpoints for refresh, revocation, and logout according to the deployment’s configured capabilities. Local logout clears the browser session even when the provider is unavailable.
7. **Remove provider-specific compatibility paths.** Seller context selection is now an Account lookup held in the BFF session, and actor resolution no longer consumes Grab-specific introspection fields. Remove deprecated Account token operations after verifying their callers have migrated. Migrate each deployed bearer consumer independently.

| Outcome | HTTP behavior |
| --- | --- |
| Missing, expired, invalid, or revoked credentials or invalidated session | Return 401 and require authentication. |
| Authenticated actor lacks the requested assignment or permission | Return 403. |
| An operation requires seller context and none has been selected | Preserve the existing 409 response. |
| Required identity, issuer, or directory state cannot be verified due to an outage | Return 503 for the affected operation. |

**Phase gate:** With the bundled issuer stopped, complete external sign-in, refresh, seller context selection, scoped browser access, direct bearer API access, and logout. Run the corresponding journeys again with Spring as issuer.

## 6. Phase 4 — Add optional Account-to-issuer subject synchronization

This is a separate, one-way flow from Account to the bundled issuer. Directory synchronization in Phase 2 flows from the external IAM into Account. Enable and monitor the two flows independently.

1. Publish a versioned subject-change contract from the Account event-contract module. Carry the platform user ID, monotonic subject revision, lifecycle state, and profile fields approved for issuer claims. Keep passwords, password hashes, tokens, and role assignments out of the payload.
2. Write each subject change and its identity outbox record in the same transaction. Route only the intended event types to the restricted issuer topic. Reuse the Redpanda transport and retain Account’s existing in-process event consumers.
3. Provide a private, paginated Account snapshot carrying the same per-subject revisions. Start durable event consumption before the snapshot begins. Apply a snapshot row or event only when its revision exceeds the stored revision. Persist deletion revisions so delayed events cannot recreate removed records.
4. Store the projection in the issuer’s schema. Acknowledge delivery after its transaction commits. Use projected metadata only for profile claims permitted by the client’s scopes. Keep credential verification and security-sensitive eligibility checks live through Account.
5. Leave projection sync disabled by default. Support rebuild by replaying the snapshot and concurrent changes. Monitor consumer lag and the last successful snapshot separately from live issuer health.

**Phase gate:** Snapshot and replay converge while Account is changing. Duplicate and out-of-order events preserve the latest state. Projection lag cannot extend access, and disabling the consumer leaves the live Spring deployment functional.

## 7. Acceptance tests and release sequence

| ID | Phase | Evidence required before release |
| --- | --- | --- |
| AC-01 | 1 | PostgreSQL Testcontainers migration preserves legacy user IDs, bindings, invitations, and login behavior. Concurrent linking resolves once or reports a conflict. |
| AC-02 | 1–2 | Local and external IDs differ in fixtures. Duplicate email never automatically links users. Directory enablement cannot override platform suspension. |
| AC-03 | 2 | Provider adapter contract tests cover paging, rate limits, rename, disablement, confirmed deletion, partial scans, restart, and stale authorization data. Proxy tests verify live reads and failure handling. |
| AC-04 | 3 | Exercise all three deployment modes. Test an independent OIDC implementation with Spring stopped, key rotation, wrong issuer or audience, expired tokens, and ID-token rejection. |
| AC-05 | 3 | Verify zero, one, and multiple contexts, cross-user assignment rejection, concurrent switch and logout, immediate binding revocation, and suspend/reactivate behavior. Verify browser routes, HAL responses, cookies, and CSRF. |
| AC-06 | 3 | Exercise scoped endpoints in each deployed bearer runtime. A valid token for another audience or a role from token claims alone cannot authorize an operation. |
| AC-07 | 4 | Broker and database integration tests cover restart, duplicate delivery, replay, deletion, overlapping snapshot and live updates, and the disabled projection configuration. |
| AC-08 | 1–4 | Message-contract compatibility checks and ArchUnit tests cover headless Account, CQRS isolation, persistence boundaries, and group transaction ownership. |

**Deploy in dependency order.**

1. Deploy additive Account schema and private contracts. Run compatibility checks against existing consumers and rehearse migration on pre-change identity data.
2. Configure directory access and reconcile external links. Review ambiguous matches, disabled identities, and expected role bindings before allowing external sign-in.
3. Deploy provider-neutral resource-server and BFF adapters with local Spring still selected. Exercise existing sessions and bearer clients, then pilot the external issuer with a separate session namespace and fresh sign-in.
4. Monitor unresolved subjects, directory failures and freshness, Account lookup latency, context-selection failures, and issuer validation failures. Roll back the issuer independently by restoring its previous trust and client configuration and requiring fresh login. Preserve Account IDs, links, and bindings.
5. Enable the optional projection after the snapshot and replay tests pass. Observe outbox backlog and issuer consumer lag. If replication fails, disable its use for claims and continue the live Account flow. Retire the legacy grant and token paths after verifying that their consumers have migrated.
