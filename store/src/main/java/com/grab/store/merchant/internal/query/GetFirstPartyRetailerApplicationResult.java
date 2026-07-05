package com.grab.store.merchant.internal.query;

public record GetFirstPartyRetailerApplicationResult(
        String merchantId,
        String applicantUserId,
        String type,
        String status,
        boolean completedContactInfo,
        boolean completedBasicInfo,
        boolean completedBusinessRegistration
) {
}
