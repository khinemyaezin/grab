package com.merchant.adapter.persistence.adapter;

import com.grab.framework.support.PersistenceExecutor;
import com.merchant.adapter.persistence.entity.MerchantAccountEntity;
import com.merchant.adapter.persistence.repository.jpa.MerchantAccountJpaRepository;
import com.merchant.application.model.read.MerchantAccountView;
import com.merchant.application.port.outbound.MerchantAccountQueryPort;
import com.merchant.domain.enums.MerchantStatus;
import com.merchant.domain.enums.MerchantType;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class MerchantAccountQueryAdapter implements MerchantAccountQueryPort {
    private final MerchantAccountJpaRepository jpaRepository;
    private final PersistenceExecutor executor;

    @Override
    public Optional<MerchantAccountView> findById(String merchantId) {
        return executor.query("MerchantAccount", () -> jpaRepository.findByUuid(merchantId).map(this::toView));
    }

    @Override
    public List<MerchantAccountView> findByApplicantUserId(String applicantUserId) {
        return executor.query("MerchantAccount", () ->
                jpaRepository.findByApplicantUserIdOrderByCreatedAtDesc(applicantUserId).stream()
                        .map(this::toView)
                        .toList());
    }

    @Override
    public List<MerchantAccountView> findByStatus(MerchantStatus status) {
        return executor.query("MerchantAccount", () ->
                jpaRepository.findByStatusOrderByCreatedAt(status).stream()
                        .map(this::toView)
                        .toList());
    }

    @Override
    public Optional<MerchantAccountView> findByApplicantUserIdAndType(String applicantUserId, MerchantType type) {
        return executor.query("MerchantAccount", () ->
                jpaRepository.findByApplicantUserIdAndType(applicantUserId, type).map(this::toView));
    }

    private MerchantAccountView toView(MerchantAccountEntity entity) {
        return new MerchantAccountView(
                entity.getUuid(),
                entity.getApplicantUserId(),
                entity.getType(),
                entity.getLegalName(),
                entity.getDisplayName(),
                entity.getRegistrationCountryCode(),
                entity.getRegistrationNumber(),
                entity.getContactEmail(),
                entity.getContactPhone(),
                entity.getAddressLine1(),
                entity.getAddressLine2(),
                entity.getAddressCity(),
                entity.getAddressRegion(),
                entity.getAddressPostalCode(),
                entity.getAddressCountryCode(),
                entity.getStatus(),
                entity.getLifecycleReason(),
                entity.getReviewedBy(),
                entity.getReviewedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }
}
