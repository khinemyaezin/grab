package com.grab.app.productmerchandising.inventory.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.app.shared.events.catalog.ProductDeletedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductVariantDeletedIntegrationEvent;
import com.inventory.application.model.write.DiscontinueInventoryForDeletedVariantCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductVariantDeletedInventoryCascadeEventListenerTest {

    @Mock
    private CommandBus commandBus;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private ProductVariantDeletedInventoryCascadeEventListener listener;

    @Test
    void onVariantDeleted_deletedVariant_dispatchesDiscontinueCommand() {
        when(idGenerator.convertIdFrom("variant-1")).thenReturn(new CommonId("variant-1"));

        listener.onVariantDeleted(new ProductVariantDeletedIntegrationEvent(
                "product-1", "variant-1", Instant.now(), 1));

        ArgumentCaptor<DiscontinueInventoryForDeletedVariantCommand> captor =
                ArgumentCaptor.forClass(DiscontinueInventoryForDeletedVariantCommand.class);
        verify(commandBus).dispatch(captor.capture());
        assertThat(captor.getValue().productVariantId().getValue()).isEqualTo("variant-1");
    }

    @Test
    void onProductDeleted_multipleVariants_dispatchesCommandPerVariant() {
        when(idGenerator.convertIdFrom("variant-1")).thenReturn(new CommonId("variant-1"));
        when(idGenerator.convertIdFrom("variant-2")).thenReturn(new CommonId("variant-2"));

        listener.onProductDeleted(new ProductDeletedIntegrationEvent(
                "product-1",
                List.of("variant-1", "variant-2"),
                Instant.now(),
                1
        ));

        ArgumentCaptor<DiscontinueInventoryForDeletedVariantCommand> captor =
                ArgumentCaptor.forClass(DiscontinueInventoryForDeletedVariantCommand.class);
        verify(commandBus, times(2)).dispatch(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(command -> command.productVariantId().getValue())
                .containsExactly("variant-1", "variant-2");
    }

    @Test
    void onProductDeleted_withNoVariants_doesNotDispatch() {
        listener.onProductDeleted(new ProductDeletedIntegrationEvent(
                "product-1", List.of(), Instant.now(), 1));

        verify(commandBus, never()).dispatch(any());
    }
}
