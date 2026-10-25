package com.grab;

import com.grab.app.ordermanagement.workflows.internal.api.rest.processor.ProductSearchWorkflowLinksProcessor;
import com.grab.app.ordermanagement.workflows.internal.api.rest.processor.CatalogRootWorkflowLinksProcessor;
import com.grab.app.productmerchandising.catalog.api.representation.CatalogRootModel;
import com.grab.app.productmerchandising.catalog.api.representation.ProductSearchResponse;
import org.springframework.core.ResolvableType;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.RepresentationModelProcessorInvoker;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowRepresentationLinkProcessorTest {
    private final ProductSearchWorkflowLinksProcessor processor = new ProductSearchWorkflowLinksProcessor();

    @Test
    void activeProductGetsWorkflowLinkExactlyOnce() {
        EntityModel<ProductSearchResponse> model = EntityModel.of(product("ACTIVE"));

        processor.process(model);
        processor.process(model);

        assertThat(model.getLinks("update-sellable-product")).hasSize(1);
    }

    @Test
    void draftProductDoesNotGetUpdateSellableWorkflowLink() {
        EntityModel<ProductSearchResponse> model = EntityModel.of(product("DRAFT"));

        processor.process(model);

        assertThat(model.getLink("update-sellable-product")).isEmpty();
    }

    @Test
    void pagedProductSearchProcessesEachItemAndPreservesEmptyPages() {
        RepresentationModelProcessorInvoker invoker = new RepresentationModelProcessorInvoker(List.of(processor));
        ResolvableType pageType = ResolvableType.forClassWithGenerics(
                PagedModel.class,
                ResolvableType.forClassWithGenerics(EntityModel.class, ProductSearchResponse.class));
        PagedModel<EntityModel<ProductSearchResponse>> page = PagedModel.of(
                List.of(EntityModel.of(product("ACTIVE")), EntityModel.of(product("DRAFT"))),
                new PagedModel.PageMetadata(20, 0, 2));

        CollectionModel<EntityModel<ProductSearchResponse>> processedPage = invoker.invokeProcessorsFor(page, pageType);

        assertThat(processedPage).isInstanceOf(PagedModel.class);
        assertThat(processedPage.getContent()).hasSize(2);
        assertThat(processedPage.getContent().iterator().next().getLink("update-sellable-product")).isPresent();
        assertThat(processedPage.getContent().stream().skip(1).findFirst().orElseThrow()
                .getLink("update-sellable-product")).isEmpty();

        PagedModel<EntityModel<ProductSearchResponse>> emptyPage = PagedModel.empty(
                new PagedModel.PageMetadata(20, 0, 0),
                ResolvableType.forClassWithGenerics(EntityModel.class, ProductSearchResponse.class));
        CollectionModel<EntityModel<ProductSearchResponse>> processedEmptyPage =
                invoker.invokeProcessorsFor(emptyPage, pageType);

        assertThat(processedEmptyPage).isInstanceOf(PagedModel.class);
        assertThat(processedEmptyPage.getContent()).isEmpty();
    }

    @Test
    void onlyCatalogRootGetsTheCatalogRootWorkflowLinks() {
        CatalogRootWorkflowLinksProcessor catalogRootProcessor = new CatalogRootWorkflowLinksProcessor();
        CatalogRootModel catalogRoot = new CatalogRootModel();

        catalogRootProcessor.process(catalogRoot);
        catalogRootProcessor.process(catalogRoot);

        assertThat(catalogRoot.getLinks("create-sellable-product")).hasSize(1);
        assertThat(catalogRoot.getLinks("update-sellable-product")).hasSize(1);
        assertThat(catalogRoot.getLinks("update-product-variant")).hasSize(1);
    }

    @Test
    void nonActiveProductsDoNotGetSearchWorkflowLinks() {
        for (String status : List.of("DRAFT", "ARCHIVED", "SUSPENDED")) {
            EntityModel<ProductSearchResponse> model = EntityModel.of(product(status));

            processor.process(model);

            assertThat(model.getLink("update-sellable-product")).as(status).isEmpty();
        }
    }

    private ProductSearchResponse product(String status) {
        return new ProductSearchResponse("prod-1", "Shirt", status, "shirt", "Apparel", "cat-1", null, List.of());
    }
}
