package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.entity.entity.VariantOptionEntity;
import com.catalog.infrastructure.view.VariantOptionView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VariantOptionJpaRepo extends JpaRepository<VariantOptionEntity, Long> {
    Optional<VariantOptionEntity> findByUuid(String uuid);

    @Query("""
        select new com.catalog.infrastructure.view.VariantOptionView(
            vo.uuid,
            vo.name,
            vt.uuid,
            vt.name
        )
        from VariantOptionEntity vo
        inner join vo.variantType vt
        where vo.uuid in :uuids
      """)
    List<VariantOptionView> findAllByUuidIn(@Param("uuids") List<String> uuids);

    @Query("""
        select new com.catalog.infrastructure.view.VariantOptionView(
            vo.uuid,
            vo.name,
            vt.uuid,
            vt.name
        )
        from VariantOptionEntity vo
        inner join vo.variantType vt
        where lower(vo.name) like lower(concat('%', :name, '%'))
        and (:typeId is null or vt.uuid = :typeId)
        order by vo.name asc
      """)
    List<VariantOptionView> findByNameContainingIgnoreCaseAndTypeId(@Param("name") String name, @Param("typeId") String typeId);
}
