package com.merchant.infrastructure.mapper.jpa;

import com.grab.framework.mapper.IdMapper;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.infrastructure.entity.MerchantAccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class MerchantAccountEntityMapper {
    @Mapping(ignore = true, target = "id")
    @Mapping(source = "id", target = "uuid")
    @Mapping(source = "applicantUserId", target = "applicantUserId")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "name.legalName", target = "legalName")
    @Mapping(source = "name.displayName", target = "displayName")
    @Mapping(source = "registration.countryCode", target = "registrationCountryCode")
    @Mapping(source = "registration.registrationNumber", target = "registrationNumber")
    @Mapping(source = "contact.email", target = "contactEmail")
    @Mapping(source = "contact.phone", target = "contactPhone")
    @Mapping(source = "registeredAddress.line1", target = "addressLine1")
    @Mapping(source = "registeredAddress.line2", target = "addressLine2")
    @Mapping(source = "registeredAddress.city", target = "addressCity")
    @Mapping(source = "registeredAddress.region", target = "addressRegion")
    @Mapping(source = "registeredAddress.postalCode", target = "addressPostalCode")
    @Mapping(source = "registeredAddress.countryCode", target = "addressCountryCode")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "lifecycleReason.value", target = "lifecycleReason")
    @Mapping(source = "reviewedBy", target = "reviewedBy")
    @Mapping(source = "reviewedAt", target = "reviewedAt")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "version", target = "version")
    public abstract void toEntity(MerchantAccount source, @MappingTarget MerchantAccountEntity destination);
}
