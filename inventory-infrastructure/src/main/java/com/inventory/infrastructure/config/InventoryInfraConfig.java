package com.inventory.infrastructure.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.outbox.JsonOutboxEventSerializer;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.support.PersistenceExecutor;
import com.grab.outbox.infrastructure.jpa.JpaOutboxStore;
import com.grab.outbox.infrastructure.OutboxStore;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.repository.InventoryReservationRepository;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.domain.repository.StockMovementRepository;
import com.inventory.infrastructure.mapper.jpa.*;
import com.inventory.infrastructure.mapper.jpa.impl.InventoryJpaAssemblerImpl;
import com.inventory.infrastructure.mapper.jpa.impl.LocationJpaAssemblerImpl;
import com.inventory.infrastructure.mapper.jpa.impl.InventoryReservationJpaAssemblerImpl;
import com.inventory.infrastructure.mapper.jpa.impl.StockMovementJpaAssemblerImpl;
import com.inventory.infrastructure.repository.jpa.InventoryItemJpaRepository;
import com.inventory.infrastructure.repository.jpa.InventoryReservationJpaRepository;
import com.inventory.infrastructure.repository.jpa.LocationJpaRepository;
import com.inventory.infrastructure.repository.jpa.StockMovementJpaRepository;
import com.inventory.infrastructure.outbox.InventoryOutboxEvent;
import com.inventory.infrastructure.outbox.InventoryOutboxEventProcessor;
import com.inventory.infrastructure.outbox.InventoryOutboxEventProducer;
import com.inventory.infrastructure.repository.jpa.impl.DefaultInventoryRepository;
import com.inventory.infrastructure.repository.jpa.impl.DefaultInventoryReservationRepository;
import com.inventory.infrastructure.repository.jpa.impl.DefaultLocationRepository;
import com.inventory.infrastructure.repository.jpa.impl.DefaultStockMovementRepository;
import com.inventory.infrastructure.repository.jpa.support.InventoryPersistenceExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;

@Configuration
@Import(InventoryDomainConfig.class)
public class InventoryInfraConfig {

    @Bean("inventoryOutboxEventSerializer")
    public OutboxEventSerializer inventoryOutboxEventSerializer() {
        return new JsonOutboxEventSerializer();
    }

    @Bean("inventoryOutboxEventDispatcher")
    public OutboxEventDispatcher inventoryOutboxEventDispatcher(ApplicationEventPublisher applicationEventPublisher) {
        return applicationEventPublisher::publishEvent;
    }

    @Bean("inventoryDomainEventProducer")
    public DomainEventProducer inventoryDomainEventProducer(
            @Qualifier("inventoryOutboxStore") OutboxStore<InventoryOutboxEvent, Long> outboxStore,
            @Qualifier("inventoryOutboxEventSerializer") OutboxEventSerializer serializer
    ) {
        return new InventoryOutboxEventProducer(outboxStore, serializer);
    }

    @Bean("inventoryOutboxStore")
    public OutboxStore<InventoryOutboxEvent, Long> inventoryOutboxStore(JpaContext context) {
        return new JpaOutboxStore<>(
                context.getEntityManagerByManagedType(InventoryOutboxEvent.class),
                InventoryOutboxEvent.class
        );
    }

    @Bean
    public InventoryOutboxEventProcessor inventoryOutboxEventProcessor(
            @Qualifier("inventoryOutboxStore") OutboxStore<InventoryOutboxEvent, Long> outboxStore,
            @Qualifier("inventoryOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("inventoryOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("inventoryTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${inventory.outbox.batch-size:20}") int batchSize,
            @Value("${inventory.outbox.retry-delay-ms:30000}") long retryDelayMs,
            @Value("${inventory.outbox.claim-timeout-ms:120000}") long claimTimeoutMs,
            @Value("${inventory.outbox.retention-ms:604800000}") long retentionMs
    ) {
        return new InventoryOutboxEventProcessor(
                outboxStore,
                serializer,
                dispatcher,
                transactionManager,
                batchSize,
                Duration.ofMillis(retryDelayMs),
                Duration.ofMillis(claimTimeoutMs),
                Duration.ofMillis(retentionMs)
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
    public InventoryReservationJpaAssembler inventoryReservationJpaAssembler(
            InventoryReservationEntityMapper inventoryReservationEntityMapper,
            InventoryReservationMapper inventoryReservationMapper
    ) {
        return new InventoryReservationJpaAssemblerImpl(
                inventoryReservationEntityMapper,
                inventoryReservationMapper
        );
    }

    @Bean
    public LocationJpaAssembler locationJpaAssembler(
            LocationEntityMapper locationEntityMapper,
            ZoneEntityMapper zoneEntityMapper,
            BinEntityMapper binEntityMapper,
            LocationMapper locationMapper,
            ZoneMapper zoneMapper,
            BinMapper binMapper
    ) {
        return new LocationJpaAssemblerImpl(
                locationEntityMapper,
                zoneEntityMapper,
                binEntityMapper,
                locationMapper,
                zoneMapper,
                binMapper
        );
    }

    @Bean
    public StockMovementJpaAssembler stockMovementJpaAssembler(
            StockMovementEntityMapper stockMovementEntityMapper,
            StockMovementMapper stockMovementMapper
    ) {
        return new StockMovementJpaAssemblerImpl(
                stockMovementEntityMapper,
                stockMovementMapper
        );
    }

    @Bean("inventoryPersistenceExecutor")
    public PersistenceExecutor inventoryPersistenceExecutor() {
        return new InventoryPersistenceExecutor();
    }

    @Bean
    public InventoryRepository inventoryRepository(InventoryItemJpaRepository jpaRepository,
                                                   InventoryJpaAssembler mapper,
                                                   @Qualifier("inventoryDomainEventProducer") DomainEventProducer domainEventProducer,
                                                   @Qualifier("inventoryPersistenceExecutor") PersistenceExecutor executor) {
        return new DefaultInventoryRepository(
                jpaRepository,
                mapper,
                domainEventProducer,
                executor
        );
    }

    @Bean
    public InventoryReservationRepository inventoryReservationRepository(InventoryReservationJpaRepository jpaRepository,
                                                                         InventoryReservationJpaAssembler mapper,
                                                                         @Qualifier("inventoryPersistenceExecutor") PersistenceExecutor executor) {
        return new DefaultInventoryReservationRepository(jpaRepository, mapper, executor);
    }

    @Bean
    public LocationRepository locationRepository(LocationJpaRepository jpaRepository,
                                                 LocationJpaAssembler mapper,
                                                 @Qualifier("inventoryPersistenceExecutor") PersistenceExecutor executor) {
        return new DefaultLocationRepository(jpaRepository, mapper, executor);
    }

    @Bean
    public StockMovementRepository stockMovementRepository(StockMovementJpaRepository jpaRepository,
                                                           StockMovementJpaAssembler mapper,
                                                           @Qualifier("inventoryPersistenceExecutor") PersistenceExecutor executor){
        return new DefaultStockMovementRepository(jpaRepository, mapper, executor);
    }
}
