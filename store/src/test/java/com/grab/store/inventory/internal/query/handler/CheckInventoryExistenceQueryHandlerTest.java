package com.grab.store.inventory.internal.query.handler;

import com.inventory.application.service.CheckInventoryExistenceService;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.inventory.application.model.read.CheckInventoryExistenceQuery;
import com.inventory.application.model.read.CheckInventoryExistenceResult;
import com.inventory.application.port.outbound.InventoryQueryPort;
import com.inventory.application.model.read.InventoryExistenceView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckInventoryExistenceServiceTest {

    @Mock
    private InventoryQueryPort inventoryQueryPort;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private CheckInventoryExistenceService handler;

    @Test
    void handle_withMixedSkus_shouldMarkExistsAndMissing() {
        CheckInventoryExistenceQuery query = new CheckInventoryExistenceQuery(
                new CommonId("seller-1"),
                new CommonId("loc-1"),
                List.of("SKU-001", "SKU-002", "SKU-003")
        );
        when(inventoryQueryPort.findExistenceByMerchantLocationAndSkus(
                "seller-1", "loc-1", List.of("SKU-001", "SKU-002", "SKU-003")))
                .thenReturn(List.of(
                        new InventoryExistenceView("uuid-inv-1", "SKU-001"),
                        new InventoryExistenceView("uuid-inv-2", "SKU-002")
                ));
        when(idGenerator.convertIdFrom("uuid-inv-1")).thenReturn(new CommonId("uuid-inv-1"));
        when(idGenerator.convertIdFrom("uuid-inv-2")).thenReturn(new CommonId("uuid-inv-2"));

        CheckInventoryExistenceResult result = handler.execute(query);

        assertThat(result.items()).containsExactly(
                new CheckInventoryExistenceResult.Entry("SKU-001", true, new CommonId("uuid-inv-1")),
                new CheckInventoryExistenceResult.Entry("SKU-002", true, new CommonId("uuid-inv-2")),
                new CheckInventoryExistenceResult.Entry("SKU-003", false, null)
        );
    }

    @Test
    void handle_withDuplicateSkus_shouldDedupePreservingOrder() {
        CheckInventoryExistenceQuery query = new CheckInventoryExistenceQuery(
                new CommonId("seller-1"),
                new CommonId("loc-1"),
                List.of("SKU-001", "SKU-002", "SKU-001")
        );
        when(inventoryQueryPort.findExistenceByMerchantLocationAndSkus(
                eq("seller-1"), eq("loc-1"), eq(List.of("SKU-001", "SKU-002"))))
                .thenReturn(List.of(new InventoryExistenceView("uuid-inv-1", "SKU-001")));
        when(idGenerator.convertIdFrom("uuid-inv-1")).thenReturn(new CommonId("uuid-inv-1"));

        CheckInventoryExistenceResult result = handler.execute(query);

        assertThat(result.items()).containsExactly(
                new CheckInventoryExistenceResult.Entry("SKU-001", true, new CommonId("uuid-inv-1")),
                new CheckInventoryExistenceResult.Entry("SKU-002", false, null)
        );
        verify(inventoryQueryPort).findExistenceByMerchantLocationAndSkus(
                "seller-1", "loc-1", List.of("SKU-001", "SKU-002"));
    }
}
