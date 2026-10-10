package com.grab.store.identity.internal.config;

import com.grab.framework.cqrs.command.CommandBus;
import com.identity.adapter.persistence.exception.IdentityInfraError;
import com.identity.adapter.persistence.exception.IdentityInfraException;
import com.identity.application.model.write.EnsureSecurityCatalogStateCommand;
import com.identity.application.model.write.EnsureSecurityCatalogStateResult;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class SecurityCatalogInitializationAspect {
    private final ObjectProvider<CommandBus> commandBusProvider;

    public SecurityCatalogInitializationAspect(ObjectProvider<CommandBus> commandBusProvider) {
        this.commandBusProvider = commandBusProvider;
    }

    @Around("@annotation(com.grab.store.identity.internal.config.RequiresSecurityCatalog)")
    public Object ensureCatalogExists(ProceedingJoinPoint invocation) throws Throwable {
        CommandBus commandBus = commandBusProvider.getObject();
        var ensureCommand = new EnsureSecurityCatalogStateCommand(true);
        try {
            commandBus.dispatch(ensureCommand);
        } catch (IdentityInfraException exception) {
            if (!(exception.getMessageSource() instanceof IdentityInfraError.SecurityCatalogInitializationRace)) {
                throw exception;
            }
            var verifyCommand = new EnsureSecurityCatalogStateCommand(false);
            EnsureSecurityCatalogStateResult verification;
            try {
                verification = commandBus.dispatch(verifyCommand);
            } catch (RuntimeException verificationFailure) {
                exception.addSuppressed(verificationFailure);
                throw exception;
            }
            if (!verification.initialized()) {
                throw exception;
            }
        }
        return invocation.proceed();
    }
}
