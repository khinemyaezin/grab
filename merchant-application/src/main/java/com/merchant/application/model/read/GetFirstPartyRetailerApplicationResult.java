package com.merchant.application.model.read;

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
