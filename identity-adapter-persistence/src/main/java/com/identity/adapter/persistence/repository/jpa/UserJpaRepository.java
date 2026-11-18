package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.entity.UserEntity;
import com.identity.application.model.read.ActiveSubjectView;
import com.identity.application.model.read.UserAssignmentView;
import com.identity.domain.enums.UserStatus;
import com.identity.application.model.read.CredentialView;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUuid(String uuid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserEntity u WHERE u.uuid = :uuid")
    Optional<UserEntity> findByUuidForUpdate(@Param("uuid") String uuid);
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("""
            SELECT new com.identity.application.model.read.CredentialView(
                u.uuid, u.passwordHash, u.status, u.authenticationVersion)
            FROM UserEntity u
            WHERE u.email = :email
            """)
    Optional<CredentialView> findCredentialView(@Param("email") String email);

    @Query("""
            SELECT new com.identity.application.model.read.ActiveSubjectView(u.uuid, u.authenticationVersion)
            FROM UserEntity u
            WHERE u.uuid = :uuid AND u.status = :status
            """)
    Optional<ActiveSubjectView> findSubject(@Param("uuid") String uuid, @Param("status") UserStatus status);

    @Query("""
            SELECT new com.identity.application.model.read.UserAssignmentView(
                   u.uuid, u.email, u.status, u.createdAt,
                   aa.uuid, r.code, aa.scopeKey, aa.scopeId, aa.status)
            FROM UserEntity u
            LEFT JOIN AccessAssignmentEntity aa ON aa.user.uuid = u.uuid
            LEFT JOIN aa.role r
            WHERE u.uuid = :userId
            ORDER BY aa.createdAt
            """)
    List<UserAssignmentView> queryUserAndByUserId(@Param("userId") String userId);
}
