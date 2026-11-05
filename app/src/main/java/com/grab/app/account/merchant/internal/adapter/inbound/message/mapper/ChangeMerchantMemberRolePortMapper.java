package com.grab.app.account.merchant.internal.adapter.inbound.message.mapper;

import com.grab.app.account.merchant.port.message.MerchantMembershipPort.MemberMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.merchant.application.model.write.ChangeMerchantMemberRoleCommand;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.domain.valueobject.MerchantRole;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ChangeMerchantMemberRolePortMapper {

    public ChangeMerchantMemberRoleCommand toCommand(Id merchantId, Id memberId, Id actorId, String newRole) {
        MerchantRole role = newRole != null ? MerchantRole.of(newRole) : MerchantRole.of("MEMBER");
        return new ChangeMerchantMemberRoleCommand(merchantId, memberId, actorId, role);
    }

    public abstract MemberMessageResult toResponse(MerchantMemberResult result);
}
