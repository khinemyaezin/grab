package com.inventory.application.port.inbound;

import com.inventory.application.model.read.SearchBinsQuery;
import org.springframework.data.domain.Page;
import com.inventory.application.model.read.SearchBinsResult;

public interface SearchBinsUseCase {
    Page<SearchBinsResult> execute(SearchBinsQuery query);
}
