package com.grab.app.account.merchant.internal.adapter.inbound.message.mapper;

import com.grab.app.account.merchant.port.message.MerchantAccountPort.MerchantMessageResult;
import com.grab.app.account.merchant.port.message.MerchantAccountPort.UpdateMerchantProfileMessage;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.application.model.write.UpdateMerchantProfileCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UpdateMerchantProfilePortMapper {

    public UpdateMerchantProfileCommand toCommand(Id merchantId, UpdateMerchantProfileMessage message) {
        if (message == null) {
            return null;
        }
        return new UpdateMerchantProfileCommand(
                merchantId,
                message.applicantUserId(),
                message.legalName(),
                message.displayName(),
                message.registrationCountryCode(),
                message.registrationNumber(),
                message.contactEmail(),
                message.contactPhone(),
                message.addressLine1(),
                message.addressLine2(),
                message.addressCity(),
                message.addressRegion(),
                message.addressPostalCode(),
                message.addressCountryCode()
        );
    }

    @Mapping(target = "businessType", source = "type")
    public abstract MerchantMessageResult toResponse(MerchantAccountResult result);
}

