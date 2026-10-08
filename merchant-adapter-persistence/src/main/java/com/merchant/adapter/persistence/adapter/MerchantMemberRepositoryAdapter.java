package com.merchant.adapter.persistence.adapter;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import com.merchant.adapter.persistence.entity.MerchantMemberEntity;
import com.merchant.adapter.persistence.mapper.jpa.impl.MerchantMemberJpaAssembler;
import com.merchant.adapter.persistence.repository.jpa.MerchantMemberJpaRepository;
import com.merchant.domain.aggregate.MerchantMember;
import com.merchant.domain.enums.MemberStatus;
import com.merchant.domain.port.outbound.MerchantMemberRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class MerchantMemberRepositoryAdapter implements MerchantMemberRepository {
    private final MerchantMemberJpaRepository members;
    private final MerchantMemberJpaAssembler assembler;
    private final DomainEventProducer events;
    private final PersistenceExecutor executor;

    @Override
    public Optional<MerchantMember> findById(Id id) {
        return executor.query("MerchantMember", () ->
                members.findByUuid(id.getValue()).map(assembler::toDomain));
    }

    @Override
    public Optional<MerchantMember> findByMerchantIdAndUserId(Id merchantId, Id userId) {
        return executor.query("MerchantMember", () ->
                members.findByMerchantIdAndUserId(merchantId.getValue(), userId.getValue())
                        .map(assembler::toDomain));
    }

    @Override
    public long countActiveAdmins(Id merchantId) {
        return executor.query("MerchantMember", () ->
                members.countByMerchantIdAndRoleAndStatus(
                        merchantId.getValue(),
                        "MERCHANT_ADMIN",
                        MemberStatus.ACTIVE
                ));
    }

    @Override
    public boolean existsByMerchantIdAndUserId(Id merchantId, Id userId) {
        return executor.query("MerchantMember", () ->
                members.existsByMerchantIdAndUserId(merchantId.getValue(), userId.getValue()));
    }

    @Override
    public MerchantMember save(MerchantMember member) {
        return executor.command("MerchantMember", () -> {
            MerchantMemberEntity existing = members.findByUuid(member.getId().getValue()).orElse(null);
            MerchantMemberEntity saved = members.save(assembler.toEntity(member, existing));
            List<Event> pending = member.pullEvents();
            events.produce("MerchantMember", member.getId().getValue(), pending);
            return assembler.toDomain(saved);
        });
    }
}
