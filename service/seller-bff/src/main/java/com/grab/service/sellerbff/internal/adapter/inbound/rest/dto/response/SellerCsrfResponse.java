package com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response;

public record SellerCsrfResponse(String headerName, String parameterName, String token) {}
