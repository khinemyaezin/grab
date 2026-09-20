package com.identity.application.port.inbound;

import com.identity.application.model.read.GetUserProfileQuery;
import com.identity.application.model.read.GetUserProfileResult;

public interface GetUserProfileUseCase {
    GetUserProfileResult execute(GetUserProfileQuery query);
}
