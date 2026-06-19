package com.identity.infrastructure.repository.jpa;

import com.identity.domain.enums.UserStatus;
import com.identity.infrastructure.entity.RoleEntity;
import com.identity.infrastructure.entity.UserEntity;
import com.identity.infrastructure.repository.jpa.config.RepositoryTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class UserJpaRepositoryTest extends RepositoryTestConfig {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private RoleJpaRepository roleJpaRepository;

    private UserEntity activeUser;
    private UserEntity suspendedUser;

    @BeforeEach
    void setUp() {
        userJpaRepository.deleteAll();
        roleJpaRepository.deleteAll();

        RoleEntity adminRole = new RoleEntity();
        adminRole.setUuid("uuid-role-admin");
        adminRole.setCode("ADMIN");
        adminRole.setName("Administrator");
        adminRole.setActive(true);
        roleJpaRepository.save(adminRole);

        activeUser = new UserEntity();
        activeUser.setUuid("uuid-user-active");
        activeUser.setEmail("active@example.com");
        activeUser.setPasswordHash("hashed-pwd-1");
        activeUser.setStatus(UserStatus.ACTIVE);
        activeUser.setRoles(Set.of(adminRole));
        activeUser.setCreatedAt(LocalDateTime.now());
        activeUser.setUpdatedAt(LocalDateTime.now());

        suspendedUser = new UserEntity();
        suspendedUser.setUuid("uuid-user-suspended");
        suspendedUser.setEmail("suspended@example.com");
        suspendedUser.setPasswordHash("hashed-pwd-2");
        suspendedUser.setStatus(UserStatus.SUSPENDED);
        suspendedUser.setCreatedAt(LocalDateTime.now());
        suspendedUser.setUpdatedAt(LocalDateTime.now());

        userJpaRepository.saveAll(List.of(activeUser, suspendedUser));
    }

    @Test
    void findByUuid_returnsEntity_whenExists() {
        Optional<UserEntity> result = userJpaRepository.findByUuid("uuid-user-active");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("active@example.com");
        assertThat(result.get().getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void findByUuid_returnsEmpty_whenNotExists() {
        Optional<UserEntity> result = userJpaRepository.findByUuid("non-existent-uuid");

        assertThat(result).isEmpty();
    }

    @Test
    void findByEmail_returnsEntity_whenExists() {
        Optional<UserEntity> result = userJpaRepository.findByEmail("active@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getUuid()).isEqualTo("uuid-user-active");
    }

    @Test
    void findByEmail_returnsEmpty_whenNotExists() {
        Optional<UserEntity> result = userJpaRepository.findByEmail("unknown@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    void existsByEmail_returnsTrue_whenExists() {
        boolean result = userJpaRepository.existsByEmail("active@example.com");

        assertThat(result).isTrue();
    }

    @Test
    void existsByEmail_returnsFalse_whenNotExists() {
        boolean result = userJpaRepository.existsByEmail("unknown@example.com");

        assertThat(result).isFalse();
    }

    @Test
    void findByUuid_loadsRoles() {
        Optional<UserEntity> result = userJpaRepository.findByUuid("uuid-user-active");

        assertThat(result).isPresent();
        assertThat(result.get().getRoles()).hasSize(1);
        assertThat(result.get().getRoles().iterator().next().getCode()).isEqualTo("ADMIN");
    }

    @Test
    void findByEmail_returnsSuspendedUser() {
        Optional<UserEntity> result = userJpaRepository.findByEmail("suspended@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(UserStatus.SUSPENDED);
    }

    @Test
    void save_persistsNewUser() {
        UserEntity newUser = new UserEntity();
        newUser.setUuid("uuid-user-new");
        newUser.setEmail("new@example.com");
        newUser.setPasswordHash("hashed-pwd-new");
        newUser.setStatus(UserStatus.PENDING_APPROVAL);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        UserEntity saved = userJpaRepository.save(newUser);

        assertThat(saved.getId()).isNotNull();
        assertThat(userJpaRepository.findByUuid("uuid-user-new")).isPresent();
        assertThat(userJpaRepository.existsByEmail("new@example.com")).isTrue();
    }

    @Test
    void findAll_returnsAllUsers() {
        List<UserEntity> result = userJpaRepository.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserEntity::getEmail)
                .containsExactlyInAnyOrder("active@example.com", "suspended@example.com");
    }
}
