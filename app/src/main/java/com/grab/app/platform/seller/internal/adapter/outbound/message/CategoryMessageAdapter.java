package com.grab.app.platform.seller.internal.adapter.outbound.message;

import com.grab.app.merchandising.product.port.message.CatalogCategoryPort;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.GetCategoryChildrenMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.GetCategoryMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.GetCategoryTreeMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.GetLeafCategoriesMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.SaveCategoryMessageMapper;
import com.grab.app.platform.seller.port.outbound.SellerCategoryOutboundPort;
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
    public CategoryOutboundResult saveCategory(SaveCategoryRecord record) {
        CatalogCategoryPort.CreateCategoryMessage message = saveCategoryMapper.toMessage(record);
        CatalogCategoryPort.CategoryMessageResult result = catalogCategoryPort.createCategory(message);
        return saveCategoryMapper.toResult(result);
    }

    @Override
    public Optional<CategoryOutboundResult> findCategoryById(String categoryIdStr) {
        Id categoryId = idGenerator.convertIdFrom(categoryIdStr);
        return catalogCategoryPort.findCategoryById(categoryId).map(getCategoryMapper::toResult);
    }

    @Override
    public CategoryNodeOutboundResult getCategoryTree(String rootIdStr) {
        Id rootId = (rootIdStr != null && !rootIdStr.isBlank())
                ? idGenerator.convertIdFrom(rootIdStr)
                : null;
        CatalogCategoryPort.CategoryTreeNodeResult result = catalogCategoryPort.getCategoryTree(rootId);
        return getTreeMapper.toResult(result);
    }

    @Override
    public List<CategoryOutboundResult> findChildren(String categoryIdStr) {
        Id categoryId = idGenerator.convertIdFrom(categoryIdStr);
        List<CatalogCategoryPort.CategoryMessageResult> results = catalogCategoryPort.getChildCategories(categoryId);
        return getChildrenMapper.toResultList(results);
    }

    @Override
    public List<CategoryOutboundResult> findLeafCategories(String name) {
        List<CatalogCategoryPort.CategoryMessageResult> results = catalogCategoryPort.getLeafCategories(name);
        return getLeavesMapper.toResultList(results);
    }

    @Override
    public void deleteCategory(String categoryIdStr) {
        Id categoryId = idGenerator.convertIdFrom(categoryIdStr);
        catalogCategoryPort.deleteCategory(categoryId);
    }
}
