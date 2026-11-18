package com.grab.service.authorizationserver.port.outbound;

public interface AccountIdentityPort {
    Subject verify(String email, String password);

    Subject resolve(String userId);

    record Subject(String userId, long authenticationVersion) {}

    class AccountUnavailableException extends RuntimeException {
        public AccountUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
