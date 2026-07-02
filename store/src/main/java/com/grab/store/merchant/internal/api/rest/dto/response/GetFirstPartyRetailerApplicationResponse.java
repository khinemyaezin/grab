package com.grab.store.merchant.internal.api.rest.dto.response;

public record GetFirstPartyRetailerApplicationResponse(
        String merchantId,
        String applicantUserId,
        String type,
        String status,
        boolean completedContactInfo,
        boolean completedBasicInfo,
        boolean completedBusinessRegistration
) {

}
