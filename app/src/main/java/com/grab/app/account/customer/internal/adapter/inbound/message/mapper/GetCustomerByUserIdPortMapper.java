package com.grab.app.account.customer.internal.adapter.inbound.message.mapper;

import com.customer.application.model.read.CustomerView;
import com.customer.application.model.read.GetCustomerByUserIdQuery;
import com.grab.app.account.customer.port.message.CustomerAccountPort.CustomerProfileResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetCustomerByUserIdPortMapper {

    public GetCustomerByUserIdQuery toQuery(Id userId) {
        return userId != null ? new GetCustomerByUserIdQuery(userId) : null;
    }

    public abstract CustomerProfileResult toResponse(CustomerView view);
}

