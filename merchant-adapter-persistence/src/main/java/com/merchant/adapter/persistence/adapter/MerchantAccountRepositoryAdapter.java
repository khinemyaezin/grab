package com.merchant.adapter.persistence.adapter;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import com.merchant.adapter.persistence.entity.MerchantAccountEntity;
import com.merchant.adapter.persistence.mapper.jpa.impl.MerchantAccountJpaAssembler;
import com.merchant.adapter.persistence.repository.jpa.MerchantAccountJpaRepository;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.enums.MerchantStatus;
import com.merchant.domain.enums.MerchantType;
import com.merchant.domain.port.outbound.MerchantAccountRepository;
import com.merchant.domain.valueobject.BusinessRegistration;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class MerchantAccountRepositoryAdapter implements MerchantAccountRepository {
    private final MerchantAccountJpaRepository merchants;
    private final MerchantAccountJpaAssembler assembler;
    private final DomainEventProducer events;
    private final PersistenceExecutor executor;

    @Override
    public Optional<MerchantAccount> findById(Id id) {
        return executor.query("MerchantAccount", () ->
                merchants.findByUuid(id.getValue()).map(assembler::toDomain));
    }

    @Override
    public List<MerchantAccount> findByApplicantUserId(Id applicantUserId) {
        return executor.query("MerchantAccount", () ->
                merchants
                        .findByApplicantUserIdOrderByCreatedAtDesc(applicantUserId.getValue())
                        .stream().map(assembler::toDomain).toList());
    }

    @Override
    public List<MerchantAccount> findByStatus(MerchantStatus status) {
        return executor.query("MerchantAccount", () ->
                merchants.findByStatusOrderByCreatedAt(status)
                        .stream().map(assembler::toDomain).toList());
    }

    @Override
    public boolean existsOpenApplication(Id applicantUserId, MerchantType type) {
        return executor.query("MerchantAccount", () ->
                merchants.existsOpenApplication(applicantUserId.getValue(), type));
    }

    @Override
    public boolean existsRegistration(BusinessRegistration registration, Id excludingMerchantId) {
        return executor.query("MerchantAccount", () ->
                merchants.existsRegistration(
                        registration.countryCode(), registration.registrationNumber(), excludingMerchantId.getValue()
                ));
    }

    @Override
    public MerchantAccount save(MerchantAccount merchant) {
        return executor.command("MerchantAccount", () -> {
            MerchantAccountEntity existing = merchants.findByUuid(merchant.getId().getValue()).orElse(null);
            MerchantAccountEntity saved = merchants.save(assembler.toEntity(merchant, existing));
            List<Event> pending = merchant.pullEvents();
            events.produce("MerchantAccount", merchant.getId().getValue(), pending);
            return assembler.toDomain(saved);
        });
    }
}
