package com.inventory.domain.service.impl;

import com.grab.framework.id.Id;
import com.inventory.domain.support.InventoryDomainLoggerExtension;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.enums.InventoryStatus;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.service.ReorderService.ReorderPriority;
import com.inventory.domain.service.ReorderService.ReorderSuggestion;
import com.inventory.domain.valueobject.InventoryQuantity;
import com.inventory.domain.valueobject.ReorderConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test points: <br>
 * See docs/features/reorder-service.md
 **/
@ExtendWith({MockitoExtension.class, InventoryDomainLoggerExtension.class})
class DefaultReorderServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    private DefaultReorderService reorderService;

    @BeforeEach
    void setUp() {
        reorderService = new DefaultReorderService(inventoryRepository);
    }

    @Test
    void calculatePriority_withZeroAvailable_shouldReturnCritical() {
        InventoryItem item = createInventoryItem("item-1", "SKU-001", "loc-1", 0, 10, 20, 50);

        ReorderPriority result = reorderService.calculatePriority(item);

        assertThat(result).isEqualTo(ReorderPriority.CRITICAL);
    }

    @Test
    void calculatePriority_withAvailableBelowSafetyStock_shouldReturnCritical() {
        InventoryItem item = createInventoryItem("item-1", "SKU-001", "loc-1", 5, 10, 20, 50);

        ReorderPriority result = reorderService.calculatePriority(item);

        assertThat(result).isEqualTo(ReorderPriority.CRITICAL);
    }

    @Test
    void calculatePriority_withAvailableEqualSafetyStock_shouldReturnCritical() {
        InventoryItem item = createInventoryItem("item-1", "SKU-001", "loc-1", 10, 10, 20, 50);

        ReorderPriority result = reorderService.calculatePriority(item);

        assertThat(result).isEqualTo(ReorderPriority.CRITICAL);
    }

    @Test
    void calculatePriority_withAvailableBelowReorderPoint_shouldReturnHigh() {
        InventoryItem item = createInventoryItem("item-1", "SKU-001", "loc-1", 15, 10, 20, 50);

        ReorderPriority result = reorderService.calculatePriority(item);

        assertThat(result).isEqualTo(ReorderPriority.HIGH);
    }

    @Test
    void calculatePriority_withAvailableEqualReorderPoint_shouldReturnHigh() {
        InventoryItem item = createInventoryItem("item-1", "SKU-001", "loc-1", 20, 10, 20, 50);

        ReorderPriority result = reorderService.calculatePriority(item);

        assertThat(result).isEqualTo(ReorderPriority.HIGH);
    }

    @Test
    void calculatePriority_withAvailableWithin20PercentAboveReorderPoint_shouldReturnMedium() {
        InventoryItem item = createInventoryItem("item-1", "SKU-001", "loc-1", 22, 10, 20, 50);

        ReorderPriority result = reorderService.calculatePriority(item);

        assertThat(result).isEqualTo(ReorderPriority.MEDIUM);
    }

    @Test
    void calculatePriority_withAvailableAtExact20PercentAboveReorderPoint_shouldReturnMedium() {
        InventoryItem item = createInventoryItem("item-1", "SKU-001", "loc-1", 24, 10, 20, 50);

        ReorderPriority result = reorderService.calculatePriority(item);

        assertThat(result).isEqualTo(ReorderPriority.MEDIUM);
    }

    @Test
    void calculatePriority_withAvailableAbove20Percent_shouldReturnLow() {
        InventoryItem item = createInventoryItem("item-1", "SKU-001", "loc-1", 25, 10, 20, 50);

        ReorderPriority result = reorderService.calculatePriority(item);

        assertThat(result).isEqualTo(ReorderPriority.LOW);
    }

    @Test
    void calculatePriority_withHighAvailable_shouldReturnLow() {
        InventoryItem item = createInventoryItem("item-1", "SKU-001", "loc-1", 100, 10, 20, 50);

        ReorderPriority result = reorderService.calculatePriority(item);

        assertThat(result).isEqualTo(ReorderPriority.LOW);
    }

    @Test
    void calculateReorderSuggestions_shouldReturnAllActiveItemsWithNonLowPriority() {
        InventoryItem critical = createInventoryItem("critical", "SKU-001", "loc-1", 0, 10, 20, 50);
        InventoryItem high = createInventoryItem("high", "SKU-002", "loc-1", 15, 10, 20, 50);
        InventoryItem medium = createInventoryItem("medium", "SKU-003", "loc-1", 22, 10, 20, 50);
        InventoryItem low = createInventoryItem("low", "SKU-004", "loc-1", 100, 10, 20, 50);

        when(inventoryRepository.findAll()).thenReturn(List.of(critical, high, medium, low));

        List<ReorderSuggestion> result = reorderService.calculateReorderSuggestions();

        assertThat(result).hasSize(3);
        assertThat(result.stream().map(ReorderSuggestion::sku).toList()).containsExactlyInAnyOrder("SKU-001", "SKU-002", "SKU-003");
        verify(inventoryRepository).findAll();
    }

    @Test
    void calculateReorderSuggestions_shouldFilterInactiveItems() {
        InventoryItem active = createInventoryItem("active", "SKU-001", "loc-1", 0, 10, 20, 50);
        InventoryItem inactive = createInactiveInventoryItem("inactive", "SKU-002", "loc-1", 0, 10, 20, 50);

        when(inventoryRepository.findAll()).thenReturn(List.of(active, inactive));

        List<ReorderSuggestion> result = reorderService.calculateReorderSuggestions();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().sku()).isEqualTo("SKU-001");
    }

    @Test
    void calculateReorderSuggestions_shouldFilterLowPriorityItems() {
        InventoryItem critical = createInventoryItem("critical", "SKU-001", "loc-1", 0, 10, 20, 50);
        InventoryItem low = createInventoryItem("low", "SKU-002", "loc-1", 50, 10, 20, 50);

        when(inventoryRepository.findAll()).thenReturn(List.of(critical, low));

        List<ReorderSuggestion> result = reorderService.calculateReorderSuggestions();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().sku()).isEqualTo("SKU-001");
    }

    @Test
    void calculateReorderSuggestions_shouldSortByPriority() {
        InventoryItem medium = createInventoryItem("medium", "SKU-002", "loc-1", 22, 10, 20, 50);
        InventoryItem high = createInventoryItem("high", "SKU-003", "loc-1", 15, 10, 20, 50);
        InventoryItem critical = createInventoryItem("critical", "SKU-004", "loc-1", 0, 10, 20, 50);

        when(inventoryRepository.findAll()).thenReturn(List.of(medium, high, critical));

        List<ReorderSuggestion> result = reorderService.calculateReorderSuggestions();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).priority()).isEqualTo(ReorderPriority.CRITICAL);
        assertThat(result.get(1).priority()).isEqualTo(ReorderPriority.HIGH);
        assertThat(result.get(2).priority()).isEqualTo(ReorderPriority.MEDIUM);
    }

    @Test
    void calculateReorderSuggestions_shouldReturnEmptyWhenNoActiveItems() {
        InventoryItem suspended = createInactiveInventoryItem("suspended", "SKU-001", "loc-1", 0, 10, 20, 50);

        when(inventoryRepository.findAll()).thenReturn(List.of(suspended));

        List<ReorderSuggestion> result = reorderService.calculateReorderSuggestions();

        assertThat(result).isEmpty();
    }

    @Test
    void calculateReorderSuggestions_shouldMapAllFieldsCorrectly() {
        InventoryItem item = createInventoryItem("item-1", "SKU-001", "loc-1", 15, 10, 20, 50);

        when(inventoryRepository.findAll()).thenReturn(List.of(item));

        List<ReorderSuggestion> result = reorderService.calculateReorderSuggestions();

        assertThat(result).hasSize(1);
        ReorderSuggestion suggestion = result.getFirst();
        assertThat(suggestion.inventoryItemId()).isEqualTo(item.getId());
        assertThat(suggestion.sku()).isEqualTo("SKU-001");
        assertThat(suggestion.productVariantId()).isEqualTo(item.getProductVariantId());
        assertThat(suggestion.locationId()).isEqualTo(item.getLocationId());
        assertThat(suggestion.currentAvailable()).isEqualTo(15);
        assertThat(suggestion.reorderPoint()).isEqualTo(20);
        assertThat(suggestion.suggestedQuantity()).isGreaterThan(0);
        assertThat(suggestion.priority()).isEqualTo(ReorderPriority.HIGH);
    }


    @Test
    void calculateReorderSuggestionsForLocation_shouldReturnOnlySuggestionsForLocation() {
        Id locationId = id("loc-1");
        InventoryItem item1 = createInventoryItem("item-1", "SKU-001", "loc-1", 0, 10, 20, 50);

        when(inventoryRepository.findByLocation(locationId)).thenReturn(List.of(item1));

        List<ReorderSuggestion> result = reorderService.calculateReorderSuggestionsForLocation(locationId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().sku()).isEqualTo("SKU-001");
        assertThat(result.getFirst().locationId()).isEqualTo(locationId);
        verify(inventoryRepository).findByLocation(locationId);
    }

    @Test
    void calculateReorderSuggestionsForLocation_shouldFilterInactiveItems() {
        Id locationId = id("loc-1");
        InventoryItem active = createInventoryItem("active", "SKU-001", "loc-1", 0, 10, 20, 50);
        InventoryItem inactive = createInactiveInventoryItem("inactive", "SKU-002", "loc-1", 0, 10, 20, 50);

        when(inventoryRepository.findByLocation(locationId)).thenReturn(List.of(active, inactive));

        List<ReorderSuggestion> result = reorderService.calculateReorderSuggestionsForLocation(locationId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().sku()).isEqualTo("SKU-001");
    }

    @Test
    void calculateReorderSuggestionsForLocation_shouldSortByPriority() {
        Id locationId = id("loc-1");
        InventoryItem high = createInventoryItem("high", "SKU-001", "loc-1", 15, 10, 20, 50);
        InventoryItem critical = createInventoryItem("critical", "SKU-002", "loc-1", 0, 10, 20, 50);

        when(inventoryRepository.findByLocation(locationId)).thenReturn(List.of(high, critical));

        List<ReorderSuggestion> result = reorderService.calculateReorderSuggestionsForLocation(locationId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).priority()).isEqualTo(ReorderPriority.CRITICAL);
        assertThat(result.get(1).priority()).isEqualTo(ReorderPriority.HIGH);
    }


    @Test
    void calculateReorderSuggestionsForSku_shouldReturnOnlySuggestionsForSku() {
        InventoryItem item1 = createInventoryItem("item-1", "SKU-001", "loc-1", 0, 10, 20, 50);
        InventoryItem item2 = createInventoryItem("item-2", "SKU-001", "loc-2", 0, 10, 20, 50);

        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(item1, item2));

        List<ReorderSuggestion> result = reorderService.calculateReorderSuggestionsForSku("SKU-001");

        assertThat(result).hasSize(2);
        assertThat(result.stream().map(ReorderSuggestion::sku).toList())
                .allMatch(sku -> sku.equals("SKU-001"));
        verify(inventoryRepository).findBySku("SKU-001");
    }

    @Test
    void calculateReorderSuggestionsForSku_shouldFilterInactiveItems() {
        InventoryItem active = createInventoryItem("active", "SKU-001", "loc-1", 0, 10, 20, 50);
        InventoryItem inactive = createInactiveInventoryItem("inactive", "SKU-001", "loc-2", 0, 10, 20, 50);

        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(active, inactive));

        List<ReorderSuggestion> result = reorderService.calculateReorderSuggestionsForSku("SKU-001");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().locationId().getValue()).isEqualTo("loc-1");
    }

    @Test
    void calculateReorderSuggestionsForSku_shouldSortByPriority() {
        InventoryItem high = createInventoryItem("high", "SKU-001", "loc-1", 15, 10, 20, 50);
        InventoryItem critical = createInventoryItem("critical", "SKU-001", "loc-2", 0, 10, 20, 50);

        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(high, critical));

        List<ReorderSuggestion> result = reorderService.calculateReorderSuggestionsForSku("SKU-001");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).priority()).isEqualTo(ReorderPriority.CRITICAL);
        assertThat(result.get(1).priority()).isEqualTo(ReorderPriority.HIGH);
    }


    @Test
    void getCriticalReorderItems_shouldReturnOutOfStockAndLowStockItems() {
        InventoryItem outOfStock = createInventoryItem("out-of-stock", "SKU-001", "loc-1", 0, 10, 20, 50);
        InventoryItem lowStock = createInventoryItem("low-stock", "SKU-002", "loc-1", 5, 10, 20, 50);
        InventoryItem normal = createInventoryItem("normal", "SKU-003", "loc-1", 50, 10, 20, 50);

        when(inventoryRepository.findOutOfStock()).thenReturn(List.of(outOfStock));
        when(inventoryRepository.findLowStock()).thenReturn(List.of(lowStock));

        List<InventoryItem> result = reorderService.getCriticalReorderItems();

        assertThat(result).hasSize(2);
        assertThat(result).contains(outOfStock, lowStock);
        assertThat(result).doesNotContain(normal);
    }

    @Test
    void getCriticalReorderItems_shouldFilterInactiveItems() {
        InventoryItem outOfStock = createInventoryItem("out-of-stock", "SKU-001", "loc-1", 0, 10, 20, 50);
        InventoryItem outOfStockInactive = createInactiveInventoryItem("inactive-oos", "SKU-002", "loc-1", 0, 10, 20, 50);

        when(inventoryRepository.findOutOfStock()).thenReturn(List.of(outOfStock, outOfStockInactive));
        when(inventoryRepository.findLowStock()).thenReturn(List.of());

        List<InventoryItem> result = reorderService.getCriticalReorderItems();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(outOfStock);
        assertThat(result.getFirst().isActive()).isTrue();
    }

    @Test
    void getCriticalReorderItems_shouldRemoveDuplicates() {
        InventoryItem item = createInventoryItem("item-1", "SKU-001", "loc-1", 0, 10, 20, 50);

        when(inventoryRepository.findOutOfStock()).thenReturn(List.of(item));
        when(inventoryRepository.findLowStock()).thenReturn(List.of(item));

        List<InventoryItem> result = reorderService.getCriticalReorderItems();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(item);
    }

    @Test
    void getCriticalReorderItems_shouldReturnEmptyWhenNoCriticalItems() {
        when(inventoryRepository.findOutOfStock()).thenReturn(List.of());
        when(inventoryRepository.findLowStock()).thenReturn(List.of());

        List<InventoryItem> result = reorderService.getCriticalReorderItems();

        assertThat(result).isEmpty();
    }

    @Test
    void getCriticalReorderItems_shouldHandleOnlyOutOfStock() {
        InventoryItem outOfStock = createInventoryItem("out-of-stock", "SKU-001", "loc-1", 0, 10, 20, 50);

        when(inventoryRepository.findOutOfStock()).thenReturn(List.of(outOfStock));
        when(inventoryRepository.findLowStock()).thenReturn(List.of());

        List<InventoryItem> result = reorderService.getCriticalReorderItems();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(outOfStock);
    }

    @Test
    void getCriticalReorderItems_shouldHandleOnlyLowStock() {
        InventoryItem lowStock = createInventoryItem("low-stock", "SKU-001", "loc-1", 5, 10, 20, 50);

        when(inventoryRepository.findOutOfStock()).thenReturn(List.of());
        when(inventoryRepository.findLowStock()).thenReturn(List.of(lowStock));

        List<InventoryItem> result = reorderService.getCriticalReorderItems();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(lowStock);
    }


    private InventoryItem createInventoryItem(
            String itemId, String sku, String locationId,
            int onHand, int safetyStock, int reorderPoint, int reorderQuantity) {
        return new InventoryItem(
                id(itemId),
                sku,
                id("variant-1"),
                id(locationId),
                InventoryQuantity.withOnHand(onHand),
                ReorderConfig.of(safetyStock, reorderPoint, reorderQuantity),
                InventoryStatus.ACTIVE,
                LocalDateTime.now()
        );
    }

    private InventoryItem createInactiveInventoryItem(
            String itemId, String sku, String locationId,
            int onHand, int safetyStock, int reorderPoint, int reorderQuantity) {
        return new InventoryItem(
                id(itemId),
                sku,
                id("variant-1"),
                id(locationId),
                InventoryQuantity.withOnHand(onHand),
                ReorderConfig.of(safetyStock, reorderPoint, reorderQuantity),
                InventoryStatus.SUSPENDED,
                LocalDateTime.now()
        );
    }

    private Id id(String value) {
        return new Id() {
            @Override
            public String getValue() {
                return value;
            }

            @Override
            public boolean equals(Object o) {
                if (!(o instanceof Id other)) return false;
                return Objects.equals(value, other.getValue());
            }

            @Override
            public int hashCode() {
                return Objects.hashCode(value);
            }

            @Override
            public String toString() {
                return value;
            }
        };
    }
}
