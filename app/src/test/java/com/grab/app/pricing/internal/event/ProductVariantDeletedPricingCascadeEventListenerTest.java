package com.grab.app.catalog.pricing.internal.adapter.inbound.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.app.shared.events.catalog.ProductVariantDeletedIntegrationEvent;
import com.pricing.application.model.write.DeletePriceSetForDeletedVariantCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductVariantDeletedPricingCascadeEventListenerTest {
    @Mock
    private CommandBus commandBus;
    @Mock
    private IdGenerator idGenerator;
    @Captor
    private ArgumentCaptor<DeletePriceSetForDeletedVariantCommand> commandCaptor;
    @InjectMocks
    private ProductVariantDeletedPricingCascadeEventListener listener;

    @Test
    void onVariantDeleted_existingVariant_dispatchesDeleteCommand() {
        Id variantId = new CommonId("variant-1");
        when(idGenerator.convertIdFrom("variant-1")).thenReturn(variantId);
        ProductVariantDeletedIntegrationEvent event = new ProductVariantDeletedIntegrationEvent(
                "product-1", "variant-1", Instant.parse("2026-10-01T00:00:00Z"), 1
        );

        listener.onVariantDeleted(event);

        verify(commandBus).dispatch(commandCaptor.capture());
        assertThat(commandCaptor.getValue().variantId()).isEqualTo(variantId);
    }

    @Test
    void onVariantDeleted_missingVariant_doesNotDispatchCommand() {
        ProductVariantDeletedIntegrationEvent event = new ProductVariantDeletedIntegrationEvent(
                "product-1", null, Instant.parse("2026-10-01T00:00:00Z"), 1
        );

        listener.onVariantDeleted(event);

        verifyNoInteractions(commandBus, idGenerator);
    }
}
