package com.grab.store.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.identity.internal.config.IdentityReadTransactional;
import com.identity.application.port.inbound.ListAccessAssignmentsUseCase;
import com.identity.application.model.read.ListAccessAssignmentsQuery;
import com.identity.application.model.write.AccessAssignmentResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListAccessAssignmentsQueryHandler implements QueryHandler<ListAccessAssignmentsQuery, List<AccessAssignmentResult>> {

    private final ListAccessAssignmentsUseCase listAccessAssignmentsUseCase;

    @Override
    @IdentityReadTransactional
    public List<AccessAssignmentResult> handle(ListAccessAssignmentsQuery query) {
        return listAccessAssignmentsUseCase.execute(query);
    }

    @Override
    public Class<ListAccessAssignmentsQuery> getQueryType() {
        return ListAccessAssignmentsQuery.class;
    }
}
