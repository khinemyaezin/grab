package com.identity.application.port.inbound;

import com.identity.application.model.read.ExternalUserView;
import com.identity.application.model.read.GetExternalUserQuery;

public interface GetExternalUserUseCase {
    ExternalUserView execute(GetExternalUserQuery query);
}
