package com.inventory.adapter.persistence.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.outbox.JsonOutboxEventSerializer;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.framework.support.PersistenceExecutor;
import com.grab.outbox.infrastructure.jpa.JpaOutboxStore;
import com.grab.outbox.infrastructure.OutboxRelays;
import com.grab.outbox.infrastructure.OutboxStore;
import com.inventory.adapter.persistence.mapper.jpa.*;
import com.inventory.adapter.persistence.mapper.jpa.impl.BinJpaAssemblerImpl;
import com.inventory.adapter.persistence.mapper.jpa.impl.ChannelFulfillmentRouteJpaAssemblerImpl;
import com.inventory.adapter.persistence.mapper.jpa.impl.InventoryJpaAssemblerImpl;
import com.inventory.adapter.persistence.mapper.jpa.impl.InventoryReservationJpaAssemblerImpl;
import com.inventory.adapter.persistence.mapper.jpa.impl.LocationJpaAssemblerImpl;
import com.inventory.adapter.persistence.mapper.jpa.impl.StockMovementJpaAssemblerImpl;
import com.inventory.adapter.persistence.mapper.jpa.impl.ZoneJpaAssemblerImpl;
import com.inventory.adapter.persistence.adapter.ProductVariantViewQueryAdapter;
import com.inventory.adapter.persistence.repository.jpa.ProductVariantViewJpaRepository;
import com.inventory.application.port.outbound.ProductVariantViewQueryPort;
import com.inventory.adapter.persistence.repository.jpa.BinJpaRepository;
import com.inventory.adapter.persistence.repository.jpa.ChannelFulfillmentRouteJpaRepository;
import com.inventory.adapter.persistence.repository.jpa.InventoryItemJpaRepository;
import com.inventory.adapter.persistence.repository.jpa.InventoryReservationJpaRepository;
import com.inventory.adapter.persistence.repository.jpa.LocationJpaRepository;
import com.inventory.adapter.persistence.repository.jpa.StockMovementJpaRepository;
import com.inventory.adapter.persistence.repository.jpa.ZoneJpaRepository;
import com.inventory.adapter.persistence.outbox.InventoryOutboxEvent;
import com.inventory.adapter.persistence.outbox.InventoryOutboxEventProcessor;
import com.inventory.adapter.persistence.outbox.InventoryOutboxEventProducer;
import com.inventory.domain.port.outbound.BinRepository;
import com.inventory.domain.port.outbound.ChannelFulfillmentRouteRepository;
import com.inventory.domain.port.outbound.InventoryRepository;
import com.inventory.domain.port.outbound.InventoryReservationRepository;
import com.inventory.domain.port.outbound.LocationRepository;
import com.inventory.domain.port.outbound.StockMovementRepository;
import com.inventory.domain.port.outbound.ZoneRepository;
import com.inventory.adapter.persistence.adapter.*;
import com.inventory.adapter.persistence.entity.BinEntity;
import com.inventory.adapter.persistence.entity.InventoryItemEntity;
import com.inventory.adapter.persistence.entity.LocationEntity;
import com.inventory.adapter.persistence.entity.ZoneEntity;
import com.inventory.adapter.persistence.specification.jpa.BinSearchSpecification;
import com.inventory.adapter.persistence.specification.jpa.InventorySearchSpecification;
import com.inventory.adapter.persistence.specification.jpa.InventorySummarySpecification;
import com.inventory.adapter.persistence.specification.jpa.LocationSearchSpecification;
import com.inventory.adapter.persistence.specification.jpa.ZoneSearchSpecification;
import com.inventory.application.port.outbound.BinQueryPort;
import com.inventory.application.port.outbound.InventoryQueryPort;
import com.inventory.application.port.outbound.InventoryReservationQueryPort;
import com.inventory.application.port.outbound.LocationQueryPort;
import com.inventory.application.port.outbound.StockMovementQueryPort;
import com.inventory.application.port.outbound.ZoneQueryPort;
import com.inventory.adapter.persistence.workflow.InventoryWorkflowStepRunner;
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
public class InventoryPersistenceConfig {

    @Bean("inventoryOutboxEventSerializer")
    public OutboxEventSerializer inventoryOutboxEventSerializer() {
        return new JsonOutboxEventSerializer();
    }

    @Bean("inventoryOutboxEventDispatcher")
    public OutboxEventDispatcher inventoryOutboxEventDispatcher(ApplicationEventPublisher applicationEventPublisher) {
        return applicationEventPublisher::publishEvent;
    }

