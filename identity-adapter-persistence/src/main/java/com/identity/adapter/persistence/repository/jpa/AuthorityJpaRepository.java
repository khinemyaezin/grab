package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.entity.AuthorityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AuthorityJpaRepository extends JpaRepository<AuthorityEntity, Long> {
    Optional<AuthorityEntity> findByCode(String code);

    List<AuthorityEntity> findByCodeInAndActiveTrue(Collection<String> codes);

    @Modifying
    @Query(value = """
            INSERT INTO authorities (uuid, code, category, name, description, active)
            VALUES (:uuid, :code, :category, :name, :description, TRUE)
            ON CONFLICT (code) DO UPDATE
            SET category = EXCLUDED.category,
                name = EXCLUDED.name,
                description = EXCLUDED.description
            """, nativeQuery = true)
    void upsertByCode(
            @Param("uuid") String uuid,
            @Param("code") String code,
            @Param("category") String category,
            @Param("name") String name,
            @Param("description") String description
    );
}
