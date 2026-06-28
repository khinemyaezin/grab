package com.merchant.infrastructure.exception;

import com.grab.framework.exception.DomainException;

public class MerchantInfraException extends DomainException {
    public MerchantInfraException(MerchantInfraError error, String message, Throwable cause) {
        super(error, message, cause);
    }
}
