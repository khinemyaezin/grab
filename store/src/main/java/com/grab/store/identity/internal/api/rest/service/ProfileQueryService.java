package com.grab.store.identity.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.store.identity.internal.api.rest.dto.response.CurrentUserProfileResponse;
import com.grab.store.identity.internal.api.rest.mapper.GetCurrentUserProfileRequestMapper;
import com.grab.store.identity.internal.query.GetUserProfileQuery;
import com.grab.store.identity.internal.query.GetUserProfileResult;
import com.grab.store.shared.security.SecurityPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileQueryService {

    private final QueryBus queryBus;
    private final GetCurrentUserProfileRequestMapper profileMapper;

    public CurrentUserProfileResponse getProfile(String userId, SecurityPrincipal principal) {
        GetUserProfileQuery query = profileMapper.toQuery(userId);
        GetUserProfileResult result = queryBus.dispatch(query);
        return profileMapper.toResponse(result, principal.getAccessContext().orElse(null));
    }
}
