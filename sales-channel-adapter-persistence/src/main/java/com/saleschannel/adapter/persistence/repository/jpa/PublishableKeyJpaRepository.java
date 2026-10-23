package com.saleschannel.adapter.persistence.repository.jpa;

import com.saleschannel.adapter.persistence.entity.PublishableKeyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PublishableKeyJpaRepository extends JpaRepository<PublishableKeyJpaEntity, Long> {
    Optional<PublishableKeyJpaEntity> findByUuid(String uuid);

    boolean existsByTokenDigest(String tokenDigest);
}
