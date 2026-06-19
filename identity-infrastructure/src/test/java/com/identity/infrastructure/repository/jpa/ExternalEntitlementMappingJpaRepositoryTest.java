package com.identity.infrastructure.repository.jpa;

import com.identity.infrastructure.entity.ExternalEntitlementMappingEntity;
import com.identity.infrastructure.entity.RoleEntity;
import com.identity.infrastructure.repository.jpa.config.RepositoryTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ExternalEntitlementMappingJpaRepositoryTest extends RepositoryTestConfig {

    @Autowired
    private ExternalEntitlementMappingJpaRepository mappingJpaRepository;

    @Autowired
    private RoleJpaRepository roleJpaRepository;

    private RoleEntity adminRole;
    private RoleEntity viewerRole;

    @BeforeEach
    void setUp() {
        mappingJpaRepository.deleteAll();
        roleJpaRepository.deleteAll();

        adminRole = new RoleEntity();
        adminRole.setUuid("uuid-role-admin");
        adminRole.setCode("ADMIN");
        adminRole.setName("Administrator");
        adminRole.setActive(true);

        viewerRole = new RoleEntity();
        viewerRole.setUuid("uuid-role-viewer");
        viewerRole.setCode("VIEWER");
        viewerRole.setName("Viewer");
        viewerRole.setActive(true);

        roleJpaRepository.saveAll(List.of(adminRole, viewerRole));

        ExternalEntitlementMappingEntity mapping1 = new ExternalEntitlementMappingEntity();
        mapping1.setIssuer("google");
        mapping1.setEntitlement("admin-group");
        mapping1.setRole(adminRole);

        ExternalEntitlementMappingEntity mapping2 = new ExternalEntitlementMappingEntity();
        mapping2.setIssuer("google");
        mapping2.setEntitlement("viewer-group");
        mapping2.setRole(viewerRole);

        ExternalEntitlementMappingEntity mapping3 = new ExternalEntitlementMappingEntity();
        mapping3.setIssuer("okta");
        mapping3.setEntitlement("admin-group");
        mapping3.setRole(adminRole);

        mappingJpaRepository.saveAll(List.of(mapping1, mapping2, mapping3));
    }

    @Test
    void findByIssuerAndEntitlementIn_returnsMappings_whenIssuerAndEntitlementsMatch() {
        List<ExternalEntitlementMappingEntity> result = mappingJpaRepository
                .findByIssuerAndEntitlementIn("google", List.of("admin-group", "viewer-group"));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(m -> m.getRole().getCode())
                .containsExactlyInAnyOrder("ADMIN", "VIEWER");
    }

    @Test
    void findByIssuerAndEntitlementIn_returnsSubset_whenPartialEntitlementMatch() {
        List<ExternalEntitlementMappingEntity> result = mappingJpaRepository
                .findByIssuerAndEntitlementIn("google", List.of("admin-group"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRole().getCode()).isEqualTo("ADMIN");
    }

    @Test
    void findByIssuerAndEntitlementIn_returnsEmpty_whenIssuerMismatch() {
        List<ExternalEntitlementMappingEntity> result = mappingJpaRepository
                .findByIssuerAndEntitlementIn("azure", List.of("admin-group"));

        assertThat(result).isEmpty();
    }

    @Test
    void findByIssuerAndEntitlementIn_returnsEmpty_whenNoEntitlementMatch() {
        List<ExternalEntitlementMappingEntity> result = mappingJpaRepository
                .findByIssuerAndEntitlementIn("google", List.of("unknown-group"));

        assertThat(result).isEmpty();
    }

    @Test
    void findByIssuerAndEntitlementIn_returnsMappingsForDifferentIssuer() {
        List<ExternalEntitlementMappingEntity> result = mappingJpaRepository
                .findByIssuerAndEntitlementIn("okta", List.of("admin-group"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIssuer()).isEqualTo("okta");
        assertThat(result.get(0).getRole().getCode()).isEqualTo("ADMIN");
    }
}
