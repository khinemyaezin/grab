package com.grab.app.account.identity.internal.adapter.outbound.security.error;

import com.grab.framework.exception.MessageSource;
import org.springframework.security.core.AuthenticationException;

public class IdentityAuthenticationException extends AuthenticationException {

    private final MessageSource messageSource;

    public IdentityAuthenticationException(IdentitySecurityError error, String defaultMessage) {
        super(defaultMessage);
        this.messageSource = error;
    }

    public IdentityAuthenticationException(IdentitySecurityError error, String defaultMessage, Throwable cause) {
        super(defaultMessage, cause);
        this.messageSource = error;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }
}

