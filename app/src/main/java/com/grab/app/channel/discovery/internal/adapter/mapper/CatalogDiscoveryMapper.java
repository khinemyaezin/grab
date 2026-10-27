package com.grab.app.channel.discovery.internal.adapter.mapper;

import com.grab.discovery.application.port.outbound.CatalogPort;
import com.grab.framework.mapper.IdMapper;
import com.grab.app.catalog.catalog.port.CatalogDiscoveryQuery;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.discovery.rest.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class, unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class CatalogDiscoveryMapper {
    public abstract CatalogPort.ProductFacts toProductFacts(CatalogDiscoveryQuery.ProductFacts facts);

    public abstract CatalogPort.ProductOption toProductOption(CatalogDiscoveryQuery.ProductOption option);

    public abstract List<CatalogPort.ProductOption> toProductOptions(List<CatalogDiscoveryQuery.ProductOption> options);

    public abstract CatalogPort.VariantFacts toVariantFacts(CatalogDiscoveryQuery.VariantFacts facts);

    public abstract List<CatalogPort.VariantFacts> toVariantFacts(List<CatalogDiscoveryQuery.VariantFacts> facts);

    public abstract CatalogPort.ProductChannelPair toProductChannelPair(CatalogDiscoveryQuery.ProductChannelPair pair);

    public abstract List<CatalogPort.ProductChannelPair> toProductChannelPairs(
            List<CatalogDiscoveryQuery.ProductChannelPair> pairs
    );
}
