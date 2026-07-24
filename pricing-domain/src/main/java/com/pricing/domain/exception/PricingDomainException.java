package com.pricing.domain.exception;

import com.grab.framework.exception.DomainException;

public class PricingDomainException extends DomainException {
    public PricingDomainException(PricingDomainError error, String message) {
        super(error, message);
    }
}
