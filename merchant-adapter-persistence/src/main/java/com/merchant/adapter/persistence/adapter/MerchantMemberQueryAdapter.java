package com.merchant.adapter.persistence.adapter;

import com.grab.framework.support.PersistenceExecutor;
import com.merchant.adapter.persistence.entity.MerchantMemberEntity;
import com.merchant.adapter.persistence.repository.jpa.MerchantMemberJpaRepository;
import com.merchant.application.model.read.MerchantMemberView;
import com.merchant.application.port.outbound.MerchantMemberQueryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class MerchantMemberQueryAdapter implements MerchantMemberQueryPort {
    private final MerchantMemberJpaRepository jpaRepository;
    private final PersistenceExecutor executor;

    @Override
    public List<MerchantMemberView> findByMerchantId(String merchantId) {
        return executor.query("MerchantMember", () ->
                jpaRepository.findByMerchantIdOrderByCreatedAtAsc(merchantId).stream()
                        .map(this::toView)
                        .toList());
    }

    @Override
    public Optional<MerchantMemberView> findById(String memberId) {
        return executor.query("MerchantMember", () ->
                jpaRepository.findByUuid(memberId).map(this::toView));
    }

    @Override
    public Optional<MerchantMemberView> findByMerchantIdAndUserId(String merchantId, String userId) {
        return executor.query("MerchantMember", () ->
                jpaRepository.findByMerchantIdAndUserId(merchantId, userId).map(this::toView));
    }

    private MerchantMemberView toView(MerchantMemberEntity entity) {
        return new MerchantMemberView(
                entity.getUuid(),
                entity.getMerchantId(),
                entity.getUserId(),
                entity.getRole(),
                entity.getStatus().name(),
                entity.getInvitedBy(),
                entity.getInvitationExpiresAt(),
                entity.getJoinedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }
}
