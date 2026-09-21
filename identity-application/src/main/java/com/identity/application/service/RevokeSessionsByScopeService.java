package com.identity.application.service;

import com.identity.application.model.write.RevokeSessionsByScopeCommand;
import com.identity.application.port.inbound.RevokeSessionsByScopeUseCase;
import com.identity.domain.port.outbound.SessionStore;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RevokeSessionsByScopeService implements RevokeSessionsByScopeUseCase {

    private final SessionStore sessionStore;

    @Override
    public Void execute(RevokeSessionsByScopeCommand command) {
        sessionStore.revokeByScope(
                command.platformCode(),
                command.scopeKey(),
                command.scopeId()
        );
        return null;
    }
}
