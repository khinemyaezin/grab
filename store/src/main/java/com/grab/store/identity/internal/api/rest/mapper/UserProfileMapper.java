package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.store.identity.internal.api.rest.dto.response.UserProfileResponse;
import com.grab.store.identity.internal.command.UserProfileResult;
import com.grab.store.identity.internal.command.ChangeUserStatusCommand;
import com.grab.store.identity.internal.command.AssignRoleCommand;
import com.grab.store.identity.internal.query.GetUserProfileQuery;
import com.identity.domain.enums.UserStatus;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class UserProfileMapper {
    public abstract GetUserProfileQuery toQuery(String userId);
    public abstract ChangeUserStatusCommand toCommand(String userId, UserStatus status);
    public abstract AssignRoleCommand toCommand(String userId, String roleCode, boolean assign);
    public abstract UserProfileResponse toResponse(UserProfileResult result);
}
