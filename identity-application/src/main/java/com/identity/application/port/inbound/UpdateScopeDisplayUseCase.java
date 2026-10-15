package com.identity.application.port.inbound;

import com.identity.application.model.write.UpdateScopeDisplayCommand;

public interface UpdateScopeDisplayUseCase {
    void execute(UpdateScopeDisplayCommand command);
}
