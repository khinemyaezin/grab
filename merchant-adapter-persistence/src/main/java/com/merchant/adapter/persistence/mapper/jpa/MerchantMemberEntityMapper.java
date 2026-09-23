package com.merchant.adapter.persistence.mapper.jpa;

import com.grab.framework.mapper.IdMapper;
import com.merchant.adapter.persistence.entity.MerchantMemberEntity;
import com.merchant.domain.aggregate.MerchantMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class MerchantMemberEntityMapper {
    @Mapping(ignore = true, target = "id")
    @Mapping(source = "id", target = "uuid")
    @Mapping(source = "merchantId", target = "merchantId")
    @Mapping(ignore = true, target = "role")
    @Mapping(ignore = true, target = "authorities")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "invitedBy", target = "invitedBy")
    @Mapping(source = "invitationExpiresAt", target = "invitationExpiresAt")
    @Mapping(source = "joinedAt", target = "joinedAt")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "version", target = "version")
    public abstract void toEntity(MerchantMember source, @MappingTarget MerchantMemberEntity destination);
}
