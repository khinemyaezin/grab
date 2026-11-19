package com.grab.service.account.identity.internal.adapter.inbound.message;

import com.grab.service.contract.message.account.identity.ScimUserPort;
import com.grab.service.contract.message.account.scim.ScimAccessContextExtension;
import com.grab.service.contract.message.account.scim.ScimUser;
import org.springframework.stereotype.Component;

import java.rmi.RemoteException;

@Component
public class ScimUserPortAdapter implements ScimUserPort {
    private final ScimUserReadAdapter reads;
    private final ScimUserWriteAdapter writes;

    public ScimUserPortAdapter(ScimUserReadAdapter reads, ScimUserWriteAdapter writes) {
        this.reads = reads;
        this.writes = writes;
    }

    @Override
    public ScimUser provisionUser(ScimUser request) throws RemoteException {
        return writes.provisionUser(request);
    }

    @Override
    public ScimUser getUser(String idOrExternalId) throws RemoteException {
        return reads.getUser(idOrExternalId);
    }

    @Override
    public ScimUser updateUserStatus(String idOrExternalId, boolean active) throws RemoteException {
        return writes.updateUserStatus(idOrExternalId, active);
    }

    @Override
    public ScimAccessContextExtension getUserAccessContexts(String idOrExternalId) throws RemoteException {
        return reads.getUserAccessContexts(idOrExternalId);
    }
}
