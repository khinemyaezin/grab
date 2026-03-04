package com.grab.store.catalog.internal.api.rest.controller;

import com.catalog.domain.aggregate.Category;
import com.catalog.domain.repository.CategoryRepository;
import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.repository.jpa.CategoryJpaRepo;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.catalog.internal.api.rest.assembler.CategoryChildrenModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.CategoryModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.CategoryNodeModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.DeleteCategoryModelAssembler;
import com.grab.store.catalog.internal.api.rest.mapper.CategoryChildrenDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.CategoryDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.CategoryNodeDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.IdConverter;
import com.grab.store.catalog.internal.api.rest.mapper.SaveCategoryDtoMapper;
import com.grab.store.catalog.internal.api.rest.service.CategoryFacadeService;
import com.grab.store.catalog.internal.command.handler.DeleteCategoryCommandHandler;
import com.grab.store.catalog.internal.command.handler.SaveCategoryCommandHandler;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.cqrs.command.impl.DefaultCommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.cqrs.query.impl.DefaultQueryBus;
import com.grab.store.catalog.internal.query.handler.GetCategoryChildrenQueryHandler;
import com.grab.store.catalog.internal.query.handler.GetCategoryParentQueryHandler;
import com.grab.store.catalog.internal.query.handler.GetCategoryQueryHandler;
import com.grab.store.catalog.internal.query.handler.GetCategoryTreeQueryHandler;
import com.grab.framework.id.impl.UuidGenerator;
import com.nestedset.app.NestedSetNodeRepository;
import com.nestedset.library.model.NodeComponent;
import com.catalog.infrastructure.view.CategoryComposite;
import com.catalog.infrastructure.view.CategoryLeaf;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@TestConfiguration
public class CategoryControllerTestConfig {

    @Bean
    public IdGenerator idGenerator() {
        return new UuidGenerator();
    }

    @Bean
    public InMemoryCategoryStore inMemoryCategoryStore() {
        return new InMemoryCategoryStore();
    }

    @Bean
    public CategoryRepository categoryRepository(InMemoryCategoryStore store) {
        return new InMemoryCategoryRepository(store);
    }

    @Bean
    public NestedSetNodeRepository<CategoryEntity, Long> categoryNodeRepository(InMemoryCategoryStore store) {
        return new InMemoryNestedSetNodeRepository(store);
    }