    @Bean("inventoryOutboxRelay")
    public OutboxRelay<Long> inventoryOutboxRelay(
            @Value("${inventory.outbox.hot-queue.enabled:true}") boolean enabled,
            @Value("${inventory.outbox.hot-queue.workers:2}") int workers,
            @Value("${inventory.outbox.hot-queue.capacity:1000}") int capacity,
            @Value("${inventory.outbox.hot-queue.drain-timeout-ms:5000}") long drainTimeoutMs
    ) {
        return OutboxRelays.moduleRelay("inventory", enabled, workers, capacity, drainTimeoutMs);
    }

    @Bean("inventoryDomainEventProducer")
    public DomainEventProducer inventoryDomainEventProducer(
            @Qualifier("inventoryOutboxStore") OutboxStore<InventoryOutboxEvent, Long> outboxStore,
            @Qualifier("inventoryOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("inventoryOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new InventoryOutboxEventProducer(outboxStore, serializer, relay);
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
            @Value("${inventory.outbox.retention-ms:604800000}") long retentionMs,
            @Qualifier("inventoryOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new InventoryOutboxEventProcessor(
                outboxStore,
                serializer,
                dispatcher,
                transactionManager,
                batchSize,
                Duration.ofMillis(retryDelayMs),
                Duration.ofMillis(claimTimeoutMs),
                Duration.ofMillis(retentionMs),
                relay
        );
    }

    @Bean
    public InventoryWorkflowStepRunner inventoryWorkflowSignalEmitter(
            @Qualifier("inventoryDomainEventProducer") DomainEventProducer producer,
            @Qualifier("inventoryTransactionManager") PlatformTransactionManager transactionManager
    ) {
        return new InventoryWorkflowStepRunner(producer, transactionManager);
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
    public ChannelFulfillmentRouteJpaAssembler channelFulfillmentRouteJpaAssembler(
            ChannelFulfillmentRouteEntityMapper channelFulfillmentRouteEntityMapper,
            ChannelFulfillmentRouteMapper channelFulfillmentRouteMapper
    ) {
        return new ChannelFulfillmentRouteJpaAssemblerImpl(
                channelFulfillmentRouteEntityMapper,
                channelFulfillmentRouteMapper
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
    public InventoryRepository inventoryRepository(InventoryItemJpaRepository jpaRepository,
                                                   InventoryJpaAssembler mapper,
                                                   @Qualifier("inventoryDomainEventProducer") DomainEventProducer domainEventProducer,
                                                   @Qualifier("inventoryPersistenceExecutor") PersistenceExecutor executor) {
        return new InventoryRepositoryAdapter(
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
        return new InventoryReservationRepositoryAdapter(jpaRepository, mapper, executor);
    }

    @Bean
    public LocationRepository locationRepository(LocationJpaRepository jpaRepository,
                                                 LocationJpaAssembler mapper,
                                                 @Qualifier("inventoryDomainEventProducer") DomainEventProducer domainEventProducer,
                                                 @Qualifier("inventoryPersistenceExecutor") PersistenceExecutor executor) {
        return new LocationRepositoryAdapter(jpaRepository, mapper, domainEventProducer, executor);
    }

    @Bean
    public ChannelFulfillmentRouteRepository channelFulfillmentRouteRepository(
            ChannelFulfillmentRouteJpaRepository jpaRepository,
            LocationJpaRepository locationJpaRepository,
            ChannelFulfillmentRouteJpaAssembler mapper,
            @Qualifier("inventoryDomainEventProducer") DomainEventProducer domainEventProducer,
            @Qualifier("inventoryPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new ChannelFulfillmentRouteRepositoryAdapter(
                jpaRepository,
                locationJpaRepository,
                mapper,
                domainEventProducer,
                executor
        );
    }

    @Bean
    public ZoneRepository zoneRepository(ZoneJpaRepository jpaRepository,
                                         ZoneJpaAssembler mapper,
                                         @Qualifier("inventoryDomainEventProducer") DomainEventProducer domainEventProducer,
                                         @Qualifier("inventoryPersistenceExecutor") PersistenceExecutor executor) {
        return new ZoneRepositoryAdapter(jpaRepository, mapper, domainEventProducer, executor);
    }

    @Bean
    public BinRepository binRepository(BinJpaRepository jpaRepository,
                                       BinJpaAssembler mapper,
                                       @Qualifier("inventoryDomainEventProducer") DomainEventProducer domainEventProducer,
                                       @Qualifier("inventoryPersistenceExecutor") PersistenceExecutor executor) {
        return new BinRepositoryAdapter(jpaRepository, mapper, domainEventProducer, executor);
    }

    @Bean
    public StockMovementRepository stockMovementRepository(StockMovementJpaRepository jpaRepository,
                                                           StockMovementJpaAssembler mapper,
                                                           @Qualifier("inventoryPersistenceExecutor") PersistenceExecutor executor){
        return new StockMovementRepositoryAdapter(jpaRepository, mapper, executor);
    }

    @Bean
    public ProductVariantViewQueryPort productVariantViewQueryPort(
            ProductVariantViewJpaRepository productVariantViewJpaRepository
    ) {
        return new ProductVariantViewQueryAdapter(productVariantViewJpaRepository);
    }

    @Bean
    public InventorySearchSpecification inventorySearchSpecification(JpaContext context) {
        return new InventorySearchSpecification(context.getEntityManagerByManagedType(InventoryItemEntity.class));
    }

    @Bean
    public InventorySummarySpecification inventorySummarySpecification(JpaContext context) {
        return new InventorySummarySpecification(context.getEntityManagerByManagedType(InventoryItemEntity.class));
    }

    @Bean
    public InventoryQueryPort inventoryQueryPort(
            InventorySearchSpecification inventorySearchSpecification,
            InventorySummarySpecification inventorySummarySpecification,
            InventoryItemJpaRepository inventoryItemJpaRepository,
            LocationJpaRepository locationJpaRepository,
            ChannelFulfillmentRouteJpaRepository channelFulfillmentRouteJpaRepository,
            @Qualifier("inventoryPersistenceExecutor") PersistenceExecutor executor) {
        return new InventoryQueryAdapter(
                inventorySearchSpecification,
                inventorySummarySpecification,
                inventoryItemJpaRepository,
                locationJpaRepository,
                channelFulfillmentRouteJpaRepository,
                executor
        );
    }

    @Bean
    public InventoryReservationQueryPort inventoryReservationQueryPort(InventoryReservationJpaRepository jpaRepository,
                                                                          @Qualifier("inventoryPersistenceExecutor") PersistenceExecutor executor) {
        return new InventoryReservationQueryAdapter(jpaRepository, executor);
    }

    @Bean
    public LocationSearchSpecification locationSearchSpecification(JpaContext context) {
        return new LocationSearchSpecification(context.getEntityManagerByManagedType(LocationEntity.class));
    }

    @Bean
    public ZoneSearchSpecification zoneSearchSpecification(JpaContext context) {
        return new ZoneSearchSpecification(context.getEntityManagerByManagedType(ZoneEntity.class));
    }

    @Bean
    public BinSearchSpecification binSearchSpecification(JpaContext context) {
        return new BinSearchSpecification(context.getEntityManagerByManagedType(BinEntity.class));
    }

    @Bean
    public LocationQueryPort locationQueryPort(LocationJpaRepository jpaRepository,
                                                   LocationSearchSpecification locationSearchSpecification,
                                                   @Qualifier("inventoryPersistenceExecutor") PersistenceExecutor executor) {
        return new LocationQueryAdapter(jpaRepository, locationSearchSpecification, executor);
    }

    @Bean
    public ZoneQueryPort zoneQueryPort(ZoneJpaRepository jpaRepository,
                                           ZoneSearchSpecification zoneSearchSpecification,
                                           @Qualifier("inventoryPersistenceExecutor") PersistenceExecutor executor) {
        return new ZoneQueryAdapter(jpaRepository, zoneSearchSpecification, executor);
    }

    @Bean
    public BinQueryPort binQueryPort(BinJpaRepository jpaRepository,
                                         BinSearchSpecification binSearchSpecification,
                                         @Qualifier("inventoryPersistenceExecutor") PersistenceExecutor executor) {
        return new BinQueryAdapter(jpaRepository, binSearchSpecification, executor);
    }

    @Bean
    public StockMovementQueryPort stockMovementQueryPort(StockMovementJpaRepository jpaRepository,
                                                             @Qualifier("inventoryPersistenceExecutor") PersistenceExecutor executor){
        return new StockMovementQueryAdapter(jpaRepository, executor);
    }
}
