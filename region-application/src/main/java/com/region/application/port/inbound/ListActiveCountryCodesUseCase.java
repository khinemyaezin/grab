package com.region.application.port.inbound;

import com.region.application.model.read.ListActiveCountryCodesQuery;

import java.util.List;

public interface ListActiveCountryCodesUseCase {
    List<String> execute(ListActiveCountryCodesQuery query);
}
