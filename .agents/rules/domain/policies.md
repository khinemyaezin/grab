# R14. Policies

Load when encoding a business rule or deciding where a rule belongs.

Business rules are controlled by policies, not by handlers, services, or controllers.

## Domain policies

Path: `{name}-domain/.../policy/`

- Framework-agnostic pure domain decision rules: authorization of domain actions, placement, registration eligibility, delegation, approval criteria, and similar.
- Operate on domain types (aggregates, value objects, ids/codes).
- No Spring, no HTTP, no DTOs, no repositories.
- Examples: `RoleDelegationPolicy`, `AccessPlacementPolicy`, `MerchantApprovalPolicy`.

## Application policies

Path: `store/.../{module}/internal/policy/`

- Application/use-case rules that need application context: security scope, actor/session context, cross-cutting access checks against already-loaded aggregates.
- May use Spring (`@Component`) and application-layer types.
- MUST NOT inject repositories. Receive needed data from the calling handler or other already-resolved inputs.
- Examples: `InventoryLocationAccessPolicy`, `MerchantApprovalAccessPolicy`.

## Usage

- Handlers invoke policies, then apply aggregate mutations / persistence.
- Prefer domain policies when the rule is intrinsic to the bounded context.
- Use application policies when the rule depends on app, security, or orchestration context.
