package com.inventory.infrastructure.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.outbox.JsonOutboxEventSerializer;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.support.PersistenceExecutor;
import com.grab.outbox.infrastructure.jpa.JpaOutboxStore;
import com.grab.outbox.infrastructure.OutboxStore;
import com.inventory.infrastructure.mapper.jpa.*;
import com.inventory.infrastructure.mapper.jpa.impl.BinJpaAssemblerImpl;
import com.inventory.infrastructure.mapper.jpa.impl.InventoryJpaAssemblerImpl;
import com.inventory.infrastructure.mapper.jpa.impl.InventoryReservationJpaAssemblerImpl;
import com.inventory.infrastructure.mapper.jpa.impl.LocationJpaAssemblerImpl;
import com.inventory.infrastructure.mapper.jpa.impl.StockMovementJpaAssemblerImpl;
import com.inventory.infrastructure.mapper.jpa.impl.ZoneJpaAssemblerImpl;
import com.inventory.infrastructure.repository.jpa.BinJpaRepository;
import com.inventory.infrastructure.repository.jpa.InventoryItemJpaRepository;
import com.inventory.infrastructure.repository.jpa.InventoryReservationJpaRepository;
import com.inventory.infrastructure.repository.jpa.LocationJpaRepository;
import com.inventory.infrastructure.repository.jpa.StockMovementJpaRepository;
import com.inventory.infrastructure.repository.jpa.ZoneJpaRepository;
import com.inventory.infrastructure.outbox.InventoryOutboxEvent;
import com.inventory.infrastructure.outbox.InventoryOutboxEventProcessor;
import com.inventory.infrastructure.outbox.InventoryOutboxEventProducer;
import com.inventory.infrastructure.repository.jpa.impl.DefaultBinRepository;
import com.inventory.infrastructure.repository.jpa.impl.DefaultInventoryRepository;
import com.inventory.infrastructure.repository.jpa.impl.DefaultInventoryReservationRepository;
import com.inventory.infrastructure.repository.jpa.impl.DefaultLocationRepository;
import com.inventory.infrastructure.repository.jpa.impl.DefaultStockMovementRepository;
import com.inventory.infrastructure.repository.jpa.impl.DefaultZoneRepository;
import com.inventory.infrastructure.repository.jpa.impl.DefaultBinQueryRepository;
import com.inventory.infrastructure.repository.jpa.impl.DefaultInventoryQueryRepository;
import com.inventory.infrastructure.repository.jpa.impl.DefaultInventoryReservationQueryRepository;
import com.inventory.infrastructure.repository.jpa.impl.DefaultLocationQueryRepository;
import com.inventory.infrastructure.repository.jpa.impl.DefaultStockMovementQueryRepository;
import com.inventory.infrastructure.repository.jpa.impl.DefaultZoneQueryRepository;
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
            LocationMapper locationMapper
    ) {
        return new LocationJpaAssemblerImpl(
                locationEntityMapper,
                locationMapper
        );
    }

    @Bean
    public ZoneJpaAssembler zoneJpaAssembler(
            ZoneEntityMapper zoneEntityMapper,
            ZoneMapper zoneMapper
    ) {
        return new ZoneJpaAssemblerImpl(
                zoneEntityMapper,
                zoneMapper
        );
    }

    @Bean
    public BinJpaAssembler binJpaAssembler(
            BinEntityMapper binEntityMapper,
            BinMapper binMapper
    ) {
        return new BinJpaAssemblerImpl(
                binEntityMapper,
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
    public DefaultInventoryRepository inventoryRepository(InventoryItemJpaRepository jpaRepository,
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
    public DefaultInventoryReservationRepository inventoryReservationRepository(InventoryReservationJpaRepository jpaRepository,
                                                                         InventoryReservationJpaAssembler mapper,
                                                                         @Qualifier("inventoryPersistenceExecutor") PersistenceExecutor executor) {
        return new DefaultInventoryReservationRepository(jpaRepository, mapper, executor);
    }

    @Bean
    public DefaultLocationRepository locationRepository(LocationJpaRepository jpaRepository,
                                                 LocationJpaAssembler mapper,
                                                 @Qualifier("inventoryDomainEventProducer") DomainEventProducer domainEventProducer,
                                                 @Qualifier("inventoryPersistenceExecutor") PersistenceExecutor executor) {
        return new DefaultLocationRepository(jpaRepository, mapper, domainEventProducer, executor);
    }

    @Bean
    public DefaultZoneRepository zoneRepository(ZoneJpaRepository jpaRepository,
                                         ZoneJpaAssembler mapper,
                                         @Qualifier("inventoryDomainEventProducer") DomainEventProducer domainEventProducer,
                                         @Qualifier("inventoryPersistenceExecutor") PersistenceExecutor executor) {
        return new DefaultZoneRepository(jpaRepository, mapper, domainEventProducer, executor);
    }

    @Bean
    public DefaultBinRepository binRepository(BinJpaRepository jpaRepository,
                                       BinJpaAssembler mapper,
                                       @Qualifier("inventoryDomainEventProducer") DomainEventProducer domainEventProducer,
                                       @Qualifier("inventoryPersistenceExecutor") PersistenceExecutor executor) {
        return new DefaultBinRepository(jpaRepository, mapper, domainEventProducer, executor);
    }

    @Bean
    public DefaultStockMovementRepository stockMovementRepository(StockMovementJpaRepository jpaRepository,
                                                           StockMovementJpaAssembler mapper,
                                                           @Qualifier("inventoryPersistenceExecutor") PersistenceExecutor executor){
        return new DefaultStockMovementRepository(jpaRepository, mapper, executor);
    }

    @Bean
    public DefaultInventoryQueryRepository inventoryQueryRepository() {
        return new DefaultInventoryQueryRepository();
    }

    @Bean
    public DefaultInventoryReservationQueryRepository inventoryReservationQueryRepository(InventoryReservationJpaRepository jpaRepository,
                                                                          @Qualifier("inventoryPersistenceExecutor") PersistenceExecutor executor) {
        return new DefaultInventoryReservationQueryRepository(jpaRepository, executor);
    }

    @Bean
    public DefaultLocationQueryRepository locationQueryRepository(LocationJpaRepository jpaRepository,
                                                  @Qualifier("inventoryPersistenceExecutor") PersistenceExecutor executor) {
        return new DefaultLocationQueryRepository(jpaRepository, executor);
    }

    @Bean
    public DefaultZoneQueryRepository zoneQueryRepository(ZoneJpaRepository jpaRepository,
                                          @Qualifier("inventoryPersistenceExecutor") PersistenceExecutor executor) {
        return new DefaultZoneQueryRepository(jpaRepository, executor);
    }

    @Bean
    public DefaultBinQueryRepository binQueryRepository(BinJpaRepository jpaRepository,
                                        @Qualifier("inventoryPersistenceExecutor") PersistenceExecutor executor) {
        return new DefaultBinQueryRepository(jpaRepository, executor);
    }

    @Bean
    public DefaultStockMovementQueryRepository stockMovementQueryRepository(StockMovementJpaRepository jpaRepository,
                                                            @Qualifier("inventoryPersistenceExecutor") PersistenceExecutor executor){
        return new DefaultStockMovementQueryRepository(jpaRepository, executor);
    }
}
