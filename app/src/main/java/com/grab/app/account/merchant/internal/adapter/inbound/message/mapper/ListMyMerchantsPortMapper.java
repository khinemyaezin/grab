package com.grab.app.account.merchant.internal.adapter.inbound.message.mapper;

import com.grab.app.account.merchant.port.message.MerchantAccountPort.MerchantMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.merchant.application.model.read.ListMyMerchantsQuery;
import com.merchant.application.model.write.MerchantAccountResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListMyMerchantsPortMapper {

    public ListMyMerchantsQuery toQuery(Id applicantUserId) {
        return new ListMyMerchantsQuery(applicantUserId);
    }

    @Mapping(target = "businessType", source = "type")
    public abstract MerchantMessageResult toResponse(MerchantAccountResult result);
}
