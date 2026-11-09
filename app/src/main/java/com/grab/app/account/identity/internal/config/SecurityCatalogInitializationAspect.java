package com.grab.app.account.identity.internal.config;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.exception.MessageSource;
import com.identity.adapter.persistence.exception.IdentityInfraError;
import com.identity.adapter.persistence.exception.IdentityInfraException;
import com.identity.application.model.write.EnsureSecurityCatalogStateCommand;
import com.identity.application.model.write.EnsureSecurityCatalogStateResult;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class SecurityCatalogInitializationAspect {

    private final ObjectProvider<CommandBus> commandBusProvider;

    public SecurityCatalogInitializationAspect(
            @Qualifier("accountCommandBus") ObjectProvider<CommandBus> commandBusProvider
    ) {
        this.commandBusProvider = commandBusProvider;
    }

    @Around("@annotation(com.grab.app.account.identity.internal.config.RequiresSecurityCatalog)")
    public Object ensureCatalogExists(ProceedingJoinPoint invocation) throws Throwable {
        CommandBus commandBus = commandBusProvider.getObject();
        EnsureSecurityCatalogStateCommand ensureCommand = new EnsureSecurityCatalogStateCommand(true);
        try {
            commandBus.dispatch(ensureCommand);
        } catch (IdentityInfraException exception) {
            MessageSource messageSource = exception.getMessageSource();
            boolean isRace = messageSource instanceof IdentityInfraError.SecurityCatalogInitializationRace;
            if (!isRace) {
                throw exception;
            }
            EnsureSecurityCatalogStateCommand verifyCommand = new EnsureSecurityCatalogStateCommand(false);
            EnsureSecurityCatalogStateResult verification;
            try {
                verification = commandBus.dispatch(verifyCommand);
            } catch (RuntimeException verificationFailure) {
                exception.addSuppressed(verificationFailure);
                throw exception;
            }
            boolean isInitialized = verification.initialized();
            if (!isInitialized) {
                throw exception;
            }
        }
        return invocation.proceed();
    }
}
