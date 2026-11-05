package com.grab.app.account.merchant.port.message;

import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Universal Java Port Interface for Merchant Account lifecycle and Onboarding applications.
 * Replaces MerchantController, MerchantAdminController, and Retailer/C2C Application controllers.
 */
public interface MerchantAccountPort {

    MerchantMessageResult startApplication(StartMerchantApplicationMessage message);

    MerchantMessageResult submitApplication(Id merchantId, Id applicantUserId);

    MerchantMessageResult approveMerchant(Id merchantId, Id actorId);

    MerchantMessageResult rejectMerchant(Id merchantId, Id actorId, String reason);

    MerchantMessageResult requestChanges(Id merchantId, Id actorId, String comments);

    MerchantMessageResult suspendMerchant(Id merchantId, Id actorId, String reason);

    MerchantMessageResult reactivateMerchant(Id merchantId, Id actorId);

    MerchantMessageResult closeMerchant(Id merchantId, Id actorId);

    MerchantMessageResult updateProfile(UpdateMerchantProfileMessage message);

    Optional<MerchantMessageResult> findMerchantById(Id merchantId, Id actorId, boolean reviewerAccess, boolean scopedAccess);

    List<MerchantMessageResult> listMyMerchants(Id applicantUserId);

    List<MerchantMessageResult> listReviewQueue(String status);

    record StartMerchantApplicationMessage(
            Id applicantUserId,
            String businessType, // FIRST_PARTY, THIRD_PARTY, C2C
            String displayName,
            String primaryMarketCountryCode
    ) {}

    record UpdateMerchantProfileMessage(
            Id merchantId,
            Id applicantUserId,
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

    record MerchantMessageResult(
            String merchantId,
            String businessType,
            String status,
            String legalName,
            String displayName,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
