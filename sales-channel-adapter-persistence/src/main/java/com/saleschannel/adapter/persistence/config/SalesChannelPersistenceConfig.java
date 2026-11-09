package com.saleschannel.adapter.persistence.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.mapper.IdMapper;
import com.grab.framework.id.IdGenerator;
import com.saleschannel.adapter.persistence.adapter.PublishableKeyQueryAdapter;
import com.saleschannel.adapter.persistence.adapter.PublishableKeyRepositoryAdapter;
import com.saleschannel.adapter.persistence.entity.PublishableKeyJpaEntity;
import com.saleschannel.adapter.persistence.mapper.PublishableKeyEntityMapper;
import com.saleschannel.adapter.persistence.repository.jpa.PublishableKeyJpaRepository;
import com.saleschannel.adapter.persistence.specification.jpa.PublishableKeyQuerySpecification;
import com.saleschannel.application.port.outbound.PublishableKeyQueryPort;
import com.saleschannel.domain.port.outbound.PublishableKeyRepository;
import com.grab.framework.support.PersistenceExecutor;
import com.saleschannel.application.port.outbound.SalesChannelQueryPort;
import com.saleschannel.application.port.outbound.ChannelRegionLookupPort;
import com.saleschannel.domain.port.outbound.SalesChannelRepository;
import com.saleschannel.adapter.persistence.adapter.SalesChannelPersistenceExecutor;
import com.saleschannel.adapter.persistence.adapter.SalesChannelQueryAdapter;
import com.saleschannel.adapter.persistence.adapter.ChannelRegionQueryAdapter;
import com.saleschannel.adapter.persistence.adapter.SalesChannelRepositoryAdapter;
import com.saleschannel.adapter.persistence.entity.SalesChannelEntity;
import com.saleschannel.adapter.persistence.mapper.SalesChannelEntityMapper;
import com.saleschannel.adapter.persistence.mapper.impl.SalesChannelJpaAssembler;
import com.saleschannel.adapter.persistence.repository.SalesChannelJpaRepository;
import com.saleschannel.adapter.persistence.repository.ChannelRegionJpaRepository;
import com.saleschannel.adapter.persistence.specification.SalesChannelQuerySpecification;
import com.saleschannel.adapter.persistence.workflow.SalesChannelWorkflowStepRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.transaction.PlatformTransactionManager;
import jakarta.persistence.EntityManager;
import java.time.Duration;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.grab.framework.security.SecurityManifestPublicationQueryPort;
import com.manifest.adapter.persistence.SecurityManifestEventFactory;
import com.saleschannel.adapter.persistence.adapter.SalesChannelSecurityManifestPublicationAdapter;
import com.saleschannel.adapter.persistence.adapter.SalesChannelSecurityManifestPublicationQueryAdapter;
import com.saleschannel.adapter.persistence.entity.SalesChannelSecurityManifestPublicationEntity;

@Configuration
public class SalesChannelPersistenceConfig {
    @Bean
    PublishableKeyRepository publishableKeyRepository(
            PublishableKeyJpaRepository keys,
            PublishableKeyEntityMapper mapper,
            @Qualifier("salesChannelDomainEventProducer") DomainEventProducer events,
            @Qualifier("salesChannelPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new PublishableKeyRepositoryAdapter(keys, mapper, events, executor);
    }

    @Bean
    PublishableKeyQuerySpecification publishableKeyQuerySpecification(JpaContext context) {
        EntityManager entityManager = context.getEntityManagerByManagedType(PublishableKeyJpaEntity.class);
        return new PublishableKeyQuerySpecification(entityManager);
    }

    @Bean
    PublishableKeyQueryPort publishableKeyQueryPort(
            PublishableKeyQuerySpecification specification,
            @Qualifier("salesChannelPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new PublishableKeyQueryAdapter(specification, executor);
    }

    @Bean("salesChannelPersistenceExecutor")
    PersistenceExecutor persistenceExecutor() {
        return new SalesChannelPersistenceExecutor();
    }

    @Bean
    public SalesChannelWorkflowStepRunner salesChannelWorkflowSignalEmitter(
            @Qualifier("salesChannelDomainEventProducer") DomainEventProducer producer,
            @Qualifier("salesChannelTransactionManager") PlatformTransactionManager transactionManager
    ) {
        return new SalesChannelWorkflowStepRunner(producer, transactionManager);
    }

    @Bean
    SalesChannelJpaAssembler salesChannelAssembler(SalesChannelEntityMapper entityMapper, IdMapper ids) {
        return new SalesChannelJpaAssembler(entityMapper, ids);
    }

    @Bean
    SalesChannelRepository salesChannelRepository(
            SalesChannelJpaRepository salesChannels,
            SalesChannelJpaAssembler assembler,
            @Qualifier("salesChannelDomainEventProducer") DomainEventProducer events,
            @Qualifier("salesChannelPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new SalesChannelRepositoryAdapter(salesChannels, assembler, events, executor);
    }

    @Bean
    ChannelRegionLookupPort channelRegionLookupPort(
            ChannelRegionJpaRepository channelRegions,
            IdGenerator ids,
            @Qualifier("salesChannelPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new ChannelRegionQueryAdapter(channelRegions, ids, executor);
    }

    @Bean
    SalesChannelQuerySpecification salesChannelQuerySpecification(JpaContext context) {
        return new SalesChannelQuerySpecification(
                context.getEntityManagerByManagedType(SalesChannelEntity.class)
        );
    }

    @Bean
    SalesChannelQueryPort salesChannelQueryPort(
            SalesChannelQuerySpecification specification,
            @Qualifier("salesChannelPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new SalesChannelQueryAdapter(specification, executor);
    }

    @Bean("saleschannelSecurityManifestPublicationQueryPort")
    public SecurityManifestPublicationQueryPort saleschannelSecurityManifestPublicationQueryPort(JpaContext context) {
        EntityManager entityManager = context.getEntityManagerByManagedType(SalesChannelSecurityManifestPublicationEntity.class);
        return new SalesChannelSecurityManifestPublicationQueryAdapter(entityManager);
    }

    @Bean("saleschannelSecurityManifestPublicationPort")
    public SecurityManifestPublicationPort saleschannelSecurityManifestPublicationPort(
            JpaContext context,
            @Qualifier("salesChannelDomainEventProducer") DomainEventProducer outbox,
            @Qualifier("saleschannelSecurityManifestEventFactory") SecurityManifestEventFactory eventFactory,
            @Value("${security.manifest.republish.fixed-delay-ms:300000}") long intervalMs) {
        return new SalesChannelSecurityManifestPublicationAdapter(
                context,
                outbox,
                eventFactory,
                Duration.ofMillis(intervalMs)
        );
    }
}
