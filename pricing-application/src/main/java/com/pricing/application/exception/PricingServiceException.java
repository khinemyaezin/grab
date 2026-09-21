package com.pricing.application.exception;

import com.grab.framework.exception.DomainException;

public class PricingServiceException extends DomainException {
    public PricingServiceException(PricingServiceError error, String message) {
        super(error, message);
    }
}
