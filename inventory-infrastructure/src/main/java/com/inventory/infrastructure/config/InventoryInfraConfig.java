package com.inventory.infrastructure.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.outbox.JavaSerializationOutboxEventSerializer;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.repository.InventoryReservationRepository;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.domain.repository.StockMovementRepository;
import com.inventory.infrastructure.mapper.jpa.*;
import com.inventory.infrastructure.mapper.jpa.impl.InventoryJpaAssemblerImpl;
import com.inventory.infrastructure.repository.jpa.InventoryItemJpaRepository;
import com.inventory.infrastructure.repository.jpa.InventoryOutboxEventJpaRepository;
import com.inventory.infrastructure.repository.jpa.InventoryReservationJpaRepository;
import com.inventory.infrastructure.repository.jpa.LocationJpaRepository;
import com.inventory.infrastructure.repository.jpa.StockMovementJpaRepository;
import com.inventory.infrastructure.outbox.InventoryOutboxEventProcessor;
import com.inventory.infrastructure.outbox.InventoryOutboxEventProducer;
import com.inventory.infrastructure.repository.jpa.impl.DefaultInventoryRepository;
import com.inventory.infrastructure.repository.jpa.impl.DefaultInventoryReservationRepository;
import com.inventory.infrastructure.repository.jpa.impl.DefaultLocationRepository;
import com.inventory.infrastructure.repository.jpa.impl.DefaultStockMovementRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;

@Configuration
@Import(InventoryDomainConfig.class)
public class InventoryInfraConfig {

    @Bean("inventoryOutboxEventSerializer")
    public OutboxEventSerializer inventoryOutboxEventSerializer() {
        return new JavaSerializationOutboxEventSerializer();
    }

    @Bean("inventoryOutboxEventDispatcher")
    public OutboxEventDispatcher inventoryOutboxEventDispatcher(ApplicationEventPublisher applicationEventPublisher) {
        return applicationEventPublisher::publishEvent;
    }

    @Bean("inventoryDomainEventProducer")
    public DomainEventProducer inventoryDomainEventProducer(
            InventoryOutboxEventJpaRepository inventoryOutboxEventJpaRepository,
            @Qualifier("inventoryOutboxEventSerializer") OutboxEventSerializer serializer
    ) {
        return new InventoryOutboxEventProducer(inventoryOutboxEventJpaRepository, serializer);
    }

    @Bean
    public InventoryOutboxEventProcessor inventoryOutboxEventProcessor(
            InventoryOutboxEventJpaRepository inventoryOutboxEventJpaRepository,
            @Qualifier("inventoryOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("inventoryOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("inventoryTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${inventory.outbox.batch-size:20}") int batchSize,
            @Value("${inventory.outbox.retry-delay-ms:30000}") long retryDelayMs,
            @Value("${inventory.outbox.claim-timeout-ms:120000}") long claimTimeoutMs
    ) {
        return new InventoryOutboxEventProcessor(
                inventoryOutboxEventJpaRepository,
                serializer,
                dispatcher,
                transactionManager,
                batchSize,
                Duration.ofMillis(retryDelayMs),
                Duration.ofMillis(claimTimeoutMs)
        );
    }

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
                                                   @Qualifier("inventoryDomainEventProducer") DomainEventProducer domainEventProducer) {
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
