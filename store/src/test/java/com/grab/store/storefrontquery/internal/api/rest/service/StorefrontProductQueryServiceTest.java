package com.grab.store.storefrontquery.internal.api.rest.service;

import com.grab.discovery.application.model.read.BuyableOfferResult;
import com.grab.discovery.application.model.read.GetBuyableOfferBySlugQuery;
import com.grab.discovery.application.model.read.ResolveShoppingContextQuery;
import com.grab.discovery.application.model.read.ResolveShoppingContextResult;
import com.grab.discovery.application.model.read.SearchBuyableOffersQuery;
import com.grab.discovery.application.model.read.ShoppingContext;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.storefrontquery.internal.api.rest.dto.response.BuyableOfferResponse;
import com.grab.store.storefrontquery.internal.api.rest.dto.response.ShoppingContextResponse;
import com.grab.store.storefrontquery.internal.api.rest.mapper.GetBuyableOfferBySlugRequestMapper;
import com.grab.store.storefrontquery.internal.api.rest.mapper.ResolveShoppingContextRequestMapper;
import com.grab.store.storefrontquery.internal.api.rest.mapper.SearchBuyableOffersRequestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorefrontProductQueryServiceTest {
    @Mock
    private QueryBus queryBus;
    @Mock
    private SearchBuyableOffersRequestMapper searchMapper;
    @Mock
    private GetBuyableOfferBySlugRequestMapper getMapper;
    @Mock
    private ResolveShoppingContextRequestMapper resolveContextMapper;

    private StorefrontProductQueryService service;
    private ShoppingContext context;
    private ResolveShoppingContextResult contextResult;

    @BeforeEach
    void setUp() {
        service = new StorefrontProductQueryService(queryBus, searchMapper, getMapper, resolveContextMapper);
        context = new ShoppingContext(new CommonId("web-1"), new CommonId("region-1"), "SG", "SGD", "en-SG");
        contextResult = new ResolveShoppingContextResult(context, true);
    }

    @Test
    void list_resolvesContextThenDispatchesOfferQuery() {
        String key = "pk_test";
        String keyword = "coffee";
        String categoryId = "category-1";
        Pageable pageable = Pageable.unpaged();
        ResolveShoppingContextQuery contextQuery = new ResolveShoppingContextQuery(key, "SG", "en-SG");
        SearchBuyableOffersQuery offerQuery = new SearchBuyableOffersQuery(
                context, true, keyword, new CommonId(categoryId), pageable);
        BuyableOfferResponse response = createResponse();
        Page<BuyableOfferResult> resultPage = new PageImpl<>(List.of(createResult()));

        when(resolveContextMapper.toQuery(key, "SG", "en-SG")).thenReturn(contextQuery);
        when(queryBus.dispatch(contextQuery)).thenReturn(contextResult);
        when(searchMapper.toQuery(context, true, keyword, categoryId, pageable)).thenReturn(offerQuery);
        when(queryBus.dispatch(offerQuery)).thenReturn(resultPage);
        when(searchMapper.toResponse(resultPage.getContent().getFirst())).thenReturn(response);

        Page<BuyableOfferResponse> actual = service.list(key, "SG", "en-SG", keyword, categoryId, pageable);

        assertThat(actual.getContent()).containsExactly(response);
        verify(queryBus).dispatch(contextQuery);
        verify(queryBus).dispatch(offerQuery);
    }

    @Test
    void getBySlug_resolvesContextThenDispatchesDetailQuery() {
        String key = "pk_test";
        String slug = "iced-coffee";
        ResolveShoppingContextQuery contextQuery = new ResolveShoppingContextQuery(key, "SG", "en-SG");
        GetBuyableOfferBySlugQuery offerQuery = new GetBuyableOfferBySlugQuery(context, true, slug);
        BuyableOfferResult result = createResult();
        BuyableOfferResponse response = createResponse();

        when(resolveContextMapper.toQuery(key, "SG", "en-SG")).thenReturn(contextQuery);
        when(queryBus.dispatch(contextQuery)).thenReturn(contextResult);
        when(getMapper.toQuery(context, true, slug)).thenReturn(offerQuery);
        when(queryBus.dispatch(offerQuery)).thenReturn(result);
        when(getMapper.toResponse(result)).thenReturn(response);

        BuyableOfferResponse actual = service.getBySlug(key, "SG", "en-SG", slug);

        assertThat(actual).isEqualTo(response);
        verify(queryBus).dispatch(contextQuery);
        verify(queryBus).dispatch(offerQuery);
    }

    @Test
    void resolveShoppingContext_returnsMappedContext() {
        String key = "pk_test";
        ResolveShoppingContextQuery query = new ResolveShoppingContextQuery(key, "SG", "en-SG");
        ShoppingContextResponse response = new ShoppingContextResponse("web-1", "region-1", "SG", "SGD", "en-SG", true);

        when(resolveContextMapper.toQuery(key, "SG", "en-SG")).thenReturn(query);
        when(queryBus.dispatch(query)).thenReturn(contextResult);
        when(resolveContextMapper.toResponse(contextResult)).thenReturn(response);

        assertThat(service.resolveShoppingContext(key, "SG", "en-SG")).isEqualTo(response);
        verify(queryBus).dispatch(query);
    }

    private BuyableOfferResult createResult() {
        return new BuyableOfferResult(
                new CommonId("web-1"), new CommonId("region-1"), "SG", "SGD", "en-SG", true,
                new CommonId("prod-1"), new CommonId("seller-1"), new CommonId("category-1"),
                "Iced Coffee", "iced-coffee", null, null, "ACTIVE", true, true, true, true,
                null, null, List.of(), List.of());
    }

    private BuyableOfferResponse createResponse() {
        return new BuyableOfferResponse(
                "web-1", "prod-1", "seller-1", "category-1", "Iced Coffee", "iced-coffee", null, null,
                "ACTIVE", true, true, true, List.of(), List.of(), "region-1", "SG", "SGD", "en-SG", true,
                true, null, null);
    }
}
