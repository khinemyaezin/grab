package com.region.adapter.persistence.repository;

import com.region.adapter.persistence.entity.RegionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegionJpaRepository extends JpaRepository<RegionEntity, String> {

    @Query("""
            select case when count(c) > 0 then true else false end
            from RegionCountryEntity c
            where c.region.id = :regionId and upper(c.countryCode) = upper(:countryCode)
            """)
    boolean existsCountry(@Param("regionId") String regionId, @Param("countryCode") String countryCode);
}
