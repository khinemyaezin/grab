package com.merchant.domain.exception;

import com.grab.framework.exception.DomainException;

public class MerchantDomainException extends DomainException {
    public MerchantDomainException(MerchantDomainError error, String message) {
        super(error, message);
    }
}
