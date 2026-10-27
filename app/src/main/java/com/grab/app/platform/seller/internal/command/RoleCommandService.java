package com.grab.app.platform.seller.internal.command;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.request.CreateRoleRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.RoleResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.mapper.CreateRoleRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.mapper.ManageAuthorityRequestMapper;
import com.identity.application.model.write.CreateRoleCommand;
import com.identity.application.model.write.ManageAuthorityCommand;
import com.identity.application.model.write.RoleResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleCommandService {

    private final CommandBus commandBus;
    private final CreateRoleRequestMapper createRoleMapper;
    private final ManageAuthorityRequestMapper manageAuthorityMapper;

    public RoleResponse createRole(CreateRoleRequest request) {
        CreateRoleCommand command = createRoleMapper.toCommand(request);
        RoleResult result = commandBus.dispatch(command);
        return createRoleMapper.toResponse(result);
    }

    public RoleResponse manageAuthority(String roleCode, String authorityCode, boolean assign) {
        ManageAuthorityCommand command = manageAuthorityMapper.toCommand(roleCode, authorityCode, assign);
        RoleResult result = commandBus.dispatch(command);
        return manageAuthorityMapper.toResponse(result);
    }
}
