package com.pricing.infrastructure.repository.jpa.impl;

import com.grab.framework.support.PersistenceExecutor;
import com.pricing.infrastructure.entity.VariantPriceSetLinkEntity;
import com.pricing.infrastructure.mapper.jpa.VariantPriceSetLinkJpaAssembler;
import com.pricing.infrastructure.repository.jpa.VariantPriceSetLinkJpaRepository;
import com.pricing.infrastructure.repository.jpa.VariantPriceSetLinkRepository;
import com.pricing.infrastructure.view.VariantPriceSetLinkView;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultVariantPriceSetLinkRepository implements VariantPriceSetLinkRepository {

    private final VariantPriceSetLinkJpaRepository jpaRepository;
    private final VariantPriceSetLinkJpaAssembler assembler;
    private final PersistenceExecutor executor;

    @Override
    public Optional<VariantPriceSetLinkView> findByVariantId(String variantId) {
        return executor.query("VariantPriceSetLink", () ->
                jpaRepository.findById(variantId).map(assembler::toView));
    }

    @Override
    public void save(VariantPriceSetLinkView link) {
        executor.command("VariantPriceSetLink", () -> {
            Optional<VariantPriceSetLinkEntity> linkEntity = jpaRepository.findById(link.variantId());
            VariantPriceSetLinkEntity entity;

            if (linkEntity.isPresent()) {
                entity = assembler.buildEntity(link, linkEntity.get());
            } else {
                entity = assembler.buildEntity(link, null);
            }

            jpaRepository.save(entity);
        });
    }



    @Override
    public void deleteByVariantId(String variantId) {
        executor.command("VariantPriceSetLink", () -> jpaRepository.deleteById(variantId));
    }

    @Override
    public void deleteByPriceSetId(String priceSetId) {
        executor.command("VariantPriceSetLink", () -> jpaRepository.deleteByPriceSetId(priceSetId));
    }
}
