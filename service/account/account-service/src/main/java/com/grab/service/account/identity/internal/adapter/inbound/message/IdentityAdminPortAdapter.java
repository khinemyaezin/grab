package com.grab.service.account.identity.internal.adapter.inbound.message;

import com.grab.service.account.identity.internal.adapter.inbound.message.mapper.ChangeUserStatusPortMapper;
import com.grab.service.account.identity.internal.adapter.inbound.message.mapper.GetUserProfilePortMapper;
import com.grab.service.account.identity.internal.adapter.inbound.message.mapper.ListUsersPortMapper;
import com.grab.service.contract.message.account.identity.IdentityAdminPort;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import com.identity.application.model.read.GetUserProfileQuery;
import com.identity.application.model.read.GetUserProfileResult;
import com.identity.application.model.read.ListUsersQuery;
import com.identity.application.model.read.ListUsersResult;
import com.identity.application.model.write.ChangeUserStatusCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import com.grab.rpc.adapter.RmiExport;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RmiExport
public class IdentityAdminPortAdapter implements IdentityAdminPort {

    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final GetUserProfilePortMapper getUserProfileMapper;
    private final ListUsersPortMapper listUsersMapper;
    private final ChangeUserStatusPortMapper changeUserStatusMapper;

    public IdentityAdminPortAdapter(
            @Qualifier("accountCommandBus") CommandBus commandBus,
            @Qualifier("accountQueryBus") QueryBus queryBus,
            GetUserProfilePortMapper getUserProfileMapper,
            ListUsersPortMapper listUsersMapper,
            ChangeUserStatusPortMapper changeUserStatusMapper) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
        this.getUserProfileMapper = getUserProfileMapper;
        this.listUsersMapper = listUsersMapper;
        this.changeUserStatusMapper = changeUserStatusMapper;
    }

    @Override
    public UserDetailResult findUserById(Id userId) {
        if (userId == null) {
            return null;
        }
        GetUserProfileQuery query = getUserProfileMapper.toQuery(userId);
        GetUserProfileResult result = queryBus.dispatch(query);
        return getUserProfileMapper.toResponse(result);
    }

    @Override
    public List<UserSummaryResult> listUsers(int page, int size) {
        ListUsersQuery query = listUsersMapper.toQuery(page, size);
        Page<ListUsersResult> results = queryBus.dispatch(query);
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }
        return results.map(listUsersMapper::toResponse).toList();
    }

    @Override
    public void changeUserStatus(Id userId, String status) {
        ChangeUserStatusCommand command = changeUserStatusMapper.toCommand(userId, status);
        commandBus.dispatch(command);
    }
}
