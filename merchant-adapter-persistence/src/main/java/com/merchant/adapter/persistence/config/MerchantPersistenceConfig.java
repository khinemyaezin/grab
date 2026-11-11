package com.merchant.adapter.persistence.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.mapper.IdMapper;
import com.grab.framework.support.PersistenceExecutor;
import com.merchant.domain.port.outbound.MerchantAccountRepository;
import com.merchant.domain.port.outbound.MerchantMemberRepository;
import com.merchant.domain.port.outbound.StorefrontChannelBrandRepository;
import com.merchant.domain.port.outbound.StorefrontRepository;
import com.merchant.adapter.persistence.entity.StorefrontEntity;
import com.merchant.adapter.persistence.mapper.jpa.impl.MerchantAccountJpaAssembler;
import com.merchant.adapter.persistence.mapper.jpa.impl.MerchantMemberJpaAssembler;
import com.merchant.adapter.persistence.mapper.jpa.impl.StorefrontJpaAssembler;
import com.merchant.adapter.persistence.mapper.jpa.MerchantAccountEntityMapper;
import com.merchant.adapter.persistence.mapper.jpa.MerchantMemberEntityMapper;
import com.merchant.adapter.persistence.mapper.jpa.StorefrontChannelBrandEntityMapper;
import com.merchant.adapter.persistence.mapper.jpa.StorefrontChannelBrandJpaAssembler;
import com.merchant.adapter.persistence.mapper.jpa.StorefrontChannelBrandMapper;
import com.merchant.adapter.persistence.mapper.jpa.StorefrontEntityMapper;
import com.merchant.adapter.persistence.mapper.jpa.impl.StorefrontChannelBrandJpaAssemblerImpl;
import com.merchant.adapter.persistence.repository.jpa.MerchantMemberJpaRepository;
import com.merchant.adapter.persistence.repository.jpa.StorefrontChannelBrandJpaRepository;
import com.merchant.adapter.persistence.repository.jpa.StorefrontJpaRepository;
import com.merchant.application.port.outbound.MerchantAccountQueryPort;
import com.merchant.application.port.outbound.MerchantMemberQueryPort;
import com.merchant.application.port.outbound.StorefrontQueryPort;
import com.merchant.adapter.persistence.adapter.MerchantAccountQueryAdapter;
import com.merchant.adapter.persistence.adapter.MerchantAccountRepositoryAdapter;
import com.merchant.adapter.persistence.adapter.MerchantMemberQueryAdapter;
import com.merchant.adapter.persistence.adapter.MerchantMemberRepositoryAdapter;
import com.merchant.adapter.persistence.adapter.MerchantPersistenceExecutor;
import com.merchant.adapter.persistence.adapter.StorefrontChannelBrandRepositoryAdapter;
import com.merchant.adapter.persistence.adapter.StorefrontQueryAdapter;
import com.merchant.adapter.persistence.adapter.StorefrontRepositoryAdapter;
import com.merchant.adapter.persistence.specification.jpa.StorefrontQuerySpecification;
import com.merchant.adapter.persistence.repository.jpa.MerchantAccountJpaRepository;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.grab.framework.security.SecurityManifestPublicationQueryPort;
import com.grab.framework.security.role.RoleDeclarationPublicationPort;
import com.manifest.adapter.persistence.RoleDeclarationEventFactory;
import com.manifest.adapter.persistence.SecurityManifestEventFactory;
import com.merchant.adapter.persistence.adapter.MerchantRoleDeclarationPublicationAdapter;
import com.merchant.adapter.persistence.adapter.MerchantSecurityManifestPublicationAdapter;
import com.merchant.adapter.persistence.adapter.MerchantSecurityManifestPublicationQueryAdapter;
import com.merchant.adapter.persistence.entity.MerchantSecurityManifestPublicationEntity;
import jakarta.persistence.EntityManager;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaContext;

@Configuration
public class MerchantPersistenceConfig {

    @Bean("merchantPersistenceExecutor")
    PersistenceExecutor persistenceExecutor() { return new MerchantPersistenceExecutor(); }

    @Bean
    MerchantAccountJpaAssembler merchantAssembler(MerchantAccountEntityMapper entityMapper, IdMapper ids) {
        return new MerchantAccountJpaAssembler(entityMapper, ids);
    }

    @Bean
    MerchantAccountRepository merchantRepository(
            MerchantAccountJpaRepository merchants,
            MerchantAccountJpaAssembler assembler,
            @Qualifier("merchantDomainEventProducer") DomainEventProducer events,
            @Qualifier("merchantPersistenceExecutor") PersistenceExecutor executor) {
        return new MerchantAccountRepositoryAdapter(merchants, assembler, events, executor);
    }

