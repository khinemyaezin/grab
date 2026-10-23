# Publishable Keys for Sales Channels
## Sales Channel · Storefront Query · Security

### Problem Statement

Merchants need a simple way to tell a public storefront which sales channel to use when requesting products. A publishable key gives the storefront a channel-specific value it can include in customer requests, without exposing a merchant login or administrator credential. Merchants can create a replacement key before revoking an old one, so they can rotate storefront configuration without taking the channel offline.

A publishable key identifies a channel; it does not sign a user in or grant permission to manage products, prices, channels, or keys.

### Acceptance Criteria

- **AC-01 — Channel scope:** A key MUST belong to exactly one existing sales channel. A channel MAY have multiple active keys at the same time, each with a merchant-provided label of at most 100 characters, so credentials can be rotated safely.
- **AC-02 — Safe creation:** The system MUST generate an unpredictable key with a recognizable `pk_` prefix. The successful create response MUST return the full key once; the system MUST store only a one-way digest and non-secret display prefix. It MUST NOT return the full key from list or lookup operations, write it to logs, or cache the create response.
- **AC-03 — Key management API:** `POST /api/v1/sales-channels/channels/{salesChannelId}/publishable-keys` MUST create a key and return `201`; `GET` on the same path MUST return a paged list of key metadata and return `200`; `DELETE /api/v1/sales-channels/channels/{salesChannelId}/publishable-keys/{keyId}` MUST revoke the key and return `204`. List results MUST include the key ID, channel ID, label, display prefix, creation time, and revocation time when revoked.
- **AC-04 — Management access:** A merchant MUST be able to list keys with key-read authority and create or revoke keys with key-write authority only for that merchant’s seller-owned channels. A platform administrator with key-admin authority MUST be able to manage keys for any channel. Merchants MUST NOT manage keys for another merchant’s channel or for a platform-owned channel.
- **AC-05 — Revocation and rotation:** A key MUST remain active until revoked. Revocation MUST take effect for subsequent requests and MUST be safe to repeat. A merchant MUST be able to use a replacement key before revoking the previous key.
- **AC-06 — Public channel selection:** A public Storefront Query request MUST be able to use the `X-Publishable-Key` header instead of a `salesChannelId` to select its channel. The system MUST resolve a valid active key to its associated channel and then apply the existing channel status, publication, pricing, and inventory rules. If both the header and `salesChannelId` are supplied, they MUST refer to the same channel or the request MUST be rejected.
- **AC-07 — Invalid and disabled channels:** A malformed, unknown, or revoked key MUST NOT resolve to a channel, and an error MUST NOT reveal whether a matching key exists. A key for a disabled channel MUST NOT make that channel’s storefront content available.
- **AC-08 — Limited authority:** A publishable key MUST authorize only public storefront reads for its associated channel. It MUST NOT authenticate a merchant or administrator, authorize write or key-management operations, or grant access to another channel.

### Related Specifications

#### Related Feature Specifications
- [DiscoveryProduct](feat_002-discovery_product.md)

#### Domain Specifications
- [Publishable Key domain spec](../domain/sales-channel/architecture/publishable-key-domain-spec.md)
- [Sales Channel domain spec](../domain/sales-channel/architecture/sales-channel-domain-spec.md)

#### Technical Specifications
- [Storefront Query tech spec](../system/storefront-query-tech-spec.md)
- [API Security & Authentication tech spec](../system/api-security-tech-spec.md)
- [Security manifest consistency tech spec](../system/security-manifest-consistency-tech-spec.md)
