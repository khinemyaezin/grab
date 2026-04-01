package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.entity.entity.VariantOptionEntity;
import com.catalog.infrastructure.view.VariantOptionView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VariantOptionQueryRepository extends JpaRepository<VariantOptionEntity, Long> {
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
}
