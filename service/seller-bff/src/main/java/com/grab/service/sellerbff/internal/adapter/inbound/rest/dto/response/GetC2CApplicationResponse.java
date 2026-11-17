package com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response;

public record GetC2CApplicationResponse(
        String merchantId,
        String applicantUserId,
        String type,
        String status,
        boolean completedContactInfo,
        boolean completedBasicInfo
) {}

