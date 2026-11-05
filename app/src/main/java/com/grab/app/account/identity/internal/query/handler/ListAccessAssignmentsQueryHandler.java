package com.grab.app.account.identity.internal.query.handler;

import com.grab.app.account.identity.internal.config.IdentityReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.identity.application.model.read.ListAccessAssignmentsQuery;
import com.identity.application.model.write.AccessAssignmentResult;
import com.identity.application.port.inbound.ListAccessAssignmentsUseCase;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListAccessAssignmentsQueryHandler
        implements QueryHandler<ListAccessAssignmentsQuery, List<AccessAssignmentResult>> {

    private final ListAccessAssignmentsUseCase useCase;

    public ListAccessAssignmentsQueryHandler(ListAccessAssignmentsUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @IdentityReadTransactional
    public List<AccessAssignmentResult> handle(ListAccessAssignmentsQuery query) {
        return useCase.execute(query);
    }

    @Override
    public Class<ListAccessAssignmentsQuery> getQueryType() {
        return ListAccessAssignmentsQuery.class;
    }
}
