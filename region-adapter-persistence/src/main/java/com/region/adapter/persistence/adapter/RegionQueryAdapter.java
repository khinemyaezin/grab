package com.region.adapter.persistence.adapter;

import com.grab.framework.support.PersistenceExecutor;
import com.region.adapter.persistence.entity.RegionEntity;
import com.region.adapter.persistence.repository.RegionJpaRepository;
import com.region.application.model.read.RegionView;
import com.region.application.port.outbound.RegionQueryPort;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RegionQueryAdapter implements RegionQueryPort {
    private final RegionJpaRepository regions;
    private final PersistenceExecutor executor;

    @Override
    public Optional<RegionView> findById(String regionId) {
        return executor.query("Region", () -> regions.findById(regionId).map(this::toView));
    }

    @Override
    public boolean countryAllowed(String regionId, String countryCode) {
        return executor.query("Region", () -> regions.existsCountry(regionId, countryCode));
    }

    @Override
    public Optional<RegionView> findByCountry(String countryCode) {
        return executor.query("Region", () -> regions.findByCountryCode(countryCode).map(this::toView));
    }

    @Override
    public List<String> findActiveCountryCodes() {
        return executor.query("Region", regions::findActiveCountryCodes);
    }

    private RegionView toView(RegionEntity entity) {
        return new RegionView(
                entity.getId(),
                entity.getName(),
                entity.getCurrencyCode(),
                entity.getStatus(),
                entity.getCountries().stream()
                        .map(c -> c.getCountryCode().toUpperCase())
                        .collect(Collectors.toUnmodifiableSet())
        );
    }
}
