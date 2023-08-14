package com.coolstuff.ecommerce.grab.persistence.repository;

import com.coolstuff.ecommerce.grab.persistence.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity,Long> {

    List<RoleEntity> findByNameIn(String[] names);
    RoleEntity findByName(String name);
}
