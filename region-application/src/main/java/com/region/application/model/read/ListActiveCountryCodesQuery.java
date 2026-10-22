package com.region.application.model.read;

import com.grab.framework.cqrs.query.Query;

import java.util.List;

public record ListActiveCountryCodesQuery() implements Query<List<String>> {
}
