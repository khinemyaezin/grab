package com.identity.infrastructure.repository.jpa;

import com.identity.infrastructure.entity.UserEntity;
import com.identity.infrastructure.view.UserAssignmentView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserQueryRepository extends JpaRepository<UserEntity, Long> {

    @Query("""
            SELECT new com.identity.infrastructure.view.UserAssignmentView(
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
