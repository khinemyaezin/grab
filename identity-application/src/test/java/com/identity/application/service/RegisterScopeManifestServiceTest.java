package com.identity.application.service;

import com.grab.framework.security.ScopeDeclaration;
import com.identity.application.model.write.RegisterScopeManifestCommand;
import com.identity.domain.port.outbound.ScopeManifestRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterScopeManifestServiceTest {
    @Test
    void execute_rejectedPersistence_doesNotFallBackToStaticState() {
        ScopeManifestRepository repository = (owner, revision, declarations) -> false;
        var service = new RegisterScopeManifestService(repository);
        var declarations = List.of(new ScopeDeclaration("test.account", null));
        var command = new RegisterScopeManifestCommand("test", 1, declarations);
        assertThatThrownBy(() -> service.execute(command)).isInstanceOf(IllegalStateException.class);
    }
}
