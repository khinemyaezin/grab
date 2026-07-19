package com.inventory.domain.policy;

import com.grab.framework.id.Id;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.enums.InventoryStatus;
import com.inventory.domain.valueobject.InventoryQuantity;
import com.inventory.domain.valueobject.ReorderConfig;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VariantDeletedInventoryDiscontinuePolicyTest {

    private static final Id ID = id("item-1");
    private static final Id MERCHANT_ID = id("merchant-1");
    private static final Id VARIANT_ID = id("variant-1");
    private static final Id LOCATION_ID = id("location-1");

    @Test
    void shouldDiscontinue_activeItem_returnsTrue() {
        InventoryItem item = item(InventoryStatus.ACTIVE);

        assertThat(VariantDeletedInventoryDiscontinuePolicy.shouldDiscontinue(item)).isTrue();
    }

    @Test
    void shouldDiscontinue_outOfStockItem_returnsTrue() {
        InventoryItem item = item(InventoryStatus.OUT_OF_STOCK);

        assertThat(VariantDeletedInventoryDiscontinuePolicy.shouldDiscontinue(item)).isTrue();
    }

    @Test
    void shouldDiscontinue_suspendedItem_returnsTrue() {
        InventoryItem item = item(InventoryStatus.SUSPENDED);

        assertThat(VariantDeletedInventoryDiscontinuePolicy.shouldDiscontinue(item)).isTrue();
    }

    @Test
    void shouldDiscontinue_alreadyDiscontinued_returnsFalse() {
        InventoryItem item = item(InventoryStatus.DISCONTINUED);

        assertThat(VariantDeletedInventoryDiscontinuePolicy.shouldDiscontinue(item)).isFalse();
    }

    @Test
    void shouldDiscontinue_nullItem_returnsFalse() {
        assertThat(VariantDeletedInventoryDiscontinuePolicy.shouldDiscontinue(null)).isFalse();
    }

    @Test
    void selectForDiscontinue_filtersAlreadyDiscontinuedAndPreservesOthers() {
        InventoryItem active = item(InventoryStatus.ACTIVE);
        InventoryItem discontinued = item(InventoryStatus.DISCONTINUED);
        InventoryItem suspended = item(InventoryStatus.SUSPENDED);

        List<InventoryItem> selected = VariantDeletedInventoryDiscontinuePolicy.selectForDiscontinue(
                List.of(active, discontinued, suspended)
        );

        assertThat(selected).containsExactly(active, suspended);
    }

    @Test
    void selectForDiscontinue_nullOrEmpty_returnsEmpty() {
        assertThat(VariantDeletedInventoryDiscontinuePolicy.selectForDiscontinue(null)).isEmpty();
        assertThat(VariantDeletedInventoryDiscontinuePolicy.selectForDiscontinue(List.of())).isEmpty();
    }

    private static InventoryItem item(InventoryStatus status) {
        return new InventoryItem(
                ID,
                "SKU-001",
                MERCHANT_ID,
                VARIANT_ID,
                LOCATION_ID,
                InventoryQuantity.withOnHand(10),
                ReorderConfig.defaultConfig(),
                status,
                LocalDateTime.now()
        );
    }

    private static Id id(String value) {
        return () -> value;
    }
}
