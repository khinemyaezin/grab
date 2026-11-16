package com.grab.workflow.infrastructure.adapter;

import com.grab.framework.workflow.WorkflowReceiptUniquenessException;
import jakarta.persistence.EntityManager;
import org.hibernate.exception.ConstraintViolationException;

final class WorkflowReceiptInsert {

    private WorkflowReceiptInsert() {
    }

    static void persist(EntityManager entityManager, Object entity, String constraintName) {
        try {
            entityManager.persist(entity);
            entityManager.flush();
        } catch (RuntimeException exception) {
            for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
                if (cause instanceof ConstraintViolationException violation
                        && "23505".equals(violation.getSQLState())
                        && constraintName.equals(violation.getConstraintName())) {
                    throw new WorkflowReceiptUniquenessException("Concurrent workflow receipt insert", exception);
                }
            }
            throw exception;
        }
    }
}
