package com.grab.app.platform.seller.port.outbound;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Seller Outbound Port for Merchant onboarding, storefronts, and team members.
 */
public interface SellerMerchantOutboundPort {

    MerchantOutboundResult startApplication(StartApplicationRecord record);

    MerchantOutboundResult submitApplication(String applicationId, String actorId);

    Optional<MerchantOutboundResult> findMerchantById(String merchantId, String actorId);

    List<MerchantOutboundResult> listMyMerchants(String actorId);

    MerchantOutboundResult updateProfile(UpdateProfileRecord record);

    StorefrontOutboundResult createStorefront(CreateStorefrontRecord record);

    Optional<StorefrontOutboundResult> findStorefrontById(String merchantId, String storefrontId);

    List<StorefrontOutboundResult> listStorefronts(String merchantId);

    StorefrontOutboundResult updateStorefront(UpdateStorefrontRecord record);

    void activateStorefront(String merchantId, String storefrontId);

    void closeStorefront(String merchantId, String storefrontId);

    MerchantMemberOutboundResult inviteMember(InviteMemberRecord record);

    MerchantMemberOutboundResult acceptInvitation(String merchantId, String memberId, String actorId);

    MerchantMemberOutboundResult updateMemberRole(UpdateMemberRoleRecord record);

    void removeMember(String merchantId, String memberId, String actorId);

    List<MerchantMemberOutboundResult> listMembers(String merchantId);

    record StartApplicationRecord(
            String actorId,
            String businessType,
            String displayName,
            String primaryMarketCountryCode
    ) {}

    record UpdateProfileRecord(
            String merchantId,
            String actorId,
            String legalName,
            String displayName,
            String registrationCountryCode,
            String registrationNumber,
            String contactEmail,
            String contactPhone,
            String addressLine1,
            String addressLine2,
            String addressCity,
            String addressRegion,
            String addressPostalCode,
            String addressCountryCode
    ) {}

    record CreateStorefrontRecord(
            String merchantId,
            String name,
            String slug
    ) {}

    record UpdateStorefrontRecord(
            String merchantId,
            String storefrontId,
            String name,
            String slug
    ) {}

    record InviteMemberRecord(
            String merchantId,
            String actorId,
            String targetUserId,
            String role,
            Instant expiresAt
    ) {}

    record UpdateMemberRoleRecord(
            String merchantId,
            String memberId,
            String actorId,
            String newRole
    ) {}

    record MerchantOutboundResult(
            String merchantId,
            String businessType,
            String status,
            String legalName,
            String displayName,
            Instant createdAt,
            Instant updatedAt
    ) {}

    record StorefrontOutboundResult(
            String storefrontId,
            String merchantId,
            String name,
            String slug,
            String status,
            String lifecycleReason,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {}

    record MerchantMemberOutboundResult(
            String memberId,
            String merchantId,
            String userId,
            String role,
            String status,
            String invitedBy,
            Instant invitationExpiresAt,
            Instant joinedAt,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {}
}
