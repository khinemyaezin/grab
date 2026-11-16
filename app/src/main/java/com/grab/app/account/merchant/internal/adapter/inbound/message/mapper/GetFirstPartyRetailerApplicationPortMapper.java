package com.grab.app.account.merchant.internal.adapter.inbound.message.mapper;

import com.grab.app.account.merchant.port.message.MerchantAccountPort.FirstPartyApplicationResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.merchant.application.model.read.GetFirstPartyRetailerApplicationQuery;
import com.merchant.application.model.read.GetFirstPartyRetailerApplicationResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetFirstPartyRetailerApplicationPortMapper {

    public GetFirstPartyRetailerApplicationQuery toQuery(Id applicantUserId) {
        return new GetFirstPartyRetailerApplicationQuery(applicantUserId);
    }

    public abstract FirstPartyApplicationResult toResponse(GetFirstPartyRetailerApplicationResult result);
}

