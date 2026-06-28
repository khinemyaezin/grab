package com.grab.store.merchant.internal.command;

import com.merchant.domain.aggregate.MerchantAccount;

import java.time.Instant;

public record MerchantAccountResult(
        String merchantId,
        String applicantUserId,
        String type,
        String legalName,
        String displayName,
        RegistrationResult registration,
        ContactResult contact,
        AddressResult registeredAddress,
        String status,
        String lifecycleReason,
        String reviewedBy,
        Instant reviewedAt,
        Instant createdAt,
        Instant updatedAt,
        long version
) {

    public static MerchantAccountResult from(MerchantAccount merchant) {
        return new MerchantAccountResult(
                merchant.getId().getValue(),
                merchant.getApplicantUserId().getValue(),
                merchant.getType().name(),
                merchant.getName().legalName(),
                merchant.getName().displayName(),
                merchant.getRegistration() == null ? null : new RegistrationResult(
                        merchant.getRegistration().countryCode(), merchant.getRegistration().registrationNumber()),
                merchant.getContact() == null ? null : new ContactResult(
                        merchant.getContact().email(), merchant.getContact().phone()),
                merchant.getRegisteredAddress() == null ? null : new AddressResult(
                        merchant.getRegisteredAddress().line1(), merchant.getRegisteredAddress().line2(),
                        merchant.getRegisteredAddress().city(), merchant.getRegisteredAddress().region(),
                        merchant.getRegisteredAddress().postalCode(), merchant.getRegisteredAddress().countryCode()),
                merchant.getStatus().name(),
                merchant.getLifecycleReason() == null ? null : merchant.getLifecycleReason().value(),
                merchant.getReviewedBy() == null ? null : merchant.getReviewedBy().getValue(),
                merchant.getReviewedAt(), merchant.getCreatedAt(), merchant.getUpdatedAt(), merchant.getVersion()
        );
    }


    public record RegistrationResult(String countryCode, String registrationNumber) {
    }

    public record ContactResult(String email, String phone) {
    }

    public record AddressResult(
            String line1, String line2, String city, String region, String postalCode, String countryCode
    ) {
    }
}
