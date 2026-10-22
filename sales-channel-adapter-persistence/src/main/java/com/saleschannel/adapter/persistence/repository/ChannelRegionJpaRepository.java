package com.saleschannel.adapter.persistence.repository;

import com.saleschannel.adapter.persistence.entity.ChannelRegionEntity;
import com.saleschannel.adapter.persistence.entity.ChannelRegionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChannelRegionJpaRepository extends JpaRepository<ChannelRegionEntity, ChannelRegionId> {
    @Query("""
            select binding.id.regionId from ChannelRegionEntity binding
            where binding.id.salesChannelId = :salesChannelId and binding.enabled = true
            """)
    List<String> findEnabledRegionIds(@Param("salesChannelId") String salesChannelId);

    @Query("""
            select binding.id.regionId from ChannelRegionEntity binding
            where binding.id.salesChannelId = :salesChannelId and binding.enabled = true
            and binding.defaultRegion = true
            """)
    Optional<String> findDefaultRegionId(@Param("salesChannelId") String salesChannelId);

    boolean existsByIdSalesChannelIdAndIdRegionIdAndEnabledTrue(String salesChannelId, String regionId);
}
