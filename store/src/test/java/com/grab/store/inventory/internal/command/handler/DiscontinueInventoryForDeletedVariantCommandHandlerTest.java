package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.id.impl.CommonId;
import com.grab.store.inventory.internal.command.DiscontinueInventoryForDeletedVariantCommand;
import com.grab.store.inventory.internal.command.DiscontinueInventoryForDeletedVariantResult;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.enums.InventoryStatus;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.valueobject.InventoryQuantity;
import com.inventory.domain.valueobject.ReorderConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscontinueInventoryForDeletedVariantCommandHandlerTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private DiscontinueInventoryForDeletedVariantCommandHandler handler;

    @Test
    void handle_discontinuesEligibleItemsAndSkipsAlreadyDiscontinued() {
        InventoryItem active = item("item-1", InventoryStatus.ACTIVE, 25);
        InventoryItem discontinued = item("item-2", InventoryStatus.DISCONTINUED, 5);
        InventoryItem suspended = item("item-3", InventoryStatus.SUSPENDED, 8);
        when(inventoryRepository.findByProductVariantId(new CommonId("variant-1")))
                .thenReturn(List.of(active, discontinued, suspended));

        DiscontinueInventoryForDeletedVariantResult result = handler.handle(
                new DiscontinueInventoryForDeletedVariantCommand(new CommonId("variant-1"))
        );

        assertThat(result.productVariantId()).isEqualTo("variant-1");
        assertThat(result.discontinuedCount()).isEqualTo(2);
        assertThat(result.skippedCount()).isEqualTo(1);
        assertThat(active.getStatus()).isEqualTo(InventoryStatus.DISCONTINUED);
        assertThat(suspended.getStatus()).isEqualTo(InventoryStatus.DISCONTINUED);
        assertThat(active.getQuantity().onHand()).isEqualTo(25);
        assertThat(suspended.getQuantity().onHand()).isEqualTo(8);

        ArgumentCaptor<InventoryItem> saved = ArgumentCaptor.forClass(InventoryItem.class);
        verify(inventoryRepository, times(2)).save(saved.capture());
        assertThat(saved.getAllValues()).containsExactly(active, suspended);
    }

    @Test
    void handle_noItems_returnsZeroCounts() {
        when(inventoryRepository.findByProductVariantId(new CommonId("variant-1")))
                .thenReturn(List.of());

        DiscontinueInventoryForDeletedVariantResult result = handler.handle(
                new DiscontinueInventoryForDeletedVariantCommand(new CommonId("variant-1"))
        );

        assertThat(result.discontinuedCount()).isZero();
        assertThat(result.skippedCount()).isZero();
        verify(inventoryRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private static InventoryItem item(String id, InventoryStatus status, int onHand) {
        return new InventoryItem(
                new CommonId(id),
                "SKU-001",
                new CommonId("merchant-1"),
                new CommonId("variant-1"),
                new CommonId("location-1"),
                InventoryQuantity.withOnHand(onHand),
                ReorderConfig.defaultConfig(),
                status,
                LocalDateTime.now()
        );
    }
}
