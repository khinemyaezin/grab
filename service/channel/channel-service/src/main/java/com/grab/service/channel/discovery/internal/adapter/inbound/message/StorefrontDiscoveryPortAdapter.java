package com.grab.service.channel.discovery.internal.adapter.inbound.message;

import com.grab.service.channel.discovery.internal.adapter.inbound.message.mapper.GetBuyableOfferBySlugPortMapper;
import com.grab.service.channel.discovery.internal.adapter.inbound.message.mapper.ResolveShoppingContextPortMapper;
import com.grab.service.channel.discovery.internal.adapter.inbound.message.mapper.SearchBuyableOffersPortMapper;
import com.grab.service.contract.message.channel.discovery.StorefrontDiscoveryPort;
import com.grab.discovery.application.model.read.BuyableOfferResult;
import com.grab.discovery.application.model.read.GetBuyableOfferBySlugQuery;
import com.grab.discovery.application.model.read.ResolveShoppingContextQuery;
import com.grab.discovery.application.model.read.ResolveShoppingContextResult;
import com.grab.discovery.application.model.read.SearchBuyableOffersQuery;
import com.grab.framework.cqrs.query.QueryBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import com.grab.rpc.adapter.RmiExport;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RmiExport
public class StorefrontDiscoveryPortAdapter implements StorefrontDiscoveryPort {

    private final QueryBus queryBus;
    private final SearchBuyableOffersPortMapper searchMapper;
    private final GetBuyableOfferBySlugPortMapper getBySlugMapper;
    private final ResolveShoppingContextPortMapper resolveContextMapper;

    public StorefrontDiscoveryPortAdapter(
            @Qualifier("channelQueryBus") QueryBus queryBus,
            SearchBuyableOffersPortMapper searchMapper,
            GetBuyableOfferBySlugPortMapper getBySlugMapper,
            ResolveShoppingContextPortMapper resolveContextMapper) {
        this.queryBus = queryBus;
        this.searchMapper = searchMapper;
        this.getBySlugMapper = getBySlugMapper;
        this.resolveContextMapper = resolveContextMapper;
    }

    @Override
    public List<StorefrontOfferMessageResult> searchProducts(StorefrontSearchCriteria criteria) {
        ResolveShoppingContextQuery ctxQuery = resolveContextMapper.toQuery(
                criteria.publishableKey(), criteria.countryCode(), criteria.locale()
        );
        ResolveShoppingContextResult ctxResult = queryBus.dispatch(ctxQuery);
        if (ctxResult == null || ctxResult.context() == null) {
            return Collections.emptyList();
        }
        SearchBuyableOffersQuery query = new SearchBuyableOffersQuery(
                ctxResult.context(),
                ctxResult.regionServed(),
                criteria.keyword(),
                criteria.categoryId(),
                PageRequest.of(criteria.page(), criteria.size())
        );
        Page<BuyableOfferResult> results = queryBus.dispatch(query);
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }
        return results.map(searchMapper::toResponse).toList();
    }

    @Override
    public StorefrontOfferMessageResult findProductBySlug(
            String publishableKey, String countryCode, String locale, String slug) {
        ResolveShoppingContextQuery ctxQuery = resolveContextMapper.toQuery(
                publishableKey, countryCode, locale
        );
        ResolveShoppingContextResult ctxResult = queryBus.dispatch(ctxQuery);
        if (ctxResult == null || ctxResult.context() == null) {
            return null;
        }
        GetBuyableOfferBySlugQuery query = new GetBuyableOfferBySlugQuery(
                ctxResult.context(),
                ctxResult.regionServed(),
                slug
        );
        BuyableOfferResult result = queryBus.dispatch(query);
        return getBySlugMapper.toResponse(result);
    }

    @Override
    public ResolvedContextMessageResult resolveShoppingContext(
            String publishableKey, String countryCode, String locale) {
        ResolveShoppingContextQuery ctxQuery = resolveContextMapper.toQuery(
                publishableKey, countryCode, locale
        );
        ResolveShoppingContextResult result = queryBus.dispatch(ctxQuery);
        return resolveContextMapper.toResponse(result);
    }
}
