package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.entity.UserEntity;
import com.identity.application.model.read.UserAssignmentView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUuid(String uuid);
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("""
            SELECT new com.identity.application.model.read.UserAssignmentView(
                   u.uuid, u.email, u.status, u.createdAt,
                   aa.uuid, p.code, r.code, aa.scopeKey, aa.scopeId, aa.status)
            FROM UserEntity u
            LEFT JOIN AccessAssignmentEntity aa ON aa.user.uuid = u.uuid
            LEFT JOIN aa.platformRole pr
            LEFT JOIN pr.platform p
            LEFT JOIN pr.role r
            WHERE u.uuid = :userId
            ORDER BY aa.createdAt
            """)
    List<UserAssignmentView> queryUserAndByUserId(@Param("userId") String userId);
}
