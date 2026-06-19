package com.identity.infrastructure.repository.jpa;

import com.identity.domain.enums.UserStatus;
import com.identity.infrastructure.entity.ExternalIdentityEntity;
import com.identity.infrastructure.entity.UserEntity;
import com.identity.infrastructure.repository.jpa.config.RepositoryTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ExternalIdentityJpaRepositoryTest extends RepositoryTestConfig {

    @Autowired
    private ExternalIdentityJpaRepository externalIdentityJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        externalIdentityJpaRepository.deleteAll();
        userJpaRepository.deleteAll();

        user = new UserEntity();
        user.setUuid("uuid-user-1");
        user.setEmail("user@example.com");
        user.setPasswordHash("hashed-password");
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userJpaRepository.save(user);

        ExternalIdentityEntity googleIdentity = new ExternalIdentityEntity();
        googleIdentity.setUser(user);
        googleIdentity.setIssuer("google");
        googleIdentity.setSubject("google-sub-123");
        googleIdentity.setLinkedAt(LocalDateTime.now());

        ExternalIdentityEntity oktaIdentity = new ExternalIdentityEntity();
        oktaIdentity.setUser(user);
        oktaIdentity.setIssuer("okta");
        oktaIdentity.setSubject("okta-sub-456");
        oktaIdentity.setLinkedAt(LocalDateTime.now());

        externalIdentityJpaRepository.saveAll(java.util.List.of(googleIdentity, oktaIdentity));
    }

    @Test
    void findByIssuerAndSubject_returnsEntity_whenExists() {
        Optional<ExternalIdentityEntity> result = externalIdentityJpaRepository
                .findByIssuerAndSubject("google", "google-sub-123");

        assertThat(result).isPresent();
        assertThat(result.get().getUser().getUuid()).isEqualTo("uuid-user-1");
        assertThat(result.get().getIssuer()).isEqualTo("google");
        assertThat(result.get().getSubject()).isEqualTo("google-sub-123");
    }

    @Test
    void findByIssuerAndSubject_returnsEmpty_whenIssuerMismatch() {
        Optional<ExternalIdentityEntity> result = externalIdentityJpaRepository
                .findByIssuerAndSubject("azure", "google-sub-123");

        assertThat(result).isEmpty();
    }

    @Test
    void findByIssuerAndSubject_returnsEmpty_whenSubjectMismatch() {
        Optional<ExternalIdentityEntity> result = externalIdentityJpaRepository
                .findByIssuerAndSubject("google", "wrong-sub");

        assertThat(result).isEmpty();
    }

    @Test
    void findByIssuerAndSubject_returnsEmpty_whenNotExists() {
        Optional<ExternalIdentityEntity> result = externalIdentityJpaRepository
                .findByIssuerAndSubject("azure", "non-existent-sub");

        assertThat(result).isEmpty();
    }

    @Test
    void findByIssuerAndSubject_returnsDifferentIssuerIdentity() {
        Optional<ExternalIdentityEntity> result = externalIdentityJpaRepository
                .findByIssuerAndSubject("okta", "okta-sub-456");

        assertThat(result).isPresent();
        assertThat(result.get().getIssuer()).isEqualTo("okta");
        assertThat(result.get().getSubject()).isEqualTo("okta-sub-456");
    }
}
