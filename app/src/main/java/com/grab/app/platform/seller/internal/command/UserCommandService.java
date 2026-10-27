package com.grab.app.platform.seller.internal.command;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.UserProfileResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.mapper.ChangeUserStatusRequestMapper;
import com.identity.application.model.write.ChangeUserStatusCommand;
import com.identity.application.model.write.UserProfileResult;
import com.identity.domain.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCommandService {

    private final CommandBus commandBus;
    private final ChangeUserStatusRequestMapper changeStatusMapper;

    public UserProfileResponse changeStatus(String id, UserStatus status) {
        ChangeUserStatusCommand command = changeStatusMapper.toCommand(id, status);
        UserProfileResult result = commandBus.dispatch(command);
        return changeStatusMapper.toResponse(result);
    }
}
