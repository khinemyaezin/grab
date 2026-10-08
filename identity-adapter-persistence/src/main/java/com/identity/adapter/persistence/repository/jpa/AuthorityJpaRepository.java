package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.entity.AuthorityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AuthorityJpaRepository extends JpaRepository<AuthorityEntity, Long> {
    Optional<AuthorityEntity> findByCode(String code);

    List<AuthorityEntity> findByCodeInAndActiveTrue(Collection<String> codes);

    List<AuthorityEntity> findByCategory(String category);

    List<AuthorityEntity> findByCategoryNot(String category);

    default void upsertByCode(String uuid, String code, String category, String name, String description) {
        AuthorityEntity entity = findByCode(code).orElseGet(AuthorityEntity::new);
        if (entity.getUuid() == null) {
            entity.setUuid(uuid);
            entity.setCode(code);
            entity.setActive(true);
        }
        entity.setCategory(category);
        entity.setName(name);
        entity.setDescription(description);
        saveAndFlush(entity);
    }

}
