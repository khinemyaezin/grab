package com.grab.store.identity.internal.command.handler;

import com.grab.store.identity.internal.config.IdentityTransactional;
import com.identity.application.model.write.RegisterAuthorityManifestCommand;
import com.identity.application.port.inbound.RegisterAuthorityManifestUseCase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
class RegisterAuthorityManifestCommandHandlerTest {

    @Test
    void delegatesCommandAndDeclaresWriteTransactionBoundary() throws NoSuchMethodException {
        RecordingUseCase useCase = new RecordingUseCase();
        RegisterAuthorityManifestCommandHandler handler = new RegisterAuthorityManifestCommandHandler(useCase);
        var command = new RegisterAuthorityManifestCommand("identity", 1, List.of());

        assertThat(handler.handle(command)).isNull();
        assertThat(handler.getCommandType()).isEqualTo(RegisterAuthorityManifestCommand.class);
        assertThat(useCase.executedCommand).isSameAs(command);
        assertThat(RegisterAuthorityManifestCommandHandler.class
                .getMethod("handle", RegisterAuthorityManifestCommand.class)
                .isAnnotationPresent(IdentityTransactional.class)).isTrue();
    }

    private static final class RecordingUseCase implements RegisterAuthorityManifestUseCase {
        private RegisterAuthorityManifestCommand executedCommand;

        @Override
        public void execute(RegisterAuthorityManifestCommand command) {
            executedCommand = command;
        }
    }
}
