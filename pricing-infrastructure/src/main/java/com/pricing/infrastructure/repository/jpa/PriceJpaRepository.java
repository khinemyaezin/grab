package com.pricing.infrastructure.repository.jpa;

import com.pricing.infrastructure.entity.PriceEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PriceJpaRepository extends JpaRepository<PriceEntity, Long> {
    @EntityGraph(attributePaths = {"rules", "priceSet", "priceList", "priceList.rules"})
    @Query("""
            select p from PriceEntity p
            where p.priceSet.uuid in :priceSetIds
              and lower(p.currencyCode) = lower(:currencyCode)
            """)
    List<PriceEntity> findCandidates(
            @Param("priceSetIds") Collection<String> priceSetIds,
            @Param("currencyCode") String currencyCode
    );
}
