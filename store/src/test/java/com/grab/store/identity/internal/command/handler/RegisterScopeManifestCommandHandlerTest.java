package com.grab.store.identity.internal.command.handler;

import com.grab.store.identity.internal.config.IdentityTransactional;
import com.identity.application.model.write.RegisterScopeManifestCommand;
import com.identity.application.port.inbound.RegisterScopeManifestUseCase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterScopeManifestCommandHandlerTest {
    @Test
    void delegatesCommandAndDeclaresWriteTransactionBoundary() throws NoSuchMethodException {
        var useCase = new RecordingUseCase();
        var handler = new RegisterScopeManifestCommandHandler(useCase);
        var command = new RegisterScopeManifestCommand("inventory", 1, List.of());

        assertThat(handler.handle(command)).isNull();
        assertThat(handler.getCommandType()).isEqualTo(RegisterScopeManifestCommand.class);
        assertThat(useCase.executedCommand).isSameAs(command);
        assertThat(RegisterScopeManifestCommandHandler.class
                .getMethod("handle", RegisterScopeManifestCommand.class)
                .isAnnotationPresent(IdentityTransactional.class)).isTrue();
    }

    private static final class RecordingUseCase implements RegisterScopeManifestUseCase {
        private RegisterScopeManifestCommand executedCommand;

        @Override
        public void execute(RegisterScopeManifestCommand command) {
            executedCommand = command;
        }
    }
}