    @Bean
    StorefrontJpaAssembler storefrontAssembler(StorefrontEntityMapper entityMapper, IdMapper ids) {
        return new StorefrontJpaAssembler(entityMapper, ids);
    }

    @Bean
    StorefrontRepository storefrontRepository(
            StorefrontJpaRepository storefronts,
            StorefrontJpaAssembler assembler,
            @Qualifier("merchantDomainEventProducer") DomainEventProducer events,
            @Qualifier("merchantPersistenceExecutor") PersistenceExecutor executor) {
        return new StorefrontRepositoryAdapter(storefronts, assembler, events, executor);
    }

    @Bean
    StorefrontChannelBrandJpaAssembler storefrontChannelBrandJpaAssembler(
            StorefrontChannelBrandEntityMapper entityMapper,
            StorefrontChannelBrandMapper domainMapper
    ) {
        return new StorefrontChannelBrandJpaAssemblerImpl(entityMapper, domainMapper);
    }

    @Bean
    StorefrontChannelBrandRepository storefrontChannelBrandRepository(
            StorefrontChannelBrandJpaRepository brands,
            StorefrontJpaRepository storefronts,
            StorefrontChannelBrandJpaAssembler assembler,
            @Qualifier("merchantDomainEventProducer") DomainEventProducer events,
            @Qualifier("merchantPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new StorefrontChannelBrandRepositoryAdapter(brands, storefronts, assembler, events, executor);
    }

    @Bean
    StorefrontQuerySpecification storefrontQuerySpecification(JpaContext context) {
        return new StorefrontQuerySpecification(
                context.getEntityManagerByManagedType(StorefrontEntity.class)
        );
    }

    @Bean
    StorefrontQueryPort storefrontQueryPort(
            StorefrontQuerySpecification specification,
            @Qualifier("merchantPersistenceExecutor") PersistenceExecutor executor) {
        return new StorefrontQueryAdapter(specification, executor);
    }

    @Bean
    MerchantAccountQueryPort merchantAccountQueryPort(
            MerchantAccountJpaRepository merchants,
            @Qualifier("merchantPersistenceExecutor") PersistenceExecutor executor) {
        return new MerchantAccountQueryAdapter(merchants, executor);
    }

    @Bean
    MerchantMemberJpaAssembler merchantMemberAssembler(MerchantMemberEntityMapper entityMapper, IdMapper ids) {
        return new MerchantMemberJpaAssembler(entityMapper, ids);
    }

    @Bean
    MerchantMemberRepository merchantMemberRepository(
            MerchantMemberJpaRepository members,
            MerchantMemberJpaAssembler assembler,
            @Qualifier("merchantDomainEventProducer") DomainEventProducer events,
            @Qualifier("merchantPersistenceExecutor") PersistenceExecutor executor) {
        return new MerchantMemberRepositoryAdapter(members, assembler, events, executor);
    }

    @Bean
    MerchantMemberQueryPort merchantMemberQueryPort(
            MerchantMemberJpaRepository members,
            @Qualifier("merchantPersistenceExecutor") PersistenceExecutor executor) {
        return new MerchantMemberQueryAdapter(members, executor);
    }

    @Bean("merchantSecurityManifestPublicationQueryPort")
    public SecurityManifestPublicationQueryPort merchantSecurityManifestPublicationQueryPort(JpaContext context) {
        EntityManager entityManager = context.getEntityManagerByManagedType(MerchantSecurityManifestPublicationEntity.class);
        return new MerchantSecurityManifestPublicationQueryAdapter(entityManager);
    }

    @Bean("merchantSecurityManifestPublicationPort")
    public SecurityManifestPublicationPort merchantSecurityManifestPublicationPort(
            JpaContext context,
            @Qualifier("merchantDomainEventProducer") DomainEventProducer outbox,
            @Qualifier("merchantSecurityManifestEventFactory") SecurityManifestEventFactory eventFactory,
            @Value("${security.manifest.republish.fixed-delay-ms:300000}") long intervalMs) {
        return new MerchantSecurityManifestPublicationAdapter(
                context,
                outbox,
                eventFactory,
                Duration.ofMillis(intervalMs)
        );
    }

    @Bean("merchantRoleDeclarationPublicationPort")
    public RoleDeclarationPublicationPort merchantRoleDeclarationPublicationPort(
            JpaContext context,
            @Qualifier("merchantDomainEventProducer") DomainEventProducer outbox,
            @Qualifier("merchantRoleDeclarationEventFactory") RoleDeclarationEventFactory eventFactory,
            @Value("${security.manifest.republish.fixed-delay-ms:300000}") long intervalMs) {
        return new MerchantRoleDeclarationPublicationAdapter(
                context,
                outbox,
                eventFactory,
                Duration.ofMillis(intervalMs)
        );
    }
}
