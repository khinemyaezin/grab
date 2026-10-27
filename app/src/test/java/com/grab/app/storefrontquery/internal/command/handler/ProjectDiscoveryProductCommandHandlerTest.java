package com.grab.app.channel.discovery.internal.command.handler;

import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RefreshProductChannel;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.discovery.application.port.inbound.ProjectDiscoveryProductUseCase;
import com.grab.app.channel.discovery.internal.config.DiscoveryProjectionRetryable;
import com.grab.app.channel.discovery.internal.config.StorefrontQueryTransactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Propagation;

import java.lang.reflect.Method;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProjectDiscoveryProductCommandHandlerTest {
    @Mock
    private ProjectDiscoveryProductUseCase useCase;

    private ProjectDiscoveryProductCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ProjectDiscoveryProductCommandHandler(useCase);
    }

    @Test
    void handle_validCommand_delegatesToUseCase() {
        ProjectDiscoveryProductCommand command = command();

        Void result = handler.handle(command);

        assertThat(result).isNull();
        verify(useCase).execute(command);
    }

    @Test
    void handle_useCaseThrows_propagatesException() {
        ProjectDiscoveryProductCommand command = command();
        IllegalStateException failure = new IllegalStateException("catalog unavailable");
        doThrow(failure).when(useCase).execute(command);

        assertThatThrownBy(() -> handler.handle(command)).isSameAs(failure);
        verify(useCase).execute(command);
    }

    @Test
    void handle_hasDeclarativeRetryableAndTransactionalAnnotations() throws NoSuchMethodException {
        Method handleMethod = ProjectDiscoveryProductCommandHandler.class.getMethod("handle", ProjectDiscoveryProductCommand.class);
        StorefrontQueryTransactional txAnnotation = handleMethod.getAnnotation(StorefrontQueryTransactional.class);
        DiscoveryProjectionRetryable retryAnnotation = handleMethod.getAnnotation(DiscoveryProjectionRetryable.class);

        assertThat(txAnnotation).isNotNull();
        assertThat(txAnnotation.propagation()).isEqualTo(Propagation.REQUIRES_NEW);
        assertThat(retryAnnotation).isNotNull();
    }

    @Test
    void getCommandType_returnsProjectDiscoveryProductCommand() {
        assertThat(handler.getCommandType()).isEqualTo(ProjectDiscoveryProductCommand.class);
    }

    private ProjectDiscoveryProductCommand command() {
        Id productId = new CommonId("product-1");
        Id channelId = new CommonId("channel-1");
        Instant occurredAt = Instant.parse("2026-10-01T00:00:00Z");
        RefreshProductChannel update = new RefreshProductChannel(productId, channelId, occurredAt);
        return new ProjectDiscoveryProductCommand(update);
    }
}
