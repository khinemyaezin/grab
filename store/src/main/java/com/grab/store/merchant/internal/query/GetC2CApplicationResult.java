package com.grab.store.merchant.internal.query;

public record GetC2CApplicationResult(
        String merchantId,
        String applicantUserId,
        String type,
        String status,
        boolean completedContactInfo,
        boolean completedBasicInfo
) {
}
