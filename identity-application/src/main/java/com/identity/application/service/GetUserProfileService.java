package com.identity.application.service;

import com.identity.application.port.inbound.GetUserProfileUseCase;

import com.identity.application.exception.IdentityServiceError;
import com.identity.application.exception.IdentityServiceException;
import com.identity.application.model.read.GetUserProfileQuery;
import com.identity.application.model.read.GetUserProfileResult;
import com.identity.application.port.outbound.UserQueryPort;
import com.identity.application.model.read.UserAssignmentView;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetUserProfileService implements GetUserProfileUseCase {

    private final UserQueryPort userQueryPort;

    public GetUserProfileResult execute(GetUserProfileQuery query) {
        List<UserAssignmentView> userAssignmentViews = userQueryPort.queryUserAndByUserId(query.userId().getValue());
        
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
}
