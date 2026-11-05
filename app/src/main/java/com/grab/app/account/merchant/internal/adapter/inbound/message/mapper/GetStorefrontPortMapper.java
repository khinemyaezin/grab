package com.grab.app.account.merchant.internal.adapter.inbound.message.mapper;

import com.grab.app.account.merchant.port.message.MerchantStorefrontPort.StorefrontMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.merchant.application.model.read.GetStorefrontQuery;
import com.merchant.application.model.write.StorefrontResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetStorefrontPortMapper {

    public GetStorefrontQuery toQuery(Id storefrontId, Id merchantId) {
        return new GetStorefrontQuery(storefrontId, merchantId);
    }

    public abstract StorefrontMessageResult toResponse(StorefrontResult result);
}
