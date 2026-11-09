package com.grab.app.account.identity.internal.config;

import com.grab.framework.cqrs.command.CommandBus;
import com.identity.adapter.persistence.exception.IdentityInfraError;
import com.identity.adapter.persistence.exception.IdentityInfraException;
import com.identity.application.model.write.EnsureSecurityCatalogStateCommand;
import com.identity.application.model.write.EnsureSecurityCatalogStateResult;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityCatalogInitializationAspectTest {

    @Mock
    private CommandBus commandBus;

    @Mock
    private ObjectProvider<CommandBus> commandBusProvider;

    @Mock
    private ProceedingJoinPoint joinPoint;

    private SecurityCatalogInitializationAspect aspect;

    @BeforeEach
    void setUp() {
        when(commandBusProvider.getObject()).thenReturn(commandBus);
        aspect = new SecurityCatalogInitializationAspect(commandBusProvider);
    }

    @Test
    void ensureCatalogExists_whenInitializesSuccessfully_shouldProceed() throws Throwable {
        EnsureSecurityCatalogStateResult result = new EnsureSecurityCatalogStateResult(true);
        when(commandBus.dispatch(any(EnsureSecurityCatalogStateCommand.class))).thenReturn(result);
        Object expectedProceedResult = "proceedResult";
        when(joinPoint.proceed()).thenReturn(expectedProceedResult);

        Object actualResult = aspect.ensureCatalogExists(joinPoint);

        assertEquals(expectedProceedResult, actualResult);
        verify(joinPoint).proceed();
    }

    @Test
    void ensureCatalogExists_whenRaceOccursAndVerified_shouldProceed() throws Throwable {
        IdentityInfraError.SecurityCatalogInitializationRace raceError =
                new IdentityInfraError.SecurityCatalogInitializationRace("security_catalog_state_pkey");
        IdentityInfraException raceException = new IdentityInfraException(raceError, "Race detected", null);
        EnsureSecurityCatalogStateResult verifiedResult = new EnsureSecurityCatalogStateResult(true);

        when(commandBus.dispatch(any(EnsureSecurityCatalogStateCommand.class)))
                .thenThrow(raceException)
                .thenReturn(verifiedResult);

        Object expectedProceedResult = "proceedResult";
        when(joinPoint.proceed()).thenReturn(expectedProceedResult);

        Object actualResult = aspect.ensureCatalogExists(joinPoint);

        assertEquals(expectedProceedResult, actualResult);
        verify(joinPoint).proceed();
    }

    @Test
    void ensureCatalogExists_whenNonRaceExceptionOccurs_shouldPropagate() {
        IdentityInfraError.SecurityCatalogStateMissing missingError =
                new IdentityInfraError.SecurityCatalogStateMissing();
        IdentityInfraException missingException = new IdentityInfraException(missingError, "Missing state", null);

        when(commandBus.dispatch(any(EnsureSecurityCatalogStateCommand.class)))
                .thenThrow(missingException);

        assertThrows(IdentityInfraException.class, () -> aspect.ensureCatalogExists(joinPoint));
    }
}

