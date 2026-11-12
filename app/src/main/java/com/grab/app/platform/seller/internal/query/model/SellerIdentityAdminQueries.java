package com.grab.app.platform.seller.internal.query.model;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.*;
import com.grab.framework.cqrs.query.Query;

import java.util.List;
import java.util.Optional;

public final class SellerIdentityAdminQueries {

    private SellerIdentityAdminQueries() {}

    public record GetUserDetailQuery(
            String userId
    ) implements Query<Optional<UserProfileDetailResponse>> {}

    public record ListUsersQuery(
            int page,
            int size
    ) implements Query<List<SellerUserProfileResponse>> {}

    public record ListRolesQuery(
            String name,
            int page,
            int size
    ) implements Query<List<RoleResponse>> {}

    public record SearchRolesQuery(
            String name
    ) implements Query<List<RoleSearchResponse>> {}

    public record ListAccessAssignmentsForUserQuery(
            String userId,
            String actorScopeKey,
            String actorScopeId
    ) implements Query<List<AccessAssignmentResponse>> {}

    public record ListAvailableAccessContextsQuery(
            String userId
    ) implements Query<List<AccessContextResponse>> {}
}

