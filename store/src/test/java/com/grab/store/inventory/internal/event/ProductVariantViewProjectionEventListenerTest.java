package com.grab.store.inventory.internal.event;

import com.grab.store.catalog.events.ProductDeletedIntegrationEvent;
import com.grab.store.catalog.events.ProductNameChangedIntegrationEvent;
import com.grab.store.catalog.events.ProductVariantAddedIntegrationEvent;
import com.grab.store.catalog.events.ProductVariantDeletedIntegrationEvent;
import com.grab.store.catalog.events.ProductVariantRestoredIntegrationEvent;
import com.grab.store.catalog.events.ProductVariantUpdatedIntegrationEvent;
import com.grab.store.workflows.events.ProductVariantViewProjectedEvent;
import com.inventory.infrastructure.entity.ProductVariantViewEntity;
import com.inventory.infrastructure.repository.jpa.ProductVariantViewJpaRepository;
import com.inventory.infrastructure.view.ProductView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class ProductVariantViewProjectionEventListenerTest {

    private InMemoryProductVariantViewJpaRepository repository;
    private List<Object> published;
    private ProductVariantViewProjectionEventListener listener;

    @BeforeEach
    void setUp() {
        repository = new InMemoryProductVariantViewJpaRepository();
        published = new ArrayList<>();
        listener = new ProductVariantViewProjectionEventListener(repository, published::add);
    }

    @Test
    void onVariantAdded_shouldInsertNewViewRow() {
        listener.onVariantAdded(new ProductVariantAddedIntegrationEvent(
                "product-1", "variant-1", "SKU001", "T-Shirt", Instant.now(), 1));

        ProductVariantViewEntity saved = repository.findByVariantUuid("variant-1").orElseThrow();
        assertThat(saved.getVariantUuid()).isEqualTo("variant-1");
        assertThat(saved.getProductUuid()).isEqualTo("product-1");
        assertThat(saved.getSku()).isEqualTo("SKU001");
        assertThat(saved.getProductName()).isEqualTo("T-Shirt");
        assertThat(saved.getStatus()).isEqualTo(ProductVariantViewEntity.STATUS_ACTIVE);
        assertThat(published).hasSize(1);
        assertThat(published.getFirst()).isInstanceOf(ProductVariantViewProjectedEvent.class);
    }

    @Test
    void onVariantAdded_shouldUpsertExistingRowOnRedelivery() {
        ProductVariantViewEntity existing = new ProductVariantViewEntity();
        existing.setVariantUuid("variant-1");
        existing.setProductUuid("product-1");
        existing.setSku("OLD");
        existing.setStatus(ProductVariantViewEntity.STATUS_DELETED);
        repository.save(existing);

        listener.onVariantAdded(new ProductVariantAddedIntegrationEvent(
                "product-1", "variant-1", "SKU001", "T-Shirt", Instant.now(), 1));

        ProductVariantViewEntity saved = repository.findByVariantUuid("variant-1").orElseThrow();
        assertThat(saved.getSku()).isEqualTo("SKU001");
        assertThat(saved.getStatus()).isEqualTo(ProductVariantViewEntity.STATUS_ACTIVE);
    }

    @Test
    void onVariantUpdated_shouldUpdateSku() {
        ProductVariantViewEntity existing = new ProductVariantViewEntity();
        existing.setVariantUuid("variant-1");
        existing.setProductUuid("product-1");
        existing.setSku("SKU001");
        existing.setStatus(ProductVariantViewEntity.STATUS_ACTIVE);
        repository.save(existing);

        listener.onVariantUpdated(new ProductVariantUpdatedIntegrationEvent(
                "product-1", "variant-1", "SKU002", Instant.now(), 1));

        assertThat(repository.findByVariantUuid("variant-1").orElseThrow().getSku()).isEqualTo("SKU002");
    }

    @Test
    void onVariantDeleted_shouldMarkRowDeleted() {
        ProductVariantViewEntity existing = new ProductVariantViewEntity();
        existing.setVariantUuid("variant-1");
        existing.setStatus(ProductVariantViewEntity.STATUS_ACTIVE);
        repository.save(existing);

        listener.onVariantDeleted(new ProductVariantDeletedIntegrationEvent(
                "product-1", "variant-1", Instant.now(), 1));

        assertThat(repository.findByVariantUuid("variant-1").orElseThrow().getStatus())
                .isEqualTo(ProductVariantViewEntity.STATUS_DELETED);
    }

    @Test
    void onVariantDeleted_shouldIgnoreUnknownVariant() {
        listener.onVariantDeleted(new ProductVariantDeletedIntegrationEvent(
                "product-1", "variant-1", Instant.now(), 1));

        assertThat(repository.findByVariantUuid("variant-1")).isEmpty();
    }

    @Test
    void onVariantRestored_shouldMarkRowActive() {
        ProductVariantViewEntity existing = new ProductVariantViewEntity();
        existing.setVariantUuid("variant-1");
        existing.setStatus(ProductVariantViewEntity.STATUS_DELETED);
        repository.save(existing);

        listener.onVariantRestored(new ProductVariantRestoredIntegrationEvent(
                "product-1", "variant-1", Instant.now(), 1));

        assertThat(repository.findByVariantUuid("variant-1").orElseThrow().getStatus())
                .isEqualTo(ProductVariantViewEntity.STATUS_ACTIVE);
    }

    @Test
    void onProductNameChanged_shouldRenameAllVariantRows() {
        ProductVariantViewEntity first = new ProductVariantViewEntity();
        first.setVariantUuid("variant-1");
        first.setProductUuid("product-1");
        first.setSku("SKU1");
        first.setProductName("Old");
        first.setStatus(ProductVariantViewEntity.STATUS_ACTIVE);
        ProductVariantViewEntity second = new ProductVariantViewEntity();
        second.setVariantUuid("variant-2");
        second.setProductUuid("product-1");
        second.setSku("SKU2");
        second.setProductName("Old");
        second.setStatus(ProductVariantViewEntity.STATUS_ACTIVE);
        repository.save(first);
        repository.save(second);

        listener.onProductNameChanged(new ProductNameChangedIntegrationEvent(
                "product-1", "New Name", Instant.now(), 1));

        assertThat(repository.findAllByProductUuid("product-1"))
                .extracting(ProductVariantViewEntity::getProductName)
                .containsOnly("New Name");
    }

    @Test
    void onProductDeleted_shouldMarkAllVariantRowsDeleted() {
        ProductVariantViewEntity first = new ProductVariantViewEntity();
        first.setVariantUuid("variant-1");
        first.setProductUuid("product-1");
        first.setSku("SKU1");
        first.setStatus(ProductVariantViewEntity.STATUS_ACTIVE);
        ProductVariantViewEntity second = new ProductVariantViewEntity();
        second.setVariantUuid("variant-2");
        second.setProductUuid("product-1");
        second.setSku("SKU2");
        second.setStatus(ProductVariantViewEntity.STATUS_ACTIVE);
        repository.save(first);
        repository.save(second);

        listener.onProductDeleted(new ProductDeletedIntegrationEvent(
                "product-1", List.of("variant-1", "variant-2"), Instant.now(), 1));

        assertThat(repository.findAllByProductUuid("product-1"))
                .extracting(ProductVariantViewEntity::getStatus)
                .containsOnly(ProductVariantViewEntity.STATUS_DELETED);
    }

    private static final class InMemoryProductVariantViewJpaRepository implements ProductVariantViewJpaRepository {

        private final AtomicLong ids = new AtomicLong();
        private final List<ProductVariantViewEntity> rows = new ArrayList<>();

        @Override
        public Optional<ProductVariantViewEntity> findByVariantUuid(String variantUuid) {
            return rows.stream().filter(row -> variantUuid.equals(row.getVariantUuid())).findFirst();
        }

        @Override
        public List<ProductVariantViewEntity> findAllByProductUuid(String productUuid) {
            return rows.stream().filter(row -> productUuid.equals(row.getProductUuid())).toList();
        }

        @Override
        public Optional<ProductView> findBySkuAndStatus(String sku, String status) {
            return Optional.empty();
        }

        @Override
        public List<ProductView> findAllBySkuIn(Collection<String> skus) {
            return List.of();
        }

        @Override
        public List<ProductView> findBySkuContainingIgnoreCaseAndStatus(String sku, String status) {
            return List.of();
        }

        @Override
        public void flush() {
        }

        @Override
        public <S extends ProductVariantViewEntity> S saveAndFlush(S entity) {
            return save(entity);
        }

        @Override
        public <S extends ProductVariantViewEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
            return saveAll(entities);
        }

        @Override
        public void deleteAllInBatch(Iterable<ProductVariantViewEntity> entities) {
        }

        @Override
        public void deleteAllByIdInBatch(Iterable<Long> longs) {
        }

        @Override
        public void deleteAllInBatch() {
        }

        @Override
        public ProductVariantViewEntity getOne(Long aLong) {
            return findById(aLong).orElse(null);
        }

        @Override
        public ProductVariantViewEntity getById(Long aLong) {
            return findById(aLong).orElse(null);
        }

        @Override
        public ProductVariantViewEntity getReferenceById(Long aLong) {
            return findById(aLong).orElse(null);
        }

        @Override
        public <S extends ProductVariantViewEntity> Optional<S> findOne(Example<S> example) {
            return Optional.empty();
        }

        @Override
        public <S extends ProductVariantViewEntity> List<S> findAll(Example<S> example) {
            return List.of();
        }

        @Override
        public <S extends ProductVariantViewEntity> List<S> findAll(Example<S> example, Sort sort) {
            return List.of();
        }

        @Override
        public <S extends ProductVariantViewEntity> Page<S> findAll(Example<S> example, Pageable pageable) {
            return Page.empty();
        }

        @Override
        public <S extends ProductVariantViewEntity> long count(Example<S> example) {
            return 0;
        }

        @Override
        public <S extends ProductVariantViewEntity> boolean exists(Example<S> example) {
            return false;
        }

        @Override
        public <S extends ProductVariantViewEntity, R> R findBy(
                Example<S> example,
                Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction
        ) {
            return null;
        }

        @Override
        public <S extends ProductVariantViewEntity> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(ids.incrementAndGet());
                rows.removeIf(row -> entity.getVariantUuid() != null && entity.getVariantUuid().equals(row.getVariantUuid()));
                rows.add(entity);
            } else {
                rows.removeIf(row -> entity.getId().equals(row.getId()));
                rows.add(entity);
            }
            return entity;
        }

        @Override
        public <S extends ProductVariantViewEntity> List<S> saveAll(Iterable<S> entities) {
            List<S> saved = new ArrayList<>();
            for (S entity : entities) {
                saved.add(save(entity));
            }
            return saved;
        }

        @Override
        public Optional<ProductVariantViewEntity> findById(Long aLong) {
            return rows.stream().filter(row -> aLong.equals(row.getId())).findFirst();
        }

        @Override
        public boolean existsById(Long aLong) {
            return findById(aLong).isPresent();
        }

        @Override
        public List<ProductVariantViewEntity> findAll() {
            return List.copyOf(rows);
        }

        @Override
        public List<ProductVariantViewEntity> findAllById(Iterable<Long> longs) {
            return List.of();
        }

        @Override
        public long count() {
            return rows.size();
        }

        @Override
        public void deleteById(Long aLong) {
        }

        @Override
        public void delete(ProductVariantViewEntity entity) {
        }

        @Override
        public void deleteAllById(Iterable<? extends Long> longs) {
        }

        @Override
        public void deleteAll(Iterable<? extends ProductVariantViewEntity> entities) {
        }

        @Override
        public void deleteAll() {
            rows.clear();
        }

        @Override
        public List<ProductVariantViewEntity> findAll(Sort sort) {
            return findAll();
        }

        @Override
        public Page<ProductVariantViewEntity> findAll(Pageable pageable) {
            return Page.empty();
        }
    }
}
