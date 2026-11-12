package com.grab.app.account.merchant.internal.adapter.inbound.message.mapper;

import com.grab.app.account.merchant.port.message.MerchantAccountPort.C2CApplicationResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.merchant.application.model.read.GetC2CApplicationQuery;
import com.merchant.application.model.read.GetC2CApplicationResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetC2CApplicationPortMapper {

    public GetC2CApplicationQuery toQuery(Id applicantUserId) {
        return new GetC2CApplicationQuery(applicantUserId);
    }

    public abstract C2CApplicationResult toResponse(GetC2CApplicationResult result);
}

