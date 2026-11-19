package com.grab.service.account.identity.internal.adapter.inbound.message;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.rpc.adapter.RmiExport;
import com.grab.service.account.identity.internal.adapter.inbound.message.mapper.LinkExternalIdentityPortMapper;
import com.grab.service.contract.message.account.identity.ExternalIdentityLinkPort;
import com.identity.application.model.write.ExternalIdentityLinkResult;
import com.identity.application.model.write.LinkExternalIdentityCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.rmi.RemoteException;

@Component
@RmiExport
public class ExternalIdentityLinkPortAdapter implements ExternalIdentityLinkPort {
    private final CommandBus commands;
    private final LinkExternalIdentityPortMapper mapper;

    public ExternalIdentityLinkPortAdapter(@Qualifier("accountCommandBus") CommandBus commands,
                                           LinkExternalIdentityPortMapper mapper) {
        this.commands = commands;
        this.mapper = mapper;
    }

    @Override
    public LinkResult linkExistingIdentity(LinkRequest request) throws RemoteException {
        LinkExternalIdentityCommand command = mapper.toCommand(request);
        ExternalIdentityLinkResult result = commands.dispatch(command);
        return mapper.toResponse(result);
    }
}
