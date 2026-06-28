package com.merchant.infrastructure.mapper.jpa.impl;

import com.grab.framework.mapper.IdMapper;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.valueobject.*;
import com.merchant.infrastructure.entity.MerchantAccountEntity;
import com.merchant.infrastructure.mapper.jpa.MerchantAccountEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
public class MerchantAccountJpaAssembler {
    private final MerchantAccountEntityMapper entityMapper;
    private final IdMapper ids;

    public MerchantAccountEntity toEntity(MerchantAccount source, MerchantAccountEntity destination) {
        MerchantAccountEntity entity = destination == null ? new MerchantAccountEntity() : destination;
        entityMapper.toEntity(source, entity);
        return entity;
    }

    public MerchantAccount toDomain(MerchantAccountEntity source) {
        BusinessRegistration registration = source.getRegistrationNumber() == null ? null
                : new BusinessRegistration(source.getRegistrationCountryCode(), source.getRegistrationNumber());
        ContactInformation contact = source.getContactEmail() == null ? null
                : new ContactInformation(source.getContactEmail(), source.getContactPhone());
        RegisteredAddress address = source.getAddressLine1() == null ? null
                : new RegisteredAddress(
                        source.getAddressLine1(), source.getAddressLine2(), source.getAddressCity(),
                        source.getAddressRegion(), source.getAddressPostalCode(), source.getAddressCountryCode()
                );
        return new MerchantAccount(
                ids.map(source.getUuid()), ids.map(source.getApplicantUserId()), source.getType(),
                new MerchantName(source.getLegalName(), source.getDisplayName()), registration, contact, address,
                source.getStatus(), source.getLifecycleReason() == null ? null : new LifecycleReason(source.getLifecycleReason()),
                ids.map(source.getReviewedBy()), source.getReviewedAt(), source.getCreatedAt(), source.getUpdatedAt(),
                source.getVersion()
        );
    }
}
