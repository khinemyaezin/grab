package com.grab.app.account.merchant.internal.adapter.inbound.message.mapper;

import com.grab.app.account.merchant.port.message.MerchantMembershipPort.InviteMemberMessage;
import com.grab.app.account.merchant.port.message.MerchantMembershipPort.MemberMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.merchant.application.model.write.InviteMerchantMemberCommand;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.domain.valueobject.MerchantRole;
import org.mapstruct.Mapper;

import java.time.Instant;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class InviteMerchantMemberPortMapper {

    public InviteMerchantMemberCommand toCommand(InviteMemberMessage message) {
        if (message == null) {
            return null;
        }
        Instant expiresAt = message.expiresAt() != null ? message.expiresAt() : Instant.now().plusSeconds(86400 * 7);
        MerchantRole role = message.role() != null ? MerchantRole.of(message.role()) : MerchantRole.of("MEMBER");
        return new InviteMerchantMemberCommand(
                message.merchantId(),
                message.actorId(),
                message.targetUserId(),
                role,
                expiresAt
        );
    }

    public abstract MemberMessageResult toResponse(MerchantMemberResult result);
}

