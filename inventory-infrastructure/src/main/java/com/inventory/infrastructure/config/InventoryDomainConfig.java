package com.inventory.infrastructure.config;

import com.grab.framework.id.IdGenerator;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.repository.StockMovementRepository;
import com.inventory.domain.service.InventoryAllocationService;
import com.inventory.domain.service.InventoryStockService;
import com.inventory.domain.service.ReorderService;
import com.inventory.domain.service.impl.DefaultInventoryAllocationService;
import com.inventory.domain.service.impl.DefaultInventoryStockService;
import com.inventory.domain.service.impl.DefaultReorderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InventoryDomainConfig {
    @Bean
    public InventoryAllocationService inventoryAllocationService(
            InventoryRepository inventoryRepository,
            StockMovementRepository stockMovementRepository,
            com.inventory.domain.repository.LocationRepository locationRepository,
            IdGenerator idGenerator
    ) {
        return new DefaultInventoryAllocationService(
                inventoryRepository,
                stockMovementRepository,
                locationRepository,
                idGenerator
        );
    }

    @Bean
    public ReorderService reorderService(InventoryRepository inventoryRepository) {
        return new DefaultReorderService(inventoryRepository);
    }

    @Bean
    public InventoryStockService inventoryStockService(
            InventoryRepository inventoryRepository,
            StockMovementRepository stockMovementRepository,
            IdGenerator idGenerator
    ) {
        return new DefaultInventoryStockService(
                inventoryRepository,
                stockMovementRepository,
                idGenerator
        );
    }
}
