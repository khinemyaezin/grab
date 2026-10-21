package com.grab.discovery.adapter.persistence.mapper;

import com.grab.discovery.adapter.persistence.entity.DiscoveryProductEntity;
import com.grab.discovery.adapter.persistence.entity.DiscoveryProductOptionDocument;
import com.grab.discovery.adapter.persistence.entity.DiscoveryVariantOfferDocument;
import com.grab.discovery.application.model.projection.DiscoveryProductProjection;
import com.grab.discovery.application.model.read.DiscoveryProductView;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class DiscoveryProductEntityMapper {

    public abstract DiscoveryProductView toView(DiscoveryProductEntity entity);

    public abstract DiscoveryProductView.ProductOption toViewOption(DiscoveryProductOptionDocument option);

    public abstract List<DiscoveryProductView.ProductOption> toViewOptions(List<DiscoveryProductOptionDocument> options);

    public abstract DiscoveryProductView.VariantOffer toViewOffer(DiscoveryVariantOfferDocument offer);

    public abstract List<DiscoveryProductView.VariantOffer> toViewOffers(List<DiscoveryVariantOfferDocument> offers);

    public abstract DiscoveryProductProjection toProjection(DiscoveryProductEntity entity);

    public abstract DiscoveryProductProjection.ProductOption toProjectionOption(DiscoveryProductOptionDocument option);

    public abstract List<DiscoveryProductProjection.ProductOption> toProjectionOptions(List<DiscoveryProductOptionDocument> options);

    public abstract DiscoveryProductProjection.VariantOffer toProjectionOffer(DiscoveryVariantOfferDocument offer);

    public abstract List<DiscoveryProductProjection.VariantOffer> toProjectionOffers(List<DiscoveryVariantOfferDocument> offers);

    public abstract DiscoveryProductOptionDocument toOptionDocument(DiscoveryProductProjection.ProductOption option);

    public abstract List<DiscoveryProductOptionDocument> toOptionDocuments(List<DiscoveryProductProjection.ProductOption> options);

    public abstract DiscoveryVariantOfferDocument toVariantDocument(DiscoveryProductProjection.VariantOffer offer);

    public abstract List<DiscoveryVariantOfferDocument> toVariantDocuments(List<DiscoveryProductProjection.VariantOffer> offers);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    public abstract void updateEntity(DiscoveryProductProjection projection, @MappingTarget DiscoveryProductEntity entity);
}
