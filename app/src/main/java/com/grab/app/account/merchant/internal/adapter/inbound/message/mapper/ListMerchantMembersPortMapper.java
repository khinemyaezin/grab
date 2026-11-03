package com.grab.app.account.merchant.internal.adapter.inbound.message.mapper;

import com.grab.app.account.merchant.port.message.MerchantMembershipPort.MemberMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.merchant.application.model.read.ListMerchantMembersQuery;
import com.merchant.application.model.write.MerchantMemberResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListMerchantMembersPortMapper {

    public ListMerchantMembersQuery toQuery(Id merchantId) {
        return new ListMerchantMembersQuery(merchantId);
    }

    public abstract MemberMessageResult toResponse(MerchantMemberResult result);
}
