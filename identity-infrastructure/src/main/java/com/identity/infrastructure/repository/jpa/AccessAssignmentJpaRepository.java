package com.identity.infrastructure.repository.jpa;

import com.identity.infrastructure.entity.AccessAssignmentEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AccessAssignmentJpaRepository extends JpaRepository<AccessAssignmentEntity, Long> {
    boolean existsByUser_Uuid(String userId);

    @EntityGraph(attributePaths = {"user", "platformRole", "platformRole.platform", "platformRole.role", "platformRole.role.authorities"})
    Optional<AccessAssignmentEntity> findByUuid(String uuid);

    @EntityGraph(attributePaths = {"user", "platformRole", "platformRole.platform", "platformRole.role", "platformRole.role.authorities"})
    @Query("""
            select assignment from AccessAssignmentEntity assignment
            where assignment.user.uuid = :userId
              and assignment.platformRole.platform.code = :platformCode
              and assignment.platformRole.platform.active = true
              and assignment.platformRole.active = true
              and assignment.platformRole.role.active = true
              and assignment.status = com.identity.domain.enums.AccessAssignmentStatus.ACTIVE
              and (assignment.expiresAt is null or assignment.expiresAt > :now)
            order by assignment.createdAt
            """)
    List<AccessAssignmentEntity> findEffectiveByUserAndPlatform(
            @Param("userId") String userId,
            @Param("platformCode") String platformCode,
            @Param("now") Instant now
    );

    @EntityGraph(attributePaths = {"user", "platformRole", "platformRole.platform", "platformRole.role", "platformRole.role.authorities"})
    @Query("""
            select assignment from AccessAssignmentEntity assignment
            where assignment.uuid = :assignmentId
              and assignment.user.uuid = :userId
              and assignment.platformRole.platform.code = :platformCode
            """)
    Optional<AccessAssignmentEntity> findForContext(
            @Param("assignmentId") String assignmentId,
            @Param("userId") String userId,
            @Param("platformCode") String platformCode
    );

    @EntityGraph(attributePaths = {"user", "platformRole", "platformRole.platform", "platformRole.role"})
    @Query("""
            select assignment from AccessAssignmentEntity assignment
            where assignment.user.uuid = :userId
              and assignment.platformRole.platform.code = :platformCode
              and assignment.platformRole.role.code = :roleCode
              and assignment.scopeKey = :scopeKey
              and assignment.scopeId = :scopeId
              and assignment.status in (
                  com.identity.domain.enums.AccessAssignmentStatus.ACTIVE,
                  com.identity.domain.enums.AccessAssignmentStatus.SUSPENDED
              )
            """)
    Optional<AccessAssignmentEntity> findCurrent(
            @Param("userId") String userId,
            @Param("platformCode") String platformCode,
            @Param("roleCode") String roleCode,
            @Param("scopeKey") String scopeKey,
            @Param("scopeId") String scopeId
    );

    @Query("""
            select (count(assignment) > 0) from AccessAssignmentEntity assignment
            where assignment.user.uuid = :userId
              and assignment.platformRole.platform.code = :platformCode
              and assignment.platformRole.role.code = :roleCode
              and assignment.scopeKey = :scopeKey
              and assignment.scopeId = :scopeId
              and assignment.status = com.identity.domain.enums.AccessAssignmentStatus.ACTIVE
              and assignment.platformRole.platform.active = true
              and assignment.platformRole.active = true
              and assignment.platformRole.role.active = true
              and (assignment.expiresAt is null or assignment.expiresAt > :now)
            """)
    boolean existsEffective(
            @Param("userId") String userId,
            @Param("platformCode") String platformCode,
            @Param("roleCode") String roleCode,
            @Param("scopeKey") String scopeKey,
            @Param("scopeId") String scopeId,
            @Param("now") Instant now
    );

    @Query("""
            select (count(assignment) > 0) from AccessAssignmentEntity assignment
            where assignment.user.uuid = :userId
              and assignment.platformRole.platform.code = :platformCode
              and assignment.platformRole.role.code = :roleCode
              and assignment.scopeKey = :scopeKey
              and assignment.scopeId = :scopeId
              and assignment.status in (
                  com.identity.domain.enums.AccessAssignmentStatus.ACTIVE,
                  com.identity.domain.enums.AccessAssignmentStatus.SUSPENDED
              )
            """)
    boolean existsCurrent(
            @Param("userId") String userId,
            @Param("platformCode") String platformCode,
            @Param("roleCode") String roleCode,
            @Param("scopeKey") String scopeKey,
            @Param("scopeId") String scopeId
    );

    @EntityGraph(attributePaths = {"user", "platformRole", "platformRole.platform", "platformRole.role"})
    List<AccessAssignmentEntity> findByUser_UuidOrderByCreatedAt(String userId);
}
