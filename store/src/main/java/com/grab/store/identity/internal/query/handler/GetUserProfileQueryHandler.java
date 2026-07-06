package com.grab.store.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.identity.internal.config.IdentityReadTransactional;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.grab.store.identity.internal.query.GetUserProfileQuery;
import com.grab.store.identity.internal.query.GetUserProfileResult;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.User;
import com.identity.domain.repository.AccessAssignmentRepository;
import com.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetUserProfileQueryHandler implements QueryHandler<GetUserProfileQuery, GetUserProfileResult> {

    private final UserRepository userRepository;
    private final AccessAssignmentRepository accessAssignmentRepository;

    @Override
    @IdentityReadTransactional
    public GetUserProfileResult handle(GetUserProfileQuery query) {
        User user = userRepository.findById(query.userId())
                .orElseThrow(() -> new IdentityServiceException(
                        new IdentityServiceError.UserNotFound(query.userId().getValue()),
                        "User not found"
                ));

        List<GetUserProfileResult.AccessContextInfo> contexts = accessAssignmentRepository.findByUser(user.getId())
                .stream()
                .map(this::toContextInfo)
                .toList();

        return new GetUserProfileResult(
                user.getId().getValue(),
                user.getEmail().value(),
                user.getStatus().name(),
                user.getCreatedAt().toString(),
                contexts
        );
    }

    @Override
    public Class<GetUserProfileQuery> getQueryType() {
        return GetUserProfileQuery.class;
    }

    private GetUserProfileResult.AccessContextInfo toContextInfo(AccessAssignment assignment) {
        return new GetUserProfileResult.AccessContextInfo(
                assignment.getId().getValue(),
                assignment.getPlatformCode(),
                assignment.getRoleCode(),
                assignment.getScope().key().value(),
                assignment.getScope().scopeId(),
                assignment.getStatus().name()
        );
    }
}
