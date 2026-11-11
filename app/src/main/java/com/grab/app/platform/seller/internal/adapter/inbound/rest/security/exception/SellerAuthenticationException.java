package com.grab.app.platform.seller.internal.adapter.inbound.rest.security.exception;

import com.grab.framework.exception.MessageSource;
import org.springframework.security.core.AuthenticationException;

public class SellerAuthenticationException extends AuthenticationException {

    private final MessageSource messageSource;

    public SellerAuthenticationException(SellerSecurityError error, String defaultMessage) {
        super(defaultMessage);
        this.messageSource = error;
    }

    public SellerAuthenticationException(SellerSecurityError error, String defaultMessage, Throwable cause) {
        super(defaultMessage, cause);
        this.messageSource = error;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }
}

