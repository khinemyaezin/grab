package com.grab.store.merchant.internal.exception;

import com.grab.framework.exception.DomainException;

public class MerchantServiceException extends DomainException {
    public MerchantServiceException(MerchantServiceError error, String message) {
        super(error, message);
    }
}
