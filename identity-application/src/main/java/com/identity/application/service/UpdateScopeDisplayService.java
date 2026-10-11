package com.identity.application.service;

import com.grab.framework.id.Id;
import com.identity.application.model.write.UpdateScopeDisplayCommand;
import com.identity.application.port.inbound.UpdateScopeDisplayUseCase;
import com.identity.application.port.outbound.ScopeDisplayViewRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateScopeDisplayService implements UpdateScopeDisplayUseCase {
    private final ScopeDisplayViewRepository displays;

    @Override
    public void execute(UpdateScopeDisplayCommand command) {
        Id scopeId = command.scopeId();
        String name = command.name();
        displays.update(scopeId, name);
    }
}
