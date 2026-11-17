package com.grab.service.sellerbff.internal.adapter.outbound.message;

import lombok.SneakyThrows;
import com.grab.service.contract.message.merchandising.catalog.CatalogCategoryPort;
import com.grab.service.sellerbff.internal.adapter.outbound.message.mapper.GetCategoryChildrenMessageMapper;
import com.grab.service.sellerbff.internal.adapter.outbound.message.mapper.GetCategoryMessageMapper;
import com.grab.service.sellerbff.internal.adapter.outbound.message.mapper.GetCategoryTreeMessageMapper;
import com.grab.service.sellerbff.internal.adapter.outbound.message.mapper.GetLeafCategoriesMessageMapper;
import com.grab.service.sellerbff.internal.adapter.outbound.message.mapper.SaveCategoryMessageMapper;
import com.grab.service.sellerbff.port.outbound.SellerCategoryOutboundPort;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CategoryMessageAdapter implements SellerCategoryOutboundPort {

    private final CatalogCategoryPort catalogCategoryPort;
    private final IdGenerator idGenerator;
    private final SaveCategoryMessageMapper saveCategoryMapper;
    private final GetCategoryMessageMapper getCategoryMapper;
    private final GetCategoryTreeMessageMapper getTreeMapper;
    private final GetCategoryChildrenMessageMapper getChildrenMapper;
    private final GetLeafCategoriesMessageMapper getLeavesMapper;

    public CategoryMessageAdapter(
            CatalogCategoryPort catalogCategoryPort,
            IdGenerator idGenerator,
            SaveCategoryMessageMapper saveCategoryMapper,
            GetCategoryMessageMapper getCategoryMapper,
            GetCategoryTreeMessageMapper getTreeMapper,
            GetCategoryChildrenMessageMapper getChildrenMapper,
            GetLeafCategoriesMessageMapper getLeavesMapper
    ) {
        this.catalogCategoryPort = catalogCategoryPort;
        this.idGenerator = idGenerator;
        this.saveCategoryMapper = saveCategoryMapper;
        this.getCategoryMapper = getCategoryMapper;
        this.getTreeMapper = getTreeMapper;
        this.getChildrenMapper = getChildrenMapper;
        this.getLeavesMapper = getLeavesMapper;
    }

    @Override
    @SneakyThrows
    public CategoryOutboundResult saveCategory(SaveCategoryRecord record) {
        CatalogCategoryPort.CreateCategoryMessage message = saveCategoryMapper.toMessage(record);
        CatalogCategoryPort.CategoryMessageResult result = catalogCategoryPort.createCategory(message);
        return saveCategoryMapper.toResult(result);
    }

    @Override
    @SneakyThrows
    public Optional<CategoryOutboundResult> findCategoryById(String categoryIdStr) {
        Id categoryId = idGenerator.convertIdFrom(categoryIdStr);
        return java.util.Optional.ofNullable(catalogCategoryPort.findCategoryById(categoryId)).map(getCategoryMapper::toResult);
    }

    @Override
    @SneakyThrows
    public CategoryNodeOutboundResult getCategoryTree(String rootIdStr) {
        Id rootId = (rootIdStr != null && !rootIdStr.isBlank())
                ? idGenerator.convertIdFrom(rootIdStr)
                : null;
        CatalogCategoryPort.CategoryTreeNodeResult result = catalogCategoryPort.getCategoryTree(rootId);
        return getTreeMapper.toResult(result);
    }

    @Override
    @SneakyThrows
    public List<CategoryOutboundResult> findChildren(String categoryIdStr) {
        Id categoryId = idGenerator.convertIdFrom(categoryIdStr);
        List<CatalogCategoryPort.CategoryMessageResult> results = catalogCategoryPort.getChildCategories(categoryId);
        return getChildrenMapper.toResultList(results);
    }

    @Override
    @SneakyThrows
    public List<CategoryOutboundResult> findLeafCategories(String name) {
        List<CatalogCategoryPort.CategoryMessageResult> results = catalogCategoryPort.getLeafCategories(name);
        return getLeavesMapper.toResultList(results);
    }

    @Override
    @SneakyThrows
    public void deleteCategory(String categoryIdStr) {
        Id categoryId = idGenerator.convertIdFrom(categoryIdStr);
        catalogCategoryPort.deleteCategory(categoryId);
    }
}
