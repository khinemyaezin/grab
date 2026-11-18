package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.exception;

import org.springframework.security.access.AccessDeniedException;

public final class SellerContextRequiredException extends AccessDeniedException {
    public SellerContextRequiredException() {
        super("Select an access context to continue");
    }
}
