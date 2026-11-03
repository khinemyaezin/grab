package com.grab.app.account.identity.internal.adapter.inbound.message;

import com.grab.app.account.identity.internal.adapter.inbound.message.mapper.ChangeAccessStatusPortMapper;
import com.grab.app.account.identity.internal.adapter.inbound.message.mapper.GrantAccessPortMapper;
import com.grab.app.account.identity.internal.adapter.inbound.message.mapper.ListAccessAssignmentsPortMapper;
import com.grab.app.account.identity.internal.adapter.inbound.message.mapper.ListAccessContextsPortMapper;
import com.grab.app.account.identity.internal.adapter.inbound.message.mapper.SwitchAccessContextPortMapper;
import com.grab.app.account.identity.port.message.AccessAssignmentPort;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import com.identity.application.model.read.AccessContextResult;
import com.identity.application.model.read.ListAccessAssignmentsQuery;
import com.identity.application.model.read.ListAccessContextsQuery;
import com.identity.application.model.write.AccessAssignmentResult;
import com.identity.application.model.write.AuthResult;
import com.identity.application.model.write.ChangeAccessStatusCommand;
import com.identity.application.model.write.GrantAccessCommand;
import com.identity.application.model.write.SwitchAccessContextCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class AccessAssignmentPortAdapter implements AccessAssignmentPort {

    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final GrantAccessPortMapper grantAccessMapper;
    private final ChangeAccessStatusPortMapper changeAccessStatusMapper;
    private final SwitchAccessContextPortMapper switchContextMapper;
    private final ListAccessAssignmentsPortMapper listAssignmentsMapper;
    private final ListAccessContextsPortMapper listContextsMapper;

    public AccessAssignmentPortAdapter(
            @Qualifier("accountCommandBus") CommandBus commandBus,
            @Qualifier("accountQueryBus") QueryBus queryBus,
            GrantAccessPortMapper grantAccessMapper,
            ChangeAccessStatusPortMapper changeAccessStatusMapper,
            SwitchAccessContextPortMapper switchContextMapper,
            ListAccessAssignmentsPortMapper listAssignmentsMapper,
            ListAccessContextsPortMapper listContextsMapper) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
        this.grantAccessMapper = grantAccessMapper;
        this.changeAccessStatusMapper = changeAccessStatusMapper;
        this.switchContextMapper = switchContextMapper;
        this.listAssignmentsMapper = listAssignmentsMapper;
        this.listContextsMapper = listContextsMapper;
    }

    @Override
    public AssignmentMessageResult assignAccess(AssignAccessMessage message) {
        GrantAccessCommand command = grantAccessMapper.toCommand(message);
        AccessAssignmentResult result = commandBus.dispatch(command);
        return grantAccessMapper.toResponse(result);
    }

    @Override
    public AssignmentMessageResult changeAssignmentStatus(ChangeAssignmentStatusMessage message) {
        ChangeAccessStatusCommand command = changeAccessStatusMapper.toCommand(message);
        AccessAssignmentResult result = commandBus.dispatch(command);
        return changeAccessStatusMapper.toResponse(result);
    }

    @Override
    public AuthTokenResult switchAccessContext(SwitchContextMessage message) {
        SwitchAccessContextCommand command = switchContextMapper.toCommand(message);
        AuthResult result = commandBus.dispatch(command);
        return switchContextMapper.toResponse(result);
    }

    @Override
    public List<AssignmentMessageResult> listAssignmentsForUser(Id userId, String actorScopeKey, String actorScopeId) {
        ListAccessAssignmentsQuery query = listAssignmentsMapper.toQuery(userId, actorScopeKey, actorScopeId);
        List<AccessAssignmentResult> results = queryBus.dispatch(query);
        if (results == null) {
            return Collections.emptyList();
        }
        return results.stream().map(listAssignmentsMapper::toResponse).toList();
    }

    @Override
    public List<ContextMessageResult> listAvailableContexts(Id userId) {
        ListAccessContextsQuery query = listContextsMapper.toQuery(userId);
        List<AccessContextResult> results = queryBus.dispatch(query);
        if (results == null) {
            return Collections.emptyList();
        }
        return results.stream().map(listContextsMapper::toResponse).toList();
    }
}
