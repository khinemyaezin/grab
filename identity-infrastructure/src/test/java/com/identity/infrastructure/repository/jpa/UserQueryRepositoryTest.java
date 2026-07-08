package com.identity.infrastructure.repository.jpa;

import com.identity.domain.enums.AccessAssignmentStatus;
import com.identity.domain.enums.UserStatus;
import com.identity.infrastructure.entity.UserEntity;
import com.identity.infrastructure.repository.jpa.config.RepositoryTestConfig;
import com.identity.infrastructure.view.UserAssignmentView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class UserQueryRepositoryTest extends RepositoryTestConfig {

    @Autowired
    private UserQueryRepository userQueryRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    private UserEntity activeUser;

    @BeforeEach
    void setUp() {
        userJpaRepository.deleteAll();

        activeUser = new UserEntity();
        activeUser.setUuid("uuid-user-query-1");
        activeUser.setEmail("query1@example.com");
        activeUser.setPasswordHash("hashed-pwd-1");
        activeUser.setStatus(UserStatus.ACTIVE);
        activeUser.setCreatedAt(LocalDateTime.now());
        activeUser.setUpdatedAt(LocalDateTime.now());

        userJpaRepository.save(activeUser);
    }

    @Test
    void queryUserProfileByUserId_returnsAndWithoutAssignments_whenNoAssignmentsExist() {
        List<UserAssignmentView> result = userQueryRepository.queryUserAndByUserId("uuid-user-query-1");

        assertThat(result).hasSize(1);
        UserAssignmentView view = result.getFirst();
        assertThat(view.userId()).isEqualTo("uuid-user-query-1");
        assertThat(view.email()).isEqualTo("query1@example.com");
        assertThat(view.userStatus()).isEqualTo("ACTIVE");
        assertThat(view.createdAt()).isNotNull();
        assertThat(view.assignmentId()).isNull();
        assertThat(view.platformCode()).isNull();
        assertThat(view.roleCode()).isNull();
        assertThat(view.scopeKey()).isNull();
        assertThat(view.scopeId()).isNull();
        assertThat(view.assignmentStatus()).isNull();
    }

    @Test
    void queryUserProfileByUserId_returnsEmptyList_whenUserDoesNotExist() {
        List<UserAssignmentView> result = userQueryRepository.queryUserAndByUserId("non-existent-uuid");

        assertThat(result).isEmpty();
    }

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Test
    void queryUserProfileByUserId_returnsMultipleAssignments_whenUserHasMultipleAssignments() {
        // Setup Platform
        com.identity.infrastructure.entity.PlatformEntity platform = new com.identity.infrastructure.entity.PlatformEntity();
        platform.setUuid("uuid-platform");
        platform.setCode("PLATFORM_1");
        platform.setName("Platform 1");
        entityManager.persist(platform);

        // Setup Roles
        com.identity.infrastructure.entity.RoleEntity role1 = new com.identity.infrastructure.entity.RoleEntity();
        role1.setUuid("uuid-role-1");
        role1.setCode("ROLE_1");
        role1.setName("Role 1");
        entityManager.persist(role1);

        com.identity.infrastructure.entity.RoleEntity role2 = new com.identity.infrastructure.entity.RoleEntity();
        role2.setUuid("uuid-role-2");
        role2.setCode("ROLE_2");
        role2.setName("Role 2");
        entityManager.persist(role2);

        // Setup PlatformRoles
        com.identity.infrastructure.entity.PlatformRoleEntity pr1 = new com.identity.infrastructure.entity.PlatformRoleEntity();
        pr1.setUuid("uuid-pr-1");
        pr1.setPlatform(platform);
        pr1.setRole(role1);
        entityManager.persist(pr1);

        com.identity.infrastructure.entity.PlatformRoleEntity pr2 = new com.identity.infrastructure.entity.PlatformRoleEntity();
        pr2.setUuid("uuid-pr-2");
        pr2.setPlatform(platform);
        pr2.setRole(role2);
        entityManager.persist(pr2);

        // Setup AccessAssignments
        com.identity.infrastructure.entity.AccessAssignmentEntity assignment1 = new com.identity.infrastructure.entity.AccessAssignmentEntity();
        assignment1.setUuid("uuid-assign-1");
        assignment1.setUser(activeUser);
        assignment1.setPlatformRole(pr1);
        assignment1.setScopeKey("GLOBAL");
        assignment1.setScopeId("ALL");
        assignment1.setStatus(AccessAssignmentStatus.ACTIVE);
        assignment1.setCreatedAt(Instant.now());
        assignment1.setUpdatedAt(Instant.now());
        entityManager.persist(assignment1);

        com.identity.infrastructure.entity.AccessAssignmentEntity assignment2 = new com.identity.infrastructure.entity.AccessAssignmentEntity();
        assignment2.setUuid("uuid-assign-2");
        assignment2.setUser(activeUser);
        assignment2.setPlatformRole(pr2);
        assignment2.setScopeKey("MERCHANT");
        assignment2.setScopeId("M-123");
        assignment2.setStatus(AccessAssignmentStatus.ACTIVE);
        assignment2.setCreatedAt(Instant.now().plusSeconds(60));
        assignment2.setUpdatedAt(Instant.now().plusSeconds(60));
        entityManager.persist(assignment2);

        entityManager.flush();

        // Execution
        List<UserAssignmentView> result = userQueryRepository.queryUserAndByUserId(activeUser.getUuid());

        // Verification
        assertThat(result).hasSize(2);
        
        assertThat(result).extracting(UserAssignmentView::assignmentId)
                .containsExactlyInAnyOrder("uuid-assign-1", "uuid-assign-2");

        assertThat(result).extracting(UserAssignmentView::roleCode)
                .containsExactlyInAnyOrder("ROLE_1", "ROLE_2");
    }
}
