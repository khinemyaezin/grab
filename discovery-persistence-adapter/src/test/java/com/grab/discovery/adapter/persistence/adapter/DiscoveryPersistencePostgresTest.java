package com.grab.discovery.adapter.persistence.adapter;

import com.grab.discovery.adapter.persistence.entity.DiscoveryProductEntity;
import com.grab.discovery.adapter.persistence.entity.DiscoveryProductVariantEntity;
import com.grab.discovery.adapter.persistence.entity.DiscoveryRebuildRunEntity;
import com.grab.discovery.adapter.persistence.mapper.DiscoveryProductEntityMapper;
import com.grab.discovery.adapter.persistence.mapper.DiscoveryProductEntityMapperImpl;
import com.grab.discovery.adapter.persistence.mapper.DiscoveryRebuildRunMapper;
import com.grab.discovery.adapter.persistence.mapper.DiscoveryRebuildRunMapperImpl;
import com.grab.discovery.adapter.persistence.repository.jpa.DiscoveryProductJpaRepository;
import com.grab.discovery.adapter.persistence.repository.jpa.DiscoveryProductVariantJpaRepository;
import com.grab.discovery.adapter.persistence.repository.jpa.DiscoveryRebuildRunJpaRepository;
import com.grab.discovery.adapter.persistence.specification.jpa.DiscoveryProductSearchSpecification;
import com.grab.discovery.adapter.persistence.exception.DiscoveryInfraException;
import com.grab.discovery.application.model.projection.DiscoveryProductProjection;
import com.grab.discovery.application.model.read.DiscoveryProductView;
import com.grab.discovery.application.model.read.DiscoveryProductKey;
import com.grab.discovery.application.model.write.RebuildProgressResult;
import com.grab.discovery.application.model.write.DiscoveryRebuildRun;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.id.impl.UuidGenerator;
import com.grab.framework.mapper.IdMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = {
        "spring.flyway.locations=classpath:db/migration/storefrontquery",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ContextConfiguration(classes = DiscoveryPersistencePostgresTest.TestApplication.class)
@Import({DiscoveryProductEntityMapperImpl.class, DiscoveryRebuildRunMapperImpl.class})
class DiscoveryPersistencePostgresTest {

    @Container
    private static final PostgreSQLContainer<?> DATABASE = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", DATABASE::getJdbcUrl);
        registry.add("spring.datasource.username", DATABASE::getUsername);
        registry.add("spring.datasource.password", DATABASE::getPassword);
        registry.add("spring.datasource.driver-class-name", DATABASE::getDriverClassName);
    }

    @Autowired
    private DiscoveryProductJpaRepository products;
    @Autowired
    private DiscoveryProductVariantJpaRepository variants;
    @Autowired
    private DiscoveryRebuildRunJpaRepository runs;
    @Autowired
    private DiscoveryProductEntityMapper mapper;
    @Autowired
    private DiscoveryRebuildRunMapper rebuildMapper;
    @Autowired
    private IdGenerator idGenerator;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private PlatformTransactionManager transactionManager;

    private DiscoveryProductRepositoryAdapter projectionRepository;

    @BeforeEach
    void setUp() {
        projectionRepository = new DiscoveryProductRepositoryAdapter(
                products,
                variants,
                mapper,
                new DiscoveryPersistenceExecutor()
        );
    }

    @Test
    void save_changedVariants_replacesFactsAndExactMembershipAtomically() {
        DiscoveryProductProjection initial = projection("variant-100", "SKU-100", DiscoveryProductProjection.NEW_PROJECTION_VERSION);
        projectionRepository.save(initial);
        flushAndClear();

        assertThat(variants.findByVariantId("variant-100")).hasSize(1);
        assertThat(variants.findByVariantId("variant-10")).isEmpty();
        assertThat(variants.findBySku("SKU-10")).isEmpty();

        DiscoveryProductProjection stored = projectionRepository.findBySalesChannelAndProduct(
                initial.salesChannelId(),
                initial.productId()
        ).orElseThrow();
        assertThat(stored.options()).isEqualTo(initial.options());
        assertThat(stored.variants()).isEqualTo(initial.variants());
        DiscoveryProductProjection updated = projection(
                "variant-200",
                "SKU-200",
                stored.version()
        );
        projectionRepository.save(updated);
        flushAndClear();

        assertThat(variants.findByVariantId("variant-100")).isEmpty();
        assertThat(variants.findBySku("SKU-100")).isEmpty();
        assertThat(variants.findByVariantId("variant-200")).hasSize(1);
        assertThat(products.findBySalesChannelIdAndProductId("channel-1", "product-1"))
                .get()
                .extracting(DiscoveryProductEntity::getTitle)
                .isEqualTo("Factual product");
    }

    @Test
    void searchVisible_activePublishedSoldOutVariant_keepsProductVisible() {
        DiscoveryProductProjection projection = projection("variant-100", "SKU-100", DiscoveryProductProjection.NEW_PROJECTION_VERSION);
        DiscoveryProductProjection.VariantFact soldOutVariant = new DiscoveryProductProjection.VariantFact(
                projection.variants().getFirst().variantId(),
                "SKU-100",
                "Variant",
                "ACTIVE",
                Map.of(),
                0,
                true,
                false,
                false
        );
        projection = withVariants(projection, List.of(soldOutVariant));
        projectionRepository.save(projection);
        flushAndClear();

        DiscoveryProductSearchSpecification specification = new DiscoveryProductSearchSpecification();
        Specification<DiscoveryProductEntity> visible = specification.visibleForSalesChannel("channel-1", "factual", null);
        List<DiscoveryProductView> results = products.findAll(visible, PageRequest.of(0, 10)).stream()
                .map(mapper::toView)
                .toList();

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().variants().getFirst().availableQty()).isZero();
    }

    @Test
    void searchVisible_keywordCategoryAndPagination_returnsLiteralFilteredOrderedFacts() {
        DiscoveryProductProjection first = withProductFacts(
                projection("variant-a", "SKU-A", DiscoveryProductProjection.NEW_PROJECTION_VERSION),
                "product-a",
                "category-1",
                "50%_off item",
                "literal-keyword"
        );
        DiscoveryProductProjection second = withProductFacts(
                projection("variant-b", "SKU-B", DiscoveryProductProjection.NEW_PROJECTION_VERSION),
                "product-b",
                "category-1",
                "50XXoff item",
                "wildcard-decoy"
        );
        DiscoveryProductProjection third = withProductFacts(
                projection("variant-c", "SKU-C", DiscoveryProductProjection.NEW_PROJECTION_VERSION),
                "product-c",
                "category-2",
                "Other item",
                "other-category"
        );
        projectionRepository.save(second);
        projectionRepository.save(third);
        projectionRepository.save(first);
        flushAndClear();

        DiscoveryProductQueryAdapter queryAdapter = new DiscoveryProductQueryAdapter(
                products,
                variants,
                new DiscoveryProductSearchSpecification(),
                mapper,
                new DiscoveryPersistenceExecutor()
        );
        List<DiscoveryProductView> literalResults = queryAdapter.searchVisible(
                first.salesChannelId(),
                "50%_off",
                new CommonId("category-1"),
                PageRequest.of(0, 10)
        ).getContent();
        Page<DiscoveryProductView> firstPage = queryAdapter.searchVisible(
                first.salesChannelId(),
                null,
                null,
                PageRequest.of(0, 2)
        );
        List<DiscoveryProductView> secondPage = queryAdapter.searchVisible(
                first.salesChannelId(),
                null,
                null,
                PageRequest.of(1, 2)
        ).getContent();
        List<DiscoveryProductView> categoryResults = queryAdapter.searchVisible(
                first.salesChannelId(),
                null,
                new CommonId("category-2"),
                PageRequest.of(0, 10)
        ).getContent();

        assertThat(literalResults).extracting(DiscoveryProductView::slug).containsExactly("literal-keyword");
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getContent()).extracting(DiscoveryProductView::slug)
                .containsExactly("literal-keyword", "wildcard-decoy");
        assertThat(secondPage).extracting(DiscoveryProductView::slug).containsExactly("other-category");
        assertThat(categoryResults).extracting(DiscoveryProductView::slug).containsExactly("other-category");
    }

    @Test
    void findVisibleBySlug_noActivePublishedVariant_returnsEmpty() {
        DiscoveryProductProjection projection = withProductFacts(
                projection("variant-inactive", "SKU-INACTIVE", DiscoveryProductProjection.NEW_PROJECTION_VERSION),
                "product-inactive",
                "category-1",
                "Inactive product",
                "inactive-product"
        );
        DiscoveryProductProjection.VariantFact inactiveVariant = new DiscoveryProductProjection.VariantFact(
                projection.variants().getFirst().variantId(),
                "SKU-INACTIVE",
                "Inactive variant",
                "INACTIVE",
                Map.of(),
                0,
                true,
                false,
                false
        );
        projectionRepository.save(withVariants(projection, List.of(inactiveVariant)));
        flushAndClear();

        DiscoveryProductQueryAdapter queryAdapter = new DiscoveryProductQueryAdapter(
                products,
                variants,
                new DiscoveryProductSearchSpecification(),
                mapper,
                new DiscoveryPersistenceExecutor()
        );

        assertThat(queryAdapter.findVisibleBySlug(projection.salesChannelId(), "inactive-product")).isEmpty();
    }

    @Test
    void save_secondUnfinishedRun_rejectsAndRetainsExistingCheckpoint() {
        DiscoveryRebuildRunEntity first = rebuildRun("run-1", RebuildProgressResult.Stage.EXISTING_PROJECTIONS);
        first.setLastProductId("product-9");
        first.setLastSalesChannelId("channel-2");
        first.setProcessedCount(101);
        runs.saveAndFlush(first);
        entityManager.clear();

        DiscoveryRebuildRunEntity reloaded = runs.findById("run-1").orElseThrow();
        assertThat(reloaded.getStage()).isEqualTo(RebuildProgressResult.Stage.EXISTING_PROJECTIONS);
        assertThat(reloaded.getLastProductId()).isEqualTo("product-9");
        assertThat(reloaded.getLastSalesChannelId()).isEqualTo("channel-2");
        assertThat(reloaded.getProcessedCount()).isEqualTo(101);

        DiscoveryRebuildRunEntity second = rebuildRun("run-2", RebuildProgressResult.Stage.PUBLICATIONS);
        assertThatThrownBy(() -> runs.saveAndFlush(second)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void lockUnfinished_restartedAdapter_recoversCheckpointAndRetainsCompletedRun() {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        DiscoveryPersistenceExecutor executor = new DiscoveryPersistenceExecutor();
        DiscoveryRebuildRunRepositoryAdapter adapter = new DiscoveryRebuildRunRepositoryAdapter(runs, rebuildMapper, executor);
        CommonId runId = new CommonId("run-recovery");
        CommonId productId = new CommonId("product-9");
        CommonId channelId = new CommonId("channel-2");
        Instant startedAt = Instant.parse("2026-01-01T00:00:00Z");
        DiscoveryRebuildRun initial = transaction.execute(status -> adapter.create(runId, startedAt));
        try {
            DiscoveryRebuildRun progress = new DiscoveryRebuildRun(
                    runId, startedAt, RebuildProgressResult.Stage.EXISTING_PROJECTIONS,
                    productId, channelId, 101, 7, initial.version()
            );
            transaction.executeWithoutResult(status -> adapter.save(progress));
            DiscoveryRebuildRunRepositoryAdapter restarted = new DiscoveryRebuildRunRepositoryAdapter(runs, rebuildMapper, executor);
            DiscoveryRebuildRun recovered = transaction.execute(status -> restarted.lockUnfinished().orElseThrow());

            assertThat(recovered.lastProductId()).isEqualTo(productId);
            assertThat(recovered.lastSalesChannelId()).isEqualTo(channelId);
            assertThat(recovered.processedCount()).isEqualTo(101);
            assertThat(recovered.invalidatedCount()).isEqualTo(7);
            assertThat(recovered.version()).isGreaterThan(initial.version());
            DiscoveryRebuildRun completed = new DiscoveryRebuildRun(
                    runId, startedAt, RebuildProgressResult.Stage.COMPLETED,
                    productId, channelId, 101, 7, recovered.version()
            );
            transaction.executeWithoutResult(status -> restarted.save(completed));

            transaction.executeWithoutResult(status -> {
                entityManager.clear();
                assertThat(restarted.lockUnfinished()).isEmpty();
                assertThat(runs.findById(runId.getValue())).get()
                        .extracting(DiscoveryRebuildRunEntity::getStage)
                        .isEqualTo(RebuildProgressResult.Stage.COMPLETED);
            });
        } finally {
            transaction.executeWithoutResult(status -> runs.deleteById(runId.getValue()));
        }
    }

    @Test
    void findKeysAfter_multipleChannelProductPairs_returnsOrderedApplicationKeys() {
        DiscoveryProductProjection first = projection("variant-first", "SKU-FIRST", DiscoveryProductProjection.NEW_PROJECTION_VERSION);
        DiscoveryProductProjection second = new DiscoveryProductProjection(
                new CommonId("channel-2"), first.productId(), first.merchantId(), first.categoryId(),
                first.title(), null, first.description(), first.thumbnail(), first.productStatus(),
                false, false, false, first.options(), List.of(), first.updatedAt(), DiscoveryProductProjection.NEW_PROJECTION_VERSION
        );
        DiscoveryProductProjection third = withProductFacts(
                projection("variant-third", "SKU-THIRD", DiscoveryProductProjection.NEW_PROJECTION_VERSION),
                "product-2", "category-1", "Other product", "other-product"
        );
        projectionRepository.save(third);
        projectionRepository.save(second);
        projectionRepository.save(first);
        flushAndClear();

        List<DiscoveryProductKey> firstPage = projectionRepository.findKeysAfter(null, null, 2);
        DiscoveryProductKey cursor = firstPage.getLast();
        List<DiscoveryProductKey> secondPage = projectionRepository.findKeysAfter(
                cursor.productId(), cursor.salesChannelId(), 2
        );

        DiscoveryProductKey firstKey = new DiscoveryProductKey(first.productId(), first.salesChannelId());
        DiscoveryProductKey secondKey = new DiscoveryProductKey(second.productId(), second.salesChannelId());
        DiscoveryProductKey thirdKey = new DiscoveryProductKey(third.productId(), third.salesChannelId());
        assertThat(firstPage).containsExactly(firstKey, secondKey);
        assertThat(secondPage).containsExactly(thirdKey);
    }

    @Test
    void migrate_emptyDatabase_createsFactsAndCheckpointSchema() {
        List<String> columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'discovery_product'",
                String.class
        );

        assertThat(columns).contains("merchant_id", "category_id", "options", "variants", "updated_at");
        assertThat(columns).doesNotContain("currency", "min_price", "max_price");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'discovery_rebuild_run'",
                Integer.class
        )).isEqualTo(1);
    }

    @Test
    void save_staleVersion_rejectsUpdate() {
        DiscoveryProductProjection initial = projection("variant-100", "SKU-100", DiscoveryProductProjection.NEW_PROJECTION_VERSION);
        projectionRepository.save(initial);
        flushAndClear();
        DiscoveryProductProjection stored = projectionRepository.findBySalesChannelAndProduct(
                initial.salesChannelId(),
                initial.productId()
        ).orElseThrow();
        projectionRepository.save(withTitle(stored, "Updated title"));
        flushAndClear();

        assertThatThrownBy(() -> projectionRepository.save(stored))
                .isInstanceOf(DiscoveryInfraException.class)
                .hasCauseInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    @Test
    void save_delayedCreate_rejectsExistingVersionZeroProjection() {
        DiscoveryProductProjection delayed = projection(
                "variant-late", "SKU-LATE", DiscoveryProductProjection.NEW_PROJECTION_VERSION
        );
        DiscoveryProductProjection winner = withTitle(delayed, "Current facts from the winning insert");
        projectionRepository.save(winner);
        flushAndClear();

        assertThatThrownBy(() -> projectionRepository.save(delayed))
                .isInstanceOf(DiscoveryInfraException.class)
                .hasCauseInstanceOf(ObjectOptimisticLockingFailureException.class);

        DiscoveryProductProjection recovered = projectionRepository.findBySalesChannelAndProduct(
                winner.salesChannelId(), winner.productId()
        ).orElseThrow();
        assertThat(recovered.title()).isEqualTo(winner.title());
        assertThat(recovered.version()).isZero();
        assertThat(variants.findByVariantId("variant-late")).hasSize(1);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_duplicateMembership_rollsBackFactsAndMembership() {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        DiscoveryProductProjection initial = projection("variant-rollback", "SKU-ROLLBACK", DiscoveryProductProjection.NEW_PROJECTION_VERSION);
        transaction.executeWithoutResult(status -> projectionRepository.save(initial));
        try {
            DiscoveryProductProjection stored = transaction.execute(status -> projectionRepository.findBySalesChannelAndProduct(
                    initial.salesChannelId(), initial.productId()
            ).orElseThrow());
            DiscoveryProductProjection.VariantFact fact = stored.variants().getFirst();
            DiscoveryProductProjection changedTitle = withTitle(stored, "Must roll back");
            DiscoveryProductProjection invalid = withVariants(changedTitle, List.of(fact, fact));

            assertThatThrownBy(() -> transaction.executeWithoutResult(status -> projectionRepository.save(invalid)))
                    .isInstanceOf(DiscoveryInfraException.class);

            transaction.executeWithoutResult(status -> {
                entityManager.clear();
                DiscoveryProductProjection recovered = projectionRepository.findBySalesChannelAndProduct(
                        initial.salesChannelId(), initial.productId()
                ).orElseThrow();
                assertThat(recovered.title()).isEqualTo(initial.title());
                assertThat(recovered.version()).isEqualTo(stored.version());
                assertThat(variants.findByVariantId("variant-rollback")).hasSize(1);
            });
        } finally {
            transaction.executeWithoutResult(status -> products.deleteAll());
        }
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private DiscoveryProductProjection projection(String variantId, String sku, long version) {
        DiscoveryProductProjection.VariantFact variant = new DiscoveryProductProjection.VariantFact(
                new CommonId(variantId), sku, "Variant", "ACTIVE", Map.of("Color", "Blue"), 0, true, false, false
        );
        return new DiscoveryProductProjection(
                new CommonId("channel-1"),
                new CommonId("product-1"),
                new CommonId("merchant-1"),
                new CommonId("category-1"),
                "Factual product",
                "factual-product",
                "Description",
                null,
                "ACTIVE",
                true,
                true,
                false,
                List.of(new DiscoveryProductProjection.ProductOption("Color", List.of("Blue"))),
                List.of(variant),
                Instant.parse("2026-01-01T00:00:00Z"),
                version
        );
    }

    private DiscoveryProductProjection withVariants(
            DiscoveryProductProjection projection,
            List<DiscoveryProductProjection.VariantFact> facts
    ) {
        return new DiscoveryProductProjection(
                projection.salesChannelId(), projection.productId(), projection.merchantId(), projection.categoryId(),
                projection.title(), projection.slug(), projection.description(), projection.thumbnail(),
                projection.productStatus(), projection.published(), projection.channelEnabled(), projection.hasStock(),
                projection.options(), facts, projection.updatedAt(), projection.version()
        );
    }

    private DiscoveryProductProjection withProductFacts(
            DiscoveryProductProjection projection,
            String productId,
            String categoryId,
            String title,
            String slug
    ) {
        return new DiscoveryProductProjection(
                projection.salesChannelId(),
                new CommonId(productId),
                projection.merchantId(),
                new CommonId(categoryId),
                title,
                slug,
                projection.description(),
                projection.thumbnail(),
                projection.productStatus(),
                projection.published(),
                projection.channelEnabled(),
                projection.hasStock(),
                projection.options(),
                projection.variants(),
                projection.updatedAt(),
                projection.version()
        );
    }

    private DiscoveryProductProjection withTitle(DiscoveryProductProjection projection, String title) {
        return new DiscoveryProductProjection(
                projection.salesChannelId(), projection.productId(), projection.merchantId(), projection.categoryId(),
                title, projection.slug(), projection.description(), projection.thumbnail(), projection.productStatus(),
                projection.published(), projection.channelEnabled(), projection.hasStock(), projection.options(),
                projection.variants(), projection.updatedAt(), projection.version()
        );
    }

    private DiscoveryRebuildRunEntity rebuildRun(String id, RebuildProgressResult.Stage stage) {
        DiscoveryRebuildRunEntity run = new DiscoveryRebuildRunEntity();
        run.setRunId(id);
        run.setStartedAt(Instant.parse("2026-01-01T00:00:00Z"));
        run.setStage(stage);
        run.setProcessedCount(0);
        run.setInvalidatedCount(0);
        return run;
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = {
            DiscoveryProductEntity.class,
            DiscoveryProductVariantEntity.class,
            DiscoveryRebuildRunEntity.class
    })
    @EnableJpaRepositories(basePackageClasses = {
            DiscoveryProductJpaRepository.class,
            DiscoveryProductVariantJpaRepository.class,
            DiscoveryRebuildRunJpaRepository.class
    })
    @TestConfiguration
    static class TestApplication {
        @Bean
        IdGenerator idGenerator() {
            return new UuidGenerator();
        }

        @Bean
        IdMapper idMapper(IdGenerator idGenerator) {
            return new IdMapper(idGenerator);
        }
    }
}
