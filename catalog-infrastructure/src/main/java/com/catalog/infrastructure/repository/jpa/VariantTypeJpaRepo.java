package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.entity.entity.VariantTypeEntity;
import com.catalog.infrastructure.repository.EntityRepository;
import com.catalog.infrastructure.view.VariantTypeView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VariantTypeJpaRepo extends EntityRepository<VariantTypeEntity, Long>, JpaRepository<VariantTypeEntity, Long> {
    Optional<VariantTypeEntity> findByUuid(String uuid);

    @Query("""
            select distinct vt
            from VariantTypeEntity vt
            left join fetch vt.variantOptions vo
            where vt.uuid = :uuid
            """)
    Optional<VariantTypeEntity> findByUuidWithOptions(@Param("uuid") String uuid);

    @Query("""
        select new com.catalog.infrastructure.view.VariantTypeView(
            vo.uuid,
            vo.name
        )
        from VariantTypeEntity vo
        where lower(vo.name) like lower(concat('%', :name, '%'))
        order by vo.name asc
      """)
    List<VariantTypeView> searchByName(@Param("name") String name);
}
