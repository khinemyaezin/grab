package com.identity.infrastructure.repository.jpa;

import com.identity.infrastructure.entity.AuthorityEntity;
import com.identity.infrastructure.repository.jpa.config.RepositoryTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthorityJpaRepositoryTest extends RepositoryTestConfig {

    @Autowired
    private AuthorityJpaRepository authorityJpaRepository;

    private AuthorityEntity readAuthority;
    private AuthorityEntity writeAuthority;

    @BeforeEach
    void setUp() {
        authorityJpaRepository.deleteAll();

        readAuthority = new AuthorityEntity();
        readAuthority.setCode("READ");
        readAuthority.setName("Read Permission");
        readAuthority.setDescription("Allows read access");
        readAuthority.setActive(true);

        writeAuthority = new AuthorityEntity();
        writeAuthority.setCode("WRITE");
        writeAuthority.setName("Write Permission");
        writeAuthority.setDescription("Allows write access");
        writeAuthority.setActive(false);

        authorityJpaRepository.saveAll(List.of(readAuthority, writeAuthority));
    }

    @Test
    void findByCode_returnsEntity_whenExists() {
        Optional<AuthorityEntity> result = authorityJpaRepository.findByCode("READ");

        assertThat(result).isPresent();
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
        deleteAuthority.setCode("DELETE");
        deleteAuthority.setName("Delete Permission");
        deleteAuthority.setActive(true);

        AuthorityEntity saved = authorityJpaRepository.save(deleteAuthority);

        assertThat(saved.getId()).isNotNull();
        assertThat(authorityJpaRepository.findByCode("DELETE")).isPresent();
    }
}
