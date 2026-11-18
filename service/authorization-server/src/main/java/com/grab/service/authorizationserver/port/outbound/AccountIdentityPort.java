package com.grab.service.authorizationserver.port.outbound;

import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.AuthenticationState;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.VerifiedUser;

public interface AccountIdentityPort {
    VerifiedUser verify(String email, String password);

    AuthenticationState resolve(String userId, String assignmentId, boolean selectSingleContext);

    class AccountUnavailableException extends RuntimeException {
        public AccountUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
