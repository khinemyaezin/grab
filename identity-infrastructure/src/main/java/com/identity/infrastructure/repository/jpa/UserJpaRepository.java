package com.identity.infrastructure.repository.jpa;

import com.identity.infrastructure.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUuid(String uuid);
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}
