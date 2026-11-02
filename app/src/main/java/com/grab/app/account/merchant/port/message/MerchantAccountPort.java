package com.grab.app.account.merchant.port.message;

import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Universal Java Port Interface for Merchant Account lifecycle and Onboarding applications.
 * Unifies MerchantController, MerchantAdminController, and Retailer/C2C Application controllers.
 */
public interface MerchantAccountPort {

    MerchantResult submitApplication(SubmitMerchantApplicationMessage message);

    void approveMerchant(Id merchantId);

    void rejectMerchant(Id merchantId, String reason);

    void requestChanges(Id merchantId, String comments);

    void suspendMerchant(Id merchantId, String reason);

    void reactivateMerchant(Id merchantId);

    void closeMerchant(Id merchantId);

    void updateProfile(Id merchantId, UpdateMerchantProfileMessage message);

    Optional<MerchantResult> findMerchantById(Id merchantId);

    List<MerchantResult> listMerchants(MerchantSearchCriteria criteria);

    record SubmitMerchantApplicationMessage(
            Id ownerUserId,
            String businessName,
            String businessType, // FIRST_PARTY, THIRD_PARTY, C2C
            String taxNumber,
            String contactEmail,
            String contactPhone,
            String countryCode
    ) {}

    record UpdateMerchantProfileMessage(
            String businessName,
            String contactEmail,
            String contactPhone,
            String logoUrl
    ) {}

    record MerchantSearchCriteria(
            String status,
            String businessType,
            String query,
            int page,
            int size
    ) {}

    record MerchantResult(
            Id id,
            Id ownerUserId,
            String businessName,
            String businessType,
            String status,
            String contactEmail,
            String contactPhone,
            String countryCode,
            Instant createdAt
    ) {}
}

