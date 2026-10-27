package com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.response;

public record GetC2CApplicationResponse(
        String merchantId,
        String applicantUserId,
        String type,
        String status,
        boolean completedContactInfo,
        boolean completedBasicInfo
) {

}
