package com.grab.service.authorizationserver.core.user;

import java.util.List;

public interface IamAccessContextRepository {
    List<IamAccessContext> findByUserId(String userId);

    void replaceUserContexts(String userId, List<IamAccessContext> contexts);
}
