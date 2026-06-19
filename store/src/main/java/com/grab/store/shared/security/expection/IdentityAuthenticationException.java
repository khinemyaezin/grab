package com.grab.store.shared.security.expection;

import com.grab.framework.exception.MessageSource;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
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
}
