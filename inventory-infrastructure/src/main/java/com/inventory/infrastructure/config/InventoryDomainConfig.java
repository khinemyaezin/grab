package com.inventory.infrastructure.config;

import com.grab.framework.id.IdGenerator;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.repository.StockMovementRepository;
import com.inventory.domain.service.InventoryAllocationService;
import com.inventory.domain.service.ReorderService;
import com.inventory.domain.service.impl.DefaultInventoryAllocationService;
import com.inventory.domain.service.impl.DefaultReorderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InventoryDomainConfig {
    @Bean
    public InventoryAllocationService inventoryAllocationService(
            InventoryRepository inventoryRepository,
            StockMovementRepository stockMovementRepository,
            IdGenerator idGenerator
    ) {
        return new DefaultInventoryAllocationService(
                inventoryRepository,
                stockMovementRepository,
                idGenerator
        );
    }

    @Bean
    public ReorderService reorderService(InventoryRepository inventoryRepository) {
        return new DefaultReorderService(inventoryRepository);
    }
}
