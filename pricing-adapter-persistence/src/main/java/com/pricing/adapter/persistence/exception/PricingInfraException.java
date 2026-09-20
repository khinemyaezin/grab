package com.pricing.adapter.persistence.exception;

import com.grab.framework.exception.DomainException;

public class PricingInfraException extends DomainException {
    public PricingInfraException(PricingInfraError error, String message, Throwable cause) {
        super(error, message, cause);
    }
}