    @Bean
    public CategoryJpaRepo categoryJpaRepo(InMemoryCategoryStore store) {
        return (CategoryJpaRepo) Proxy.newProxyInstance(
                CategoryJpaRepo.class.getClassLoader(),
                new Class[]{CategoryJpaRepo.class},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return switch (method.getName()) {
                            case "toString" -> "CategoryJpaRepoProxy";
                            case "hashCode" -> System.identityHashCode(proxy);
                            case "equals" -> proxy == args[0];
                            default -> null;
                        };
                    }
                    if (method.getName().equals("findByUuid") && args != null && args.length == 1) {
                        return store.findEntityByUuid((String) args[0]);
                    }
                    if (method.getName().equals("save") && args != null && args.length == 1) {
                        return args[0];
                    }
                    throw new UnsupportedOperationException("Unsupported method: " + method.getName());
                }
        );
    }

    @Bean
    public SaveCategoryDtoMapper saveCategoryDtoMapper() {
        return Mappers.getMapper(SaveCategoryDtoMapper.class);
    }

    @Bean
    public CategoryDtoMapper categoryDtoMapper() {
        return Mappers.getMapper(CategoryDtoMapper.class);
    }

    @Bean
    public CategoryNodeDtoMapper categoryNodeDtoMapper() {
        return Mappers.getMapper(CategoryNodeDtoMapper.class);
    }

    @Bean
    public CategoryChildrenDtoMapper categoryChildrenDtoMapper() {
        return Mappers.getMapper(CategoryChildrenDtoMapper.class);
    }

    @Bean
    public CategoryModelAssembler categoryModelAssembler() {
        return new CategoryModelAssembler();
    }

    @Bean
    public CategoryNodeModelAssembler categoryNodeModelAssembler() {
        return new CategoryNodeModelAssembler();
    }

    @Bean
    public CategoryChildrenModelAssembler categoryChildrenModelAssembler() {
        return new CategoryChildrenModelAssembler();
    }

    @Bean
    public DeleteCategoryModelAssembler deleteCategoryModelAssembler() {
        return new DeleteCategoryModelAssembler();
    }

    @Bean
    public IdConverter idConverter(IdGenerator idGenerator) {
        return new IdConverter(idGenerator);
    }

    @Bean
    public QueryBus queryBus(List<QueryHandler<?, ?>> queryHandlers) {
        return new DefaultQueryBus(queryHandlers);
    }

    @Bean
    public CommandBus commandBus(List<CommandHandler<?, ?>> commandHandlers) {
        return new DefaultCommandBus(commandHandlers);
    }

    @Bean
    public SaveCategoryCommandHandler saveCategoryCommandHandler(CategoryRepository categoryRepository, IdGenerator idGenerator) {
        return new SaveCategoryCommandHandler(categoryRepository, idGenerator);
    }

    @Bean
    public DeleteCategoryCommandHandler deleteCategoryCommandHandler(CategoryRepository categoryRepository) {
        return new DeleteCategoryCommandHandler(categoryRepository);
    }

    @Bean
    public GetCategoryQueryHandler getCategoryQueryHandler(CategoryRepository categoryRepository, IdGenerator idGenerator) {
        return new GetCategoryQueryHandler(categoryRepository, idGenerator);
    }

    @Bean
    public GetCategoryParentQueryHandler getCategoryParentQueryHandler(CategoryJpaRepo categoryJpaRepo,
                                                                      NestedSetNodeRepository<CategoryEntity, Long> nodeRepository) {
        return new GetCategoryParentQueryHandler(categoryJpaRepo, nodeRepository);
    }

    @Bean
    public GetCategoryTreeQueryHandler getCategoryTreeQueryHandler(CategoryJpaRepo categoryJpaRepo,
                                                                  NestedSetNodeRepository<CategoryEntity, Long> nodeRepository) {
        return new GetCategoryTreeQueryHandler(categoryJpaRepo, nodeRepository);
    }

    @Bean
    public GetCategoryChildrenQueryHandler getCategoryChildrenQueryHandler(CategoryJpaRepo categoryJpaRepo,
                                                                          NestedSetNodeRepository<CategoryEntity, Long> nodeRepository) {
        return new GetCategoryChildrenQueryHandler(categoryJpaRepo, nodeRepository);
    }

    @Bean
    public CategoryFacadeService categoryFacadeService(
            CommandBus commandBus,
            QueryBus queryBus,
            SaveCategoryDtoMapper saveCategoryDtoMapper,
            CategoryDtoMapper categoryDtoMapper,
            CategoryNodeDtoMapper categoryNodeDtoMapper,
            CategoryChildrenDtoMapper categoryChildrenDtoMapper,
            CategoryModelAssembler categoryModelAssembler,
            CategoryNodeModelAssembler categoryNodeModelAssembler,
            CategoryChildrenModelAssembler categoryChildrenModelAssembler,
            DeleteCategoryModelAssembler deleteCategoryModelAssembler,
            IdGenerator idGenerator
    ) {
        return new CategoryFacadeService(
                commandBus,
                queryBus,
                saveCategoryDtoMapper,
                categoryDtoMapper,
                categoryNodeDtoMapper,
                categoryChildrenDtoMapper,
                categoryModelAssembler,
                categoryNodeModelAssembler,
                categoryChildrenModelAssembler,
                deleteCategoryModelAssembler,
                idGenerator
        );
    }

    static class InMemoryCategoryRepository implements CategoryRepository {
        private final InMemoryCategoryStore store;

        InMemoryCategoryRepository(InMemoryCategoryStore store) {
            this.store = store;
        }

        @Override
        public void save(Category category) {
            store.save(category);
        }

        @Override
        public Optional<Category> find(Id id) {
            return store.findCategory(id.getValue());
        }

        @Override
        public void deleteCascade(Category category) {
            store.deleteSubtree(category.getId().getValue());
        }
    }

    static class InMemoryNestedSetNodeRepository implements NestedSetNodeRepository<CategoryEntity, Long> {
        private final InMemoryCategoryStore store;

        InMemoryNestedSetNodeRepository(InMemoryCategoryStore store) {
            this.store = store;
        }

        @Override
        public void insertAsFirstRoot(CategoryEntity node) {
            store.saveEntity(node, null);
        }

        @Override
        public void insertAsLastChildOf(CategoryEntity node, CategoryEntity parent) {
            store.saveEntity(node, parent.getUuid());
        }

        @Override
        public void removeSubtree(CategoryEntity node) {
            store.deleteSubtree(node.getUuid());
        }

        @Override
        public NodeComponent<CategoryEntity> getImmediateChildren(CategoryEntity node) {
            CategoryComposite<CategoryEntity> root = new CategoryComposite<>(node);
            Set<CategoryEntity> children = store.getChildren(node.getUuid());
            for (CategoryEntity child : children) {
                NodeComponent<CategoryEntity> childNode = leafOrComposite(child);
                childNode.setParent(root);
                root.addChild(childNode);
            }
            return root;
        }

        @Override
        public Optional<CategoryEntity> getParent(CategoryEntity node) {
            return store.getParent(node.getUuid());
        }

        @Override
        public NodeComponent<CategoryEntity> getTree(CategoryEntity node) {
            return store.getTree(node.getUuid());
        }

        private NodeComponent<CategoryEntity> leafOrComposite(CategoryEntity entity) {
            if (store.hasChildren(entity.getUuid())) {
                return buildComposite(entity);
            }
            return new CategoryLeaf<>(entity);
        }

        private NodeComponent<CategoryEntity> buildComposite(CategoryEntity entity) {
            CategoryComposite<CategoryEntity> composite = new CategoryComposite<>(entity);
            Set<CategoryEntity> children = store.getChildren(entity.getUuid());
            for (CategoryEntity child : children) {
                NodeComponent<CategoryEntity> childNode = leafOrComposite(child);
                childNode.setParent(composite);
                composite.addChild(childNode);
            }
            return composite;
        }
    }

    static class InMemoryCategoryStore {
        private final Map<String, Category> categories = new ConcurrentHashMap<>();
        private final Map<String, CategoryEntity> entities = new ConcurrentHashMap<>();
        private final Map<String, String> parentByUuid = new ConcurrentHashMap<>();
        private final Map<String, Set<String>> childrenByUuid = new ConcurrentHashMap<>();
        private final AtomicLong ids = new AtomicLong(1);

        void save(Category category) {
            categories.put(category.getId().getValue(), category);
            saveEntity(buildEntity(category), category.getParentId().map(Id::getValue).orElse(null));
        }

        void saveEntity(CategoryEntity entity, String parentUuid) {
            if (entity.getId() == null) {
                entity.setId(ids.getAndIncrement());
            }
            entities.put(entity.getUuid(), entity);
            if (parentUuid == null || parentUuid.isBlank()) {
                parentByUuid.remove(entity.getUuid());
            } else {
                parentByUuid.put(entity.getUuid(), parentUuid);
                childrenByUuid.computeIfAbsent(parentUuid, key -> ConcurrentHashMap.newKeySet()).add(entity.getUuid());
            }
        }

        Optional<Category> findCategory(String uuid) {
            return Optional.ofNullable(categories.get(uuid));
        }

        Optional<CategoryEntity> findEntityByUuid(String uuid) {
            return Optional.ofNullable(entities.get(uuid));
        }

        Optional<CategoryEntity> getParent(String uuid) {
            String parentId = parentByUuid.get(uuid);
            if (parentId == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(entities.get(parentId));
        }

        boolean hasChildren(String uuid) {
            return childrenByUuid.containsKey(uuid) && !childrenByUuid.get(uuid).isEmpty();
        }

        Set<CategoryEntity> getChildren(String uuid) {
            Set<String> childIds = childrenByUuid.getOrDefault(uuid, Set.of());
            return childIds.stream()
                    .map(entities::get)
                    .filter(child -> child != null)
                    .collect(Collectors.toSet());
        }

        NodeComponent<CategoryEntity> getTree(String uuid) {
            CategoryEntity entity = entities.get(uuid);
            if (entity == null) {
                return null;
            }
            return buildTree(entity);
        }

        void deleteSubtree(String uuid) {
            Deque<String> stack = new ArrayDeque<>();
            stack.push(uuid);
            while (!stack.isEmpty()) {
                String current = stack.pop();
                Set<String> children = childrenByUuid.getOrDefault(current, Set.of());
                children.forEach(stack::push);
                childrenByUuid.remove(current);
                categories.remove(current);
                entities.remove(current);
                parentByUuid.remove(current);
            }
            parentByUuid.values().removeIf(parent -> parent.equals(uuid));
            childrenByUuid.values().forEach(children -> children.remove(uuid));
        }

        private NodeComponent<CategoryEntity> buildTree(CategoryEntity entity) {
            if (!hasChildren(entity.getUuid())) {
                return new CategoryLeaf<>(entity);
            }
            CategoryComposite<CategoryEntity> composite = new CategoryComposite<>(entity);
            for (CategoryEntity child : getChildren(entity.getUuid())) {
                NodeComponent<CategoryEntity> childNode = buildTree(child);
                childNode.setParent(composite);
                composite.addChild(childNode);
            }
            return composite;
        }

        private CategoryEntity buildEntity(Category category) {
            CategoryEntity entity = entities.getOrDefault(category.getId().getValue(), new CategoryEntity());
            entity.setUuid(category.getId().getValue());
            entity.setName(category.getName());
            entity.setActive(true);
            return entity;
        }
    }
}
