package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.entity.AuthorityEntity;
import com.identity.adapter.persistence.repository.jpa.config.RepositoryTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthorityJpaRepositoryTest extends RepositoryTestConfig {

    @Autowired
    private AuthorityJpaRepository authorityJpaRepository;

    @Autowired
    private EntityManager entityManager;

    private AuthorityEntity readAuthority;
    private AuthorityEntity writeAuthority;

    @BeforeEach
    void setUp() {
        authorityJpaRepository.deleteAll();

        readAuthority = new AuthorityEntity();
        readAuthority.setUuid("authority-read");
        readAuthority.setCode("READ");
        readAuthority.setCategory("identity");
        readAuthority.setOwnerKey("identity");
        readAuthority.setSourceRevision(1);
        readAuthority.setName("Read Permission");
        readAuthority.setDescription("Allows read access");
        readAuthority.setActive(true);

        writeAuthority = new AuthorityEntity();
        writeAuthority.setUuid("authority-write");
        writeAuthority.setCode("WRITE");
        writeAuthority.setCategory("identity");
        writeAuthority.setOwnerKey("identity");
        writeAuthority.setSourceRevision(1);
        writeAuthority.setName("Write Permission");
        writeAuthority.setDescription("Allows write access");
        writeAuthority.setActive(false);

        authorityJpaRepository.saveAll(List.of(readAuthority, writeAuthority));
    }

    @Test
    void findByCode_returnsEntity_whenExists() {
        Optional<AuthorityEntity> result = authorityJpaRepository.findByCode("READ");

        assertThat(result).isPresent();
        assertThat(result.get().getUuid()).isEqualTo("authority-read");
        assertThat(result.get().getCategory()).isEqualTo("identity");
        assertThat(result.get().getName()).isEqualTo("Read Permission");
        assertThat(result.get().getDescription()).isEqualTo("Allows read access");
        assertThat(result.get().isActive()).isTrue();
    }

    @Test
    void findByCode_returnsEmpty_whenNotExists() {
        Optional<AuthorityEntity> result = authorityJpaRepository.findByCode("NON_EXISTENT");

        assertThat(result).isEmpty();
    }

    @Test
    void findByCode_returnsInactiveAuthority() {
        Optional<AuthorityEntity> result = authorityJpaRepository.findByCode("WRITE");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Write Permission");
        assertThat(result.get().isActive()).isFalse();
    }

    @Test
    void findAll_returnsAllAuthorities() {
        List<AuthorityEntity> result = authorityJpaRepository.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(AuthorityEntity::getCode)
                .containsExactlyInAnyOrder("READ", "WRITE");
    }

    @Test
    void save_persistsNewAuthority() {
        AuthorityEntity deleteAuthority = new AuthorityEntity();
        deleteAuthority.setUuid("authority-delete");
        deleteAuthority.setCode("DELETE");
        deleteAuthority.setCategory("identity");
        deleteAuthority.setOwnerKey("identity");
        deleteAuthority.setSourceRevision(1);
        deleteAuthority.setName("Delete Permission");
        deleteAuthority.setActive(true);

        AuthorityEntity saved = authorityJpaRepository.save(deleteAuthority);

        assertThat(saved.getId()).isNotNull();
        assertThat(authorityJpaRepository.findByCode("DELETE")).isPresent();
    }

    @Test
    void upsertByCode_updatesMetadataAndPreservesInactiveStatus() {
        authorityJpaRepository.saveAndFlush(writeAuthority);

        authorityJpaRepository.upsertByCode(
                "authority-replacement",
                "WRITE",
                "merchant",
                "Updated write permission",
                "Updated description"
        );
        entityManager.clear();

        AuthorityEntity updated = authorityJpaRepository.findByCode("WRITE").orElseThrow();
        assertThat(updated.getUuid()).isEqualTo("authority-write");
        assertThat(updated.getCategory()).isEqualTo("merchant");
        assertThat(updated.getName()).isEqualTo("Updated write permission");
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        assertThat(updated.isActive()).isFalse();
        assertThat(authorityJpaRepository.count()).isEqualTo(2);
    }

    @Test
    void upsertByCode_insertsNewAuthorityAsActiveAndIsIdempotent() {
        authorityJpaRepository.upsertByCode("authority-new", "NEW", "identity", "New permission", "New description");
        authorityJpaRepository.upsertByCode("authority-new", "NEW", "identity", "New permission v2", "New description v2");
        entityManager.clear();

        AuthorityEntity created = authorityJpaRepository.findByCode("NEW").orElseThrow();
        assertThat(created.getName()).isEqualTo("New permission v2");
        assertThat(created.getDescription()).isEqualTo("New description v2");
        assertThat(created.isActive()).isTrue();
        assertThat(authorityJpaRepository.count()).isEqualTo(3);
    }
}
