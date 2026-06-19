package com.grab.store.identity.internal.api.rest.service;

import com.grab.store.identity.internal.api.rest.dto.request.CreateRoleRequest;
import com.grab.store.identity.internal.api.rest.dto.response.*;
import com.grab.store.identity.internal.config.IdentityReadTransactional;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.identity.domain.enums.UserStatus;
import com.identity.infrastructure.entity.*;
import com.identity.infrastructure.repository.jpa.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import com.grab.store.identity.internal.exception.*;

@Service
@RequiredArgsConstructor
public class IdentityAdminService {
    private final UserJpaRepository users;
    private final RoleJpaRepository roles;
    private final AuthorityJpaRepository authorities;
    private final RefreshSessionJpaRepository sessions;

    @IdentityReadTransactional
    public UserProfileResponse profile(String id) {
        return toUser(users.findByUuid(id).orElseThrow(() -> userNotFound(id)));
    }

    @IdentityReadTransactional
    public Page<UserProfileResponse> users(Pageable pageable) {
        return users.findAll(pageable).map(this::toUser);
    }

    @IdentityReadTransactional
    public UserProfileResponse status(String id, UserStatus status) {
        UserEntity u = users.findByUuid(id).orElseThrow(() -> userNotFound(id));
        u.setStatus(status);
        if (status == UserStatus.SUSPENDED)
            sessions.findByUser_Uuid(id).forEach(s -> s.setRevokedAt(java.time.Instant.now()));
        return toUser(users.save(u));
    }

    @IdentityReadTransactional
    public UserProfileResponse assignRole(String id, String code, boolean assign) {
        UserEntity u = users.findByUuid(id).orElseThrow(() -> userNotFound(id));
        RoleEntity r = roles.findByCode(code).orElseThrow(() -> roleNotFound(code));
        if (assign) u.getRoles().add(r);
        else u.getRoles().remove(r);
        return toUser(users.save(u));
    }

    @IdentityTransactional
    public RoleResponse createRole(CreateRoleRequest request) {
        if (roles.findByCode(request.code()).isPresent())
            throw new IdentityServiceException(new IdentityServiceError.RoleExists(request.code()), "Role exists");
        RoleEntity r = new RoleEntity();
        r.setUuid(UUID.randomUUID().toString());
        r.setCode(request.code());
        r.setName(request.name());
        r.setDescription(request.description());
        r.setActive(true);
        return toRole(roles.save(r));
    }

    @IdentityTransactional
    public List<RoleResponse> roles() {
        return roles.findAll().stream().map(this::toRole).toList();
    }

    @IdentityTransactional
    public RoleResponse authority(String roleCode, String authorityCode, boolean assign) {
        RoleEntity r = roles.findByCode(roleCode).orElseThrow(() -> roleNotFound(roleCode));
        AuthorityEntity a = authorities.findByCode(authorityCode).orElseThrow(() -> roleNotFound(authorityCode));
        if (assign) r.getAuthorities().add(a);
        else r.getAuthorities().remove(a);
        return toRole(roles.save(r));
    }

    private UserProfileResponse toUser(UserEntity u) {
        return new UserProfileResponse(u.getUuid(), u.getEmail(), u.getRoles().stream().map(RoleEntity::getCode).collect(Collectors.toSet()), u.getStatus().name(), u.getCreatedAt());
    }

    private RoleResponse toRole(RoleEntity r) {
        return new RoleResponse(r.getCode(), r.getName(), r.getDescription(), r.isActive(), r.getAuthorities().stream().map(AuthorityEntity::getCode).collect(Collectors.toSet()));
    }

    private IdentityServiceException userNotFound(String id) {
        return new IdentityServiceException(new IdentityServiceError.UserNotFound(id), "User not found");
    }

    private IdentityServiceException roleNotFound(String code) {
        return new IdentityServiceException(new IdentityServiceError.RoleNotFound(code), "Role or authority not found");
    }
}
