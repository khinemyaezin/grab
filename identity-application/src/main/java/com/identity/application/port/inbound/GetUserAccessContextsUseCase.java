package com.identity.application.port.inbound;

import com.identity.application.model.read.GetUserAccessContextsQuery;
import com.identity.application.model.read.UserAccessContextsResult;

public interface GetUserAccessContextsUseCase {
    UserAccessContextsResult execute(GetUserAccessContextsQuery query);
}
