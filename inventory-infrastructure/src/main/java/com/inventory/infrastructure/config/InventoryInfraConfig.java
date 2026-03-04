package com.inventory.infrastructure.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.repository.InventoryReservationRepository;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.domain.repository.StockMovementRepository;
import com.inventory.infrastructure.mapper.jpa.*;
import com.inventory.infrastructure.mapper.jpa.impl.InventoryJpaAssemblerImpl;
import com.inventory.infrastructure.repository.jpa.InventoryItemJpaRepository;
import com.inventory.infrastructure.repository.jpa.InventoryReservationJpaRepository;
import com.inventory.infrastructure.repository.jpa.LocationJpaRepository;
import com.inventory.infrastructure.repository.jpa.StockMovementJpaRepository;
import com.inventory.infrastructure.repository.jpa.impl.DefaultInventoryRepository;
import com.inventory.infrastructure.repository.jpa.impl.DefaultInventoryReservationRepository;
import com.inventory.infrastructure.repository.jpa.impl.DefaultLocationRepository;
import com.inventory.infrastructure.repository.jpa.impl.DefaultStockMovementRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(InventoryDomainConfig.class)
public class InventoryInfraConfig {

    @Bean
    public InventoryJpaAssembler inventoryJpaAssembler(InventoryItemEntityMapper inventoryItemEntityMapper,
                                                       InventoryItemMapper inventoryItemMapper) {
        return new InventoryJpaAssemblerImpl(
                inventoryItemEntityMapper,
                inventoryItemMapper
        );
    }

    @Bean
    public InventoryRepository inventoryRepository(InventoryItemJpaRepository jpaRepository,
                                                   InventoryJpaAssembler mapper,
                                                   DomainEventProducer domainEventProducer) {
        return new DefaultInventoryRepository(
                jpaRepository,
                mapper,
                domainEventProducer
        );
    }

    @Bean
    public InventoryReservationRepository inventoryReservationRepository(InventoryReservationJpaRepository jpaRepository,
                                                                         IdGenerator idGenerator) {
        return new DefaultInventoryReservationRepository(jpaRepository, idGenerator);
    }

    @Bean
    public LocationRepository locationRepository(LocationJpaRepository jpaRepository,
                                                 LocationMapper locationMapper,
                                                 ZoneMapper zoneMapper,
                                                 BinMapper binMapper) {
        return new DefaultLocationRepository(jpaRepository, locationMapper, zoneMapper, binMapper);
    }

    @Bean
    public StockMovementRepository stockMovementRepository(StockMovementJpaRepository jpaRepository,
                                                           StockMovementEntityMapper entityMapper,
                                                           StockMovementMapper domainMapper) {
        return new DefaultStockMovementRepository(jpaRepository, entityMapper, domainMapper);
    }
}
