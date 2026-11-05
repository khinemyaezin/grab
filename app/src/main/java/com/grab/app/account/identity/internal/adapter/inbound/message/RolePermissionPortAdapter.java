package com.grab.app.account.identity.internal.adapter.inbound.message;

import com.grab.app.account.identity.internal.adapter.inbound.message.mapper.CreateRolePortMapper;
import com.grab.app.account.identity.internal.adapter.inbound.message.mapper.ListRolesPortMapper;
import com.grab.app.account.identity.internal.adapter.inbound.message.mapper.ManageAuthorityPortMapper;
import com.grab.app.account.identity.internal.adapter.inbound.message.mapper.SearchRolesPortMapper;
import com.grab.app.account.identity.port.message.RolePermissionPort;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.identity.application.model.read.ListRolesQuery;
import com.identity.application.model.read.ListRolesResult;
import com.identity.application.model.read.SearchRolesQuery;
import com.identity.application.model.read.SearchRolesResult;
import com.identity.application.model.write.CreateRoleCommand;
import com.identity.application.model.write.ManageAuthorityCommand;
import com.identity.application.model.write.RoleResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class RolePermissionPortAdapter implements RolePermissionPort {

    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final CreateRolePortMapper createRoleMapper;
    private final ManageAuthorityPortMapper manageAuthorityMapper;
    private final ListRolesPortMapper listRolesMapper;
    private final SearchRolesPortMapper searchRolesMapper;

    public RolePermissionPortAdapter(
            @Qualifier("accountCommandBus") CommandBus commandBus,
            @Qualifier("accountQueryBus") QueryBus queryBus,
            CreateRolePortMapper createRoleMapper,
            ManageAuthorityPortMapper manageAuthorityMapper,
            ListRolesPortMapper listRolesMapper,
            SearchRolesPortMapper searchRolesMapper) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
        this.createRoleMapper = createRoleMapper;
        this.manageAuthorityMapper = manageAuthorityMapper;
        this.listRolesMapper = listRolesMapper;
        this.searchRolesMapper = searchRolesMapper;
    }

    @Override
    public RoleItemResult createRole(CreateRoleMessage message) {
        CreateRoleCommand command = createRoleMapper.toCommand(message);
        RoleResult result = commandBus.dispatch(command);
        return createRoleMapper.toResponse(result);
    }

    @Override
    public void grantAuthorityToRole(String roleCode, String authorityCode) {
        ManageAuthorityCommand command = manageAuthorityMapper.toCommand(roleCode, authorityCode, true);
        commandBus.dispatch(command);
    }

    @Override
    public void revokeAuthorityFromRole(String roleCode, String authorityCode) {
        ManageAuthorityCommand command = manageAuthorityMapper.toCommand(roleCode, authorityCode, false);
        commandBus.dispatch(command);
    }

    @Override
    public List<RoleItemResult> listRoles(String name, int page, int size) {
        ListRolesQuery query = listRolesMapper.toQuery(name, page, size);
        Page<ListRolesResult> roles = queryBus.dispatch(query);
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.map(listRolesMapper::toResponse).toList();
    }

    @Override
    public List<RoleSuggestionResult> searchRoles(String name) {
        SearchRolesQuery searchQuery = searchRolesMapper.toQuery(name);
        SearchRolesResult results = queryBus.dispatch(searchQuery);
        if (results == null || results.roles() == null) {
            return Collections.emptyList();
        }
        return results.roles().stream().map(searchRolesMapper::toResponse).toList();
    }
}
