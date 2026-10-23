package com.pricing.adapter.persistence.adapter;

import com.grab.framework.id.impl.CommonId;
import com.grab.framework.id.impl.UuidGenerator;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pricing.adapter.persistence.entity.PriceEntity;
import com.pricing.adapter.persistence.entity.PriceListEntity;
import com.pricing.adapter.persistence.entity.PriceListRuleEntity;
import com.pricing.adapter.persistence.entity.PricePreferenceEntity;
import com.pricing.adapter.persistence.entity.PriceRuleEntity;
import com.pricing.adapter.persistence.entity.PriceSetEntity;
import com.pricing.adapter.persistence.entity.VariantPriceSetLinkEntity;
import com.pricing.adapter.persistence.repository.config.PricingRepositoryTestConfig;
import com.pricing.adapter.persistence.repository.jpa.PriceJpaRepository;
import com.pricing.adapter.persistence.repository.jpa.PriceListJpaRepository;
import com.pricing.adapter.persistence.repository.jpa.PricePreferenceJpaRepository;
import com.pricing.adapter.persistence.repository.jpa.PriceSetJpaRepository;
import com.pricing.adapter.persistence.repository.jpa.VariantPriceSetLinkJpaRepository;
import com.pricing.application.model.read.VariantPriceSetLinkView;
import com.pricing.domain.policy.PriceCandidate;
import com.pricing.domain.enums.PriceListStatus;
import com.pricing.domain.enums.PriceListType;
import com.pricing.domain.enums.PriceRuleOperator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class PricingBatchPersistenceTest extends PricingRepositoryTestConfig {
    private static final Instant NOW = Instant.parse("2026-10-23T00:00:00Z");

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private PriceJpaRepository prices;

    @Autowired
    private PricePreferenceJpaRepository preferences;

    @Autowired
    private PriceListJpaRepository priceLists;

    @Autowired
    private PriceSetJpaRepository priceSets;

    @Autowired
    private VariantPriceSetLinkJpaRepository links;

    @BeforeEach
    void setUp() {
        PriceListEntity campaign = campaign();
        priceLists.save(campaign);
        for (int index = 1; index <= 20; index++) {
            String priceSetId = "price-set-" + index;
            PriceSetEntity priceSet = priceSet(priceSetId);
            priceSets.save(priceSet);
            PriceEntity price = price(priceSet, "price-" + index, campaign);
            prices.save(price);
            for (int variantIndex = 1; variantIndex <= 5; variantIndex++) {
                links.save(link(
                        "variant-" + index + "-" + variantIndex,
                        priceSetId
                ));
            }
        }
        PricePreferenceEntity preference = new PricePreferenceEntity();
        preference.setUuid("preference-1");
        preference.setAttribute("region_id");
        preference.setValue("region-sg");
        preference.setTaxInclusive(false);
        preference.setCreatedAt(NOW);
        preference.setUpdatedAt(NOW);
        preferences.save(preference);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void singleVariantBatchUsesOneLinkCandidateAndPreferenceSelect() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        sessionFactory.getStatistics().setStatisticsEnabled(true);
        sessionFactory.getStatistics().clear();
        VariantPriceSetLinkQueryAdapter linkAdapter = new VariantPriceSetLinkQueryAdapter(links, new PricingPersistenceExecutor());
        PriceQueryAdapter priceAdapter = priceAdapter();

        List<VariantPriceSetLinkView> linkViews = linkAdapter.findByVariantIds(List.of("variant-1-1"));
        List<Id> priceSetIds = linkViews.stream()
                .map(link -> (Id) new CommonId(link.priceSetId()))
                .distinct()
                .toList();
        List<PriceCandidate> candidates = priceAdapter.findCandidates(priceSetIds, "usd");
        priceAdapter.findPreferences();

        assertThat(linkViews).hasSize(1);
        assertThat(candidates).hasSize(1);
        assertThat(sessionFactory.getStatistics().getQueryExecutionCount()).isEqualTo(3);
    }

    @Test
    void fullPageBatchUsesOneLinkCandidateAndPreferenceSelect() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        sessionFactory.getStatistics().setStatisticsEnabled(true);
        sessionFactory.getStatistics().clear();
        VariantPriceSetLinkQueryAdapter linkAdapter = new VariantPriceSetLinkQueryAdapter(links, new PricingPersistenceExecutor());
        PriceQueryAdapter priceAdapter = priceAdapter();

        List<String> variantIds = IntStream.rangeClosed(1, 20)
                .boxed()
                .flatMap(product -> IntStream.rangeClosed(1, 5)
                        .mapToObj(variant -> "variant-" + product + "-" + variant))
                .toList();
        List<VariantPriceSetLinkView> linkViews = linkAdapter.findByVariantIds(variantIds);
        List<Id> priceSetIds = linkViews.stream()
                .map(link -> (Id) new CommonId(link.priceSetId()))
                .distinct()
                .toList();
        List<PriceCandidate> candidates = priceAdapter.findCandidates(
                priceSetIds,
                "usd"
        );
        priceAdapter.findPreferences();

        assertThat(linkViews).hasSize(100);
        assertThat(candidates).hasSize(20);
        assertThat(sessionFactory.getStatistics().getQueryExecutionCount()).isEqualTo(3);
    }

    private PriceQueryAdapter priceAdapter() {
        return new PriceQueryAdapter(
                prices,
                priceSets,
                null,
                preferences,
                new IdMapper(new UuidGenerator()),
                new ObjectMapper(),
                new PricingPersistenceExecutor()
        );
    }

    private PriceSetEntity priceSet(String uuid) {
        PriceSetEntity result = new PriceSetEntity();
        result.setUuid(uuid);
        result.setCreatedAt(NOW);
        result.setUpdatedAt(NOW);
        return result;
    }

    private PriceEntity price(PriceSetEntity priceSet, String uuid, PriceListEntity campaign) {
        PriceEntity result = new PriceEntity();
        result.setUuid(uuid);
        result.setCurrencyCode("usd");
        result.setAmount(new BigDecimal("10.00"));
        result.setRulesCount(1);
        result.setPriceSet(priceSet);
        result.setPriceList(campaign);
        result.setCreatedAt(NOW);
        result.setUpdatedAt(NOW);
        PriceRuleEntity rule = new PriceRuleEntity();
        rule.setUuid(uuid + "-rule");
        rule.setPrice(result);
        rule.setAttribute("sales_channel_id");
        rule.setValue("channel-1");
        rule.setOperator(PriceRuleOperator.EQ);
        rule.setPriority(1);
        result.setRules(new LinkedHashSet<>(Set.of(rule)));
        return result;
    }

    private PriceListEntity campaign() {
        PriceListEntity result = new PriceListEntity();
        result.setUuid("campaign-shared");
        result.setTitle("Shared campaign");
        result.setStatus(PriceListStatus.ACTIVE);
        result.setType(PriceListType.SALE);
        result.setRulesCount(1);
        result.setCreatedAt(NOW);
        result.setUpdatedAt(NOW);
        PriceListRuleEntity rule = new PriceListRuleEntity();
        rule.setUuid("campaign-rule");
        rule.setPriceList(result);
        rule.setAttribute("region_id");
        rule.setValuesJson("[\"region-sg\"]");
        result.setRules(new LinkedHashSet<>(Set.of(rule)));
        return result;
    }

    private VariantPriceSetLinkEntity link(String variantId, String priceSetId) {
        VariantPriceSetLinkEntity result = new VariantPriceSetLinkEntity();
        result.setVariantId(variantId);
        result.setPriceSetId(priceSetId);
        result.setProductId("product-1");
        result.setSku(variantId);
        result.setMerchantId("merchant-1");
        result.setCreatedAt(NOW);
        result.setUpdatedAt(NOW);
        return result;
    }
}
