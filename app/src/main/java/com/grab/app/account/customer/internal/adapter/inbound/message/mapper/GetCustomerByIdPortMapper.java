package com.grab.app.account.customer.internal.adapter.inbound.message.mapper;

import com.customer.application.model.read.CustomerView;
import com.customer.application.model.read.GetCustomerByIdQuery;
import com.grab.app.account.customer.port.message.CustomerAccountPort.CustomerProfileResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetCustomerByIdPortMapper {

    public GetCustomerByIdQuery toQuery(Id customerId) {
        return customerId != null ? new GetCustomerByIdQuery(customerId) : null;
    }

    public abstract CustomerProfileResult toResponse(CustomerView view);
}

