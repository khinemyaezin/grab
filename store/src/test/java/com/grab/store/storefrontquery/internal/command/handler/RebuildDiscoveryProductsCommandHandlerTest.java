package com.grab.store.storefrontquery.internal.command.handler;

import com.grab.discovery.application.model.write.RebuildDiscoveryProductsCommand;
import com.grab.discovery.application.model.write.RebuildProgressResult;
import com.grab.discovery.application.port.inbound.RebuildDiscoveryProductsUseCase;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.storefrontquery.internal.config.StorefrontQueryTransactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Propagation;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RebuildDiscoveryProductsCommandHandlerTest {
    @Mock
    private RebuildDiscoveryProductsUseCase useCase;

    private RebuildDiscoveryProductsCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RebuildDiscoveryProductsCommandHandler(useCase);
    }

    @Test
    void handle_validCommand_delegatesDirectlyToUseCase() {
        RebuildDiscoveryProductsCommand command = new RebuildDiscoveryProductsCommand(50);
        Id runId = new CommonId("run-1");
        Id prodId = new CommonId("prod-1");
        Id channelId = new CommonId("chan-1");
        RebuildProgressResult expected = new RebuildProgressResult(
                runId,
                RebuildProgressResult.Stage.PUBLICATIONS,
                prodId,
                channelId,
                10L,
                0L,
                false
        );
        when(useCase.execute(command)).thenReturn(expected);

        RebuildProgressResult result = handler.handle(command);

        assertThat(result).isSameAs(expected);
        verify(useCase).execute(command);
    }

    @Test
    void handle_useCaseThrows_propagatesException() {
        RebuildDiscoveryProductsCommand command = new RebuildDiscoveryProductsCommand(50);
        IllegalStateException failure = new IllegalStateException("database lock failure");
        doThrow(failure).when(useCase).execute(command);

        assertThatThrownBy(() -> handler.handle(command)).isSameAs(failure);
        verify(useCase).execute(command);
    }

    @Test
    void handle_hasDeclarativeStorefrontQueryTransactionalAnnotation() throws NoSuchMethodException {
        Method handleMethod = RebuildDiscoveryProductsCommandHandler.class.getMethod("handle", RebuildDiscoveryProductsCommand.class);
        StorefrontQueryTransactional annotation = handleMethod.getAnnotation(StorefrontQueryTransactional.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.propagation()).isEqualTo(Propagation.REQUIRED);
    }

    @Test
    void getCommandType_returnsRebuildDiscoveryProductsCommand() {
        assertThat(handler.getCommandType()).isEqualTo(RebuildDiscoveryProductsCommand.class);
    }
}
