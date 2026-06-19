package com.identity.infrastructure.repository.jpa;

import com.identity.domain.enums.UserStatus;
import com.identity.infrastructure.entity.RefreshSessionEntity;
import com.identity.infrastructure.entity.UserEntity;
import com.identity.infrastructure.repository.jpa.config.RepositoryTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class RefreshSessionJpaRepositoryTest extends RepositoryTestConfig {

    @Autowired
    private RefreshSessionJpaRepository refreshSessionJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    private UserEntity user1;
    private UserEntity user2;

    @BeforeEach
    void setUp() {
        refreshSessionJpaRepository.deleteAll();
        userJpaRepository.deleteAll();

        user1 = new UserEntity();
        user1.setUuid("uuid-user-1");
        user1.setEmail("user1@example.com");
        user1.setPasswordHash("hash1");
        user1.setStatus(UserStatus.ACTIVE);
        user1.setCreatedAt(LocalDateTime.now());
        user1.setUpdatedAt(LocalDateTime.now());

        user2 = new UserEntity();
        user2.setUuid("uuid-user-2");
        user2.setEmail("user2@example.com");
        user2.setPasswordHash("hash2");
        user2.setStatus(UserStatus.ACTIVE);
        user2.setCreatedAt(LocalDateTime.now());
        user2.setUpdatedAt(LocalDateTime.now());

        userJpaRepository.saveAll(List.of(user1, user2));

        Instant now = Instant.now();

        RefreshSessionEntity session1 = new RefreshSessionEntity();
        session1.setUser(user1);
        session1.setTokenHash("token-hash-aaa");
        session1.setTokenFamilyId("family-1");
        session1.setExpiresAt(now.plusSeconds(3600));
        session1.setCreatedAt(now);

        RefreshSessionEntity session2 = new RefreshSessionEntity();
        session2.setUser(user1);
        session2.setTokenHash("token-hash-bbb");
        session2.setTokenFamilyId("family-1");
        session2.setExpiresAt(now.plusSeconds(3600));
        session2.setCreatedAt(now);

        RefreshSessionEntity session3 = new RefreshSessionEntity();
        session3.setUser(user2);
        session3.setTokenHash("token-hash-ccc");
        session3.setTokenFamilyId("family-2");
        session3.setExpiresAt(now.plusSeconds(3600));
        session3.setCreatedAt(now);

        refreshSessionJpaRepository.saveAll(List.of(session1, session2, session3));
    }

    @Test
    void findByTokenHash_returnsEntity_whenExists() {
        Optional<RefreshSessionEntity> result = refreshSessionJpaRepository.findByTokenHash("token-hash-aaa");

        assertThat(result).isPresent();
        assertThat(result.get().getTokenFamilyId()).isEqualTo("family-1");
        assertThat(result.get().getUser().getUuid()).isEqualTo("uuid-user-1");
    }

    @Test
    void findByTokenHash_returnsEmpty_whenNotExists() {
        Optional<RefreshSessionEntity> result = refreshSessionJpaRepository.findByTokenHash("non-existent-hash");

        assertThat(result).isEmpty();
    }

    @Test
    void findByTokenFamilyId_returnsAllSessionsInFamily() {
        List<RefreshSessionEntity> result = refreshSessionJpaRepository.findByTokenFamilyId("family-1");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(s -> s.getTokenHash())
                .containsExactlyInAnyOrder("token-hash-aaa", "token-hash-bbb");
    }

    @Test
    void findByTokenFamilyId_returnsEmpty_whenFamilyNotExists() {
        List<RefreshSessionEntity> result = refreshSessionJpaRepository.findByTokenFamilyId("non-existent-family");

        assertThat(result).isEmpty();
    }

    @Test
    void findByTokenFamilyId_returnsSingleSession_forFamily2() {
        List<RefreshSessionEntity> result = refreshSessionJpaRepository.findByTokenFamilyId("family-2");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTokenHash()).isEqualTo("token-hash-ccc");
    }

    @Test
    void findByUser_Uuid_returnsSessionsForUser() {
        List<RefreshSessionEntity> result = refreshSessionJpaRepository.findByUser_Uuid("uuid-user-1");

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(s -> s.getUser().getUuid().equals("uuid-user-1"));
    }

    @Test
    void findByUser_Uuid_returnsEmpty_whenUserHasNoSessions() {
        List<RefreshSessionEntity> result = refreshSessionJpaRepository.findByUser_Uuid("non-existent-user");

        assertThat(result).isEmpty();
    }

    @Test
    void findByUser_Uuid_returnsSingleSession_forUser2() {
        List<RefreshSessionEntity> result = refreshSessionJpaRepository.findByUser_Uuid("uuid-user-2");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getEmail()).isEqualTo("user2@example.com");
    }
}
