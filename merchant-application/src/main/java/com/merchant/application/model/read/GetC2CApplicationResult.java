package com.merchant.application.model.read;

public record GetC2CApplicationResult(
        String merchantId,
        String applicantUserId,
        String type,
        String status,
        boolean completedContactInfo,
        boolean completedBasicInfo
) {
}
