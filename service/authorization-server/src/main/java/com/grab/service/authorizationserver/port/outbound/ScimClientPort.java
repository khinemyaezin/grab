package com.grab.service.authorizationserver.port.outbound;

import com.grab.service.contract.message.account.scim.ScimAccessContextExtension;
import com.grab.service.contract.message.account.scim.ScimUser;

public interface ScimClientPort {
    ScimUser provisionUser(ScimUser request);

    ScimUser getUser(String idOrExternalId);

    ScimUser updateUserStatus(String idOrExternalId, boolean active);

    ScimAccessContextExtension getUserAccessContexts(String idOrExternalId);
}
