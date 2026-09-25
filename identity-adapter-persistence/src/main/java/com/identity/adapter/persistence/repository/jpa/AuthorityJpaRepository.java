package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.entity.AuthorityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Collection;
import java.util.List;

public interface AuthorityJpaRepository extends JpaRepository<AuthorityEntity, Long> {
    Optional<AuthorityEntity> findByCode(String code);

    List<AuthorityEntity> findByCodeInAndActiveTrue(Collection<String> codes);

    @Modifying
    @Query(value = """
            INSERT INTO authorities (code, name, description, active)
            VALUES (:code, :name, :description, TRUE)
            ON CONFLICT (code) DO UPDATE
            SET name = EXCLUDED.name,
                description = EXCLUDED.description
            """, nativeQuery = true)
    void upsertByCode(
            @Param("code") String code,
            @Param("name") String name,
            @Param("description") String description
    );
}
