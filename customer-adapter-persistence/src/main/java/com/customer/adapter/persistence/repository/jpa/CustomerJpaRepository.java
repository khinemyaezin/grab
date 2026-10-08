package com.customer.adapter.persistence.repository.jpa;

import com.customer.adapter.persistence.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, Long> {
    Optional<CustomerEntity> findByUuid(String uuid);
    Optional<CustomerEntity> findByUserId(String userId);
    Optional<CustomerEntity> findByEmail(String email);
}
