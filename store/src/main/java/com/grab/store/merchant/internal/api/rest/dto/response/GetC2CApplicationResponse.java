package com.grab.store.merchant.internal.api.rest.dto.response;

public record GetC2CApplicationResponse(
        String merchantId,
        String applicantUserId,
        String type,
        String status,
        boolean completedContactInfo,
        boolean completedBasicInfo
) {

}
