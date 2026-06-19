package com.identity.infrastructure.repository.jpa;

import com.identity.infrastructure.entity.AuthorityEntity;
import com.identity.infrastructure.entity.RoleEntity;
import com.identity.infrastructure.repository.jpa.config.RepositoryTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class RoleJpaRepositoryTest extends RepositoryTestConfig {

    @Autowired
    private RoleJpaRepository roleJpaRepository;

    @Autowired
    private AuthorityJpaRepository authorityJpaRepository;

    private RoleEntity adminRole;
    private RoleEntity userRole;
    private RoleEntity viewerRole;

    @BeforeEach
    void setUp() {
        roleJpaRepository.deleteAll();
        authorityJpaRepository.deleteAll();

        AuthorityEntity readAuth = new AuthorityEntity();
        readAuth.setCode("READ");
        readAuth.setName("Read");
        readAuth.setActive(true);

        AuthorityEntity writeAuth = new AuthorityEntity();
        writeAuth.setCode("WRITE");
        writeAuth.setName("Write");
        writeAuth.setActive(true);

        authorityJpaRepository.saveAll(List.of(readAuth, writeAuth));

        adminRole = new RoleEntity();
        adminRole.setUuid("uuid-admin");
        adminRole.setCode("ADMIN");
        adminRole.setName("Administrator");
        adminRole.setDescription("Full access role");
        adminRole.setActive(true);
        adminRole.setAuthorities(Set.of(readAuth, writeAuth));

        userRole = new RoleEntity();
        userRole.setUuid("uuid-user");
        userRole.setCode("USER");
        userRole.setName("Standard User");
        userRole.setActive(true);
        userRole.setAuthorities(Set.of(readAuth));

        viewerRole = new RoleEntity();
        viewerRole.setUuid("uuid-viewer");
        viewerRole.setCode("VIEWER");
        viewerRole.setName("Viewer");
        viewerRole.setActive(false);

        roleJpaRepository.saveAll(List.of(adminRole, userRole, viewerRole));
    }

    @Test
    void findByCode_returnsEntity_whenExists() {
        Optional<RoleEntity> result = roleJpaRepository.findByCode("ADMIN");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Administrator");
        assertThat(result.get().getUuid()).isEqualTo("uuid-admin");
        assertThat(result.get().getDescription()).isEqualTo("Full access role");
        assertThat(result.get().isActive()).isTrue();
    }

    @Test
    void findByCode_returnsEmpty_whenNotExists() {
        Optional<RoleEntity> result = roleJpaRepository.findByCode("NON_EXISTENT");

        assertThat(result).isEmpty();
    }

    @Test
    void findByCode_loadsAuthorities() {
        Optional<RoleEntity> result = roleJpaRepository.findByCode("ADMIN");

        assertThat(result).isPresent();
        assertThat(result.get().getAuthorities()).hasSize(2);
        assertThat(result.get().getAuthorities())
                .extracting(AuthorityEntity::getCode)
                .containsExactlyInAnyOrder("READ", "WRITE");
    }

    @Test
    void findByCodeIn_returnsMatchingRoles() {
        List<RoleEntity> result = roleJpaRepository.findByCodeIn(List.of("ADMIN", "USER"));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(RoleEntity::getCode)
                .containsExactlyInAnyOrder("ADMIN", "USER");
    }

    @Test
    void findByCodeIn_returnsPartialMatch() {
        List<RoleEntity> result = roleJpaRepository.findByCodeIn(List.of("ADMIN", "UNKNOWN"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("ADMIN");
    }

    @Test
    void findByCodeIn_returnsEmpty_whenNoneMatch() {
        List<RoleEntity> result = roleJpaRepository.findByCodeIn(List.of("X", "Y"));

        assertThat(result).isEmpty();
    }

    @Test
    void findByCodeIn_returnsAllRequested() {
        List<RoleEntity> result = roleJpaRepository.findByCodeIn(List.of("ADMIN", "USER", "VIEWER"));

        assertThat(result).hasSize(3);
    }

    @Test
    void findAll_returnsAllRoles() {
        List<RoleEntity> result = roleJpaRepository.findAll();

        assertThat(result).hasSize(3);
        assertThat(result).extracting(RoleEntity::getCode)
                .containsExactlyInAnyOrder("ADMIN", "USER", "VIEWER");
    }
}
