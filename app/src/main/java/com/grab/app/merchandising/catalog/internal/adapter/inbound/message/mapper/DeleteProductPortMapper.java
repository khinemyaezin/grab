package com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper;

import com.catalog.application.model.write.DeleteProductCommand;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class DeleteProductPortMapper {

    public DeleteProductCommand toCommand(Id merchantId, Id productId) {
        return new DeleteProductCommand(merchantId, productId);
    }
}

