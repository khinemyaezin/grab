package com.grab.store.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.identity.internal.config.IdentityReadTransactional;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.grab.store.identity.internal.query.GetUserProfileQuery;
import com.grab.store.identity.internal.query.GetUserProfileResult;
import com.identity.infrastructure.repository.jpa.UserQueryRepository;
import com.identity.infrastructure.view.UserAssignmentView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetUserProfileQueryHandler implements QueryHandler<GetUserProfileQuery, GetUserProfileResult> {

    private final UserQueryRepository repository;

    @Override
    @IdentityReadTransactional
    public GetUserProfileResult handle(GetUserProfileQuery query) {
        List<UserAssignmentView> userAssignmentViews = repository.queryUserAndByUserId(query.userId().getValue());
        
        if (userAssignmentViews.isEmpty()) {
            throw new IdentityServiceException(
                    new IdentityServiceError.UserNotFound(query.userId().getValue()),
                    "User not found for userId: " + query.userId().getValue()
            );
        }

        UserAssignmentView first = userAssignmentViews.getFirst();

        List<GetUserProfileResult.AccessContextInfo> accessContexts = userAssignmentViews.stream()
                .filter(view -> view.assignmentId() != null)
                .map(view -> new GetUserProfileResult.AccessContextInfo(
                        view.assignmentId(),
                        view.platformCode(),
                        view.roleCode(),
                        view.scopeKey(),
                        view.scopeId(),
                        view.assignmentStatus()
                ))
                .collect(Collectors.toList());

        return new GetUserProfileResult(
                first.userId(),
                first.email(),
                first.userStatus(),
                first.createdAt(),
                accessContexts
        );
    }

    @Override
    public Class<GetUserProfileQuery> getQueryType() {
        return GetUserProfileQuery.class;
    }
}
