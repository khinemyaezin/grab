package com.grab.app.productmerchandising.inventory.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.app.shared.events.catalog.ProductDeletedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductPresentationChangedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductVariantAddedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductVariantDeletedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductVariantRestoredIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductVariantUpdatedIntegrationEvent;
import com.inventory.application.model.write.ProjectProductVariantViewCommand;
import com.inventory.application.model.write.ProjectProductVariantViewCommand.DeleteProduct;
import com.inventory.application.model.write.ProjectProductVariantViewCommand.RenameProduct;
import com.inventory.application.model.write.ProjectProductVariantViewCommand.SetVariantStatus;
import com.inventory.application.model.write.ProjectProductVariantViewCommand.UpsertVariant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductVariantViewProjectionEventListenerTest {
    private static final Id PRODUCT_ID = new CommonId("product-1");
    private static final Id VARIANT_ID = new CommonId("variant-1");
    private static final Instant EVENT_TIME = Instant.parse("2026-10-01T00:00:00Z");
    @Mock
    private CommandBus commandBus;
    @Mock
    private IdGenerator idGenerator;
    @Captor
    private ArgumentCaptor<ProjectProductVariantViewCommand> commandCaptor;
    @InjectMocks
    private ProductVariantViewProjectionEventListener listener;

    @Test
    void onVariantAdded_addedVariant_dispatchesCompleteProjectionCommand() {
        when(idGenerator.convertIdFrom("product-1")).thenReturn(PRODUCT_ID);
        when(idGenerator.convertIdFrom("variant-1")).thenReturn(VARIANT_ID);
        ProductVariantAddedIntegrationEvent event = new ProductVariantAddedIntegrationEvent(
                "product-1", "variant-1", "SKU1", "Product", true, EVENT_TIME, 1
        );

        listener.onVariantAdded(event);

        verify(commandBus).dispatch(commandCaptor.capture());
        UpsertVariant update = (UpsertVariant) commandCaptor.getValue().update();
        assertThat(update.productId()).isEqualTo(PRODUCT_ID);
        assertThat(update.variantId()).isEqualTo(VARIANT_ID);
        assertThat(update.sku()).isEqualTo("SKU1");
        assertThat(update.productName()).isEqualTo("Product");
        assertThat(update.manageInventory()).isTrue();
        assertThat(update.replacePresentation()).isTrue();
    }

    @Test
    void onVariantUpdated_updatedVariant_dispatchesPartialProjectionCommand() {
        when(idGenerator.convertIdFrom("product-1")).thenReturn(PRODUCT_ID);
        when(idGenerator.convertIdFrom("variant-1")).thenReturn(VARIANT_ID);
        ProductVariantUpdatedIntegrationEvent event = new ProductVariantUpdatedIntegrationEvent(
                "product-1", "variant-1", "SKU2", false, EVENT_TIME, 1
        );

        listener.onVariantUpdated(event);

        verify(commandBus).dispatch(commandCaptor.capture());
        UpsertVariant update = (UpsertVariant) commandCaptor.getValue().update();
        assertThat(update.sku()).isEqualTo("SKU2");
        assertThat(update.manageInventory()).isFalse();
        assertThat(update.replacePresentation()).isFalse();
    }

    @Test
    void onVariantDeleted_deletedVariant_dispatchesInactiveStatus() {
        when(idGenerator.convertIdFrom("variant-1")).thenReturn(VARIANT_ID);
        ProductVariantDeletedIntegrationEvent event = new ProductVariantDeletedIntegrationEvent(
                "product-1", "variant-1", EVENT_TIME, 1
        );

        listener.onVariantDeleted(event);

        verify(commandBus).dispatch(commandCaptor.capture());
        SetVariantStatus update = (SetVariantStatus) commandCaptor.getValue().update();
        assertThat(update.variantId()).isEqualTo(VARIANT_ID);
        assertThat(update.active()).isFalse();
    }

    @Test
    void onVariantRestored_restoredVariant_dispatchesActiveStatus() {
        when(idGenerator.convertIdFrom("variant-1")).thenReturn(VARIANT_ID);
        ProductVariantRestoredIntegrationEvent event = new ProductVariantRestoredIntegrationEvent(
                "product-1", "variant-1", EVENT_TIME, 1
        );

        listener.onVariantRestored(event);

        verify(commandBus).dispatch(commandCaptor.capture());
        SetVariantStatus update = (SetVariantStatus) commandCaptor.getValue().update();
        assertThat(update.variantId()).isEqualTo(VARIANT_ID);
        assertThat(update.active()).isTrue();
    }

    @Test
    void onProductPresentationChanged_renamedProduct_dispatchesProductRename() {
        when(idGenerator.convertIdFrom("product-1")).thenReturn(PRODUCT_ID);
        ProductPresentationChangedIntegrationEvent event = new ProductPresentationChangedIntegrationEvent(
                "product-1", "Renamed", EVENT_TIME, 1
        );

        listener.onProductPresentationChanged(event);

        verify(commandBus).dispatch(commandCaptor.capture());
        RenameProduct update = (RenameProduct) commandCaptor.getValue().update();
        assertThat(update.productId()).isEqualTo(PRODUCT_ID);
        assertThat(update.productName()).isEqualTo("Renamed");
    }

    @Test
    void onProductDeleted_deletedProduct_dispatchesTombstoneCommand() {
        when(idGenerator.convertIdFrom("product-1")).thenReturn(PRODUCT_ID);
        ProductDeletedIntegrationEvent event = new ProductDeletedIntegrationEvent(
                "product-1", List.of("variant-1"), EVENT_TIME, 1
        );

        listener.onProductDeleted(event);

        verify(commandBus).dispatch(commandCaptor.capture());
        DeleteProduct update = (DeleteProduct) commandCaptor.getValue().update();
        assertThat(update.productId()).isEqualTo(PRODUCT_ID);
    }

    @Test
    void onVariantDeleted_commandFailure_propagatesToOutboxDelivery() {
        when(idGenerator.convertIdFrom("variant-1")).thenReturn(VARIANT_ID);
        IllegalStateException failure = new IllegalStateException("projection failed");
        when(commandBus.dispatch(any(ProjectProductVariantViewCommand.class))).thenThrow(failure);
        ProductVariantDeletedIntegrationEvent event = new ProductVariantDeletedIntegrationEvent(
                "product-1", "variant-1", EVENT_TIME, 1
        );

        assertThatThrownBy(() -> listener.onVariantDeleted(event)).isSameAs(failure);
    }
}
