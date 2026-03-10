package com.inventory.domain.exception;

import com.grab.framework.exception.ErrorCategory;
import com.inventory.domain.valueobject.InventoryQuantity;
import com.inventory.domain.valueobject.ReorderConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryDomainErrorContractTest {

    @Test
    void allocationErrors_shouldUseInvAllocCodes() {
        InventoryDomainError.QuantityNotPositive quantityNotPositive = new InventoryDomainError.QuantityNotPositive();
        InventoryDomainError.NoAvailableInventory noAvailableInventory = new InventoryDomainError.NoAvailableInventory("SKU-1");
        InventoryDomainError.InsufficientStock insufficientStock = new InventoryDomainError.InsufficientStock(5, 10);

        assertThat(quantityNotPositive.code()).isEqualTo("inv.alloc.quantity_not_positive");
        assertThat(noAvailableInventory.code()).isEqualTo("inv.alloc.no_available_inventory");
        assertThat(insufficientStock.code()).isEqualTo("inv.alloc.insufficient_stock");
    }

    @Test
    void domainValidationErrors_shouldUseInvDomainCodes() {
        InventoryDomainError.InsufficientQuantity error = new InventoryDomainError.InsufficientQuantity(3, 9);

        assertThat(error.kind()).isEqualTo(ErrorCategory.BUSINESS_RULE);
        assertThat(error.code()).isEqualTo("inv.domain.insufficient_quantity");
        assertThat(error.args()).containsEntry("available", 3).containsEntry("requested", 9);
    }

    @Test
    void inventoryQuantity_negativeOnHand_shouldThrowTypedValidationError() {
        assertThatThrownBy(() -> new InventoryQuantity(-1, 0, 0, 0))
                .isInstanceOf(InventoryDomainValidationException.class)
                .satisfies(exception -> {
                    InventoryDomainValidationException typed = (InventoryDomainValidationException) exception;
                    assertThat(typed.getMessageSource().code()).isEqualTo("inv.domain.invalid_on_hand_quantity");
                    assertThat(typed.getMessageSource().kind()).isEqualTo(ErrorCategory.BUSINESS_RULE);
                });
    }

    @Test
    void reorderConfig_invalidRelation_shouldThrowTypedValidationError() {
        assertThatThrownBy(() -> new ReorderConfig(10, 5, 1, null))
                .isInstanceOf(InventoryDomainValidationException.class)
                .satisfies(exception -> {
                    InventoryDomainValidationException typed = (InventoryDomainValidationException) exception;
                    assertThat(typed.getMessageSource().code()).isEqualTo("inv.domain.invalid_reorder_config");
                    assertThat(typed.getMessageSource().kind()).isEqualTo(ErrorCategory.BUSINESS_RULE);
                });
    }
}
