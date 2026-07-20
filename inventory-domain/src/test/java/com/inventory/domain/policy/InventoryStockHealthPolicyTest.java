package com.inventory.domain.policy;

import com.inventory.domain.enums.StockHealth;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryStockHealthPolicyTest {

    @Test
    void classify_availableZeroOrNegative_returnsOutOfStock() {
        assertThat(InventoryStockHealthPolicy.classify(0, 10, 50)).isEqualTo(StockHealth.OUT_OF_STOCK);
        assertThat(InventoryStockHealthPolicy.classify(-1, 10, 50)).isEqualTo(StockHealth.OUT_OF_STOCK);
    }

    @Test
    void classify_availableAtOrBelowSafetyStock_returnsCritical() {
        assertThat(InventoryStockHealthPolicy.classify(1, 10, 50)).isEqualTo(StockHealth.CRITICAL);
        assertThat(InventoryStockHealthPolicy.classify(10, 10, 50)).isEqualTo(StockHealth.CRITICAL);
    }

    @Test
    void classify_availableAboveSafetyStockAtOrBelowReorderPoint_returnsLowStock() {
        assertThat(InventoryStockHealthPolicy.classify(11, 10, 50)).isEqualTo(StockHealth.LOW_STOCK);
        assertThat(InventoryStockHealthPolicy.classify(50, 10, 50)).isEqualTo(StockHealth.LOW_STOCK);
    }

    @Test
    void classify_availableAboveReorderPoint_returnsInStock() {
        assertThat(InventoryStockHealthPolicy.classify(51, 10, 50)).isEqualTo(StockHealth.IN_STOCK);
    }

    @Test
    void classify_zeroSafetyStock_treatsPositiveBelowReorderAsLowStock() {
        assertThat(InventoryStockHealthPolicy.classify(1, 0, 50)).isEqualTo(StockHealth.LOW_STOCK);
        assertThat(InventoryStockHealthPolicy.classify(0, 0, 50)).isEqualTo(StockHealth.OUT_OF_STOCK);
    }
}
