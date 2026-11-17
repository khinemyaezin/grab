package com.grab.service.account.identity.internal.adapter.inbound.message;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.service.account.identity.internal.adapter.inbound.message.mapper.RegisterUserPortMapper;
import com.grab.service.contract.message.account.identity.SellerRegistrationPort;
import com.grab.service.contract.message.account.identity.SellerRegistrationPort.RegisterUserRequest;
import com.grab.service.contract.message.account.identity.SellerRegistrationPort.RegistrationResult;
import com.grab.service.contract.message.account.identity.SellerRegistrationPort.RegistrationStatus;
import com.identity.application.exception.IdentityServiceException;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.application.model.write.RegisterCommand;
import com.identity.application.model.write.UserProfileResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class SellerRegistrationPortAdapter implements SellerRegistrationPort {
    private final CommandBus commands;
    private final RegisterUserPortMapper mapper;

    public SellerRegistrationPortAdapter(@Qualifier("accountCommandBus") CommandBus commands, RegisterUserPortMapper mapper) {
        this.commands = commands;
        this.mapper = mapper;
    }

    @Override
    public RegistrationResult registerUser(RegisterUserRequest request) {
        if (request.password() == null || request.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            return new RegistrationResult(RegistrationStatus.INVALID_INPUT, null, null, null, null);
        }
        try {
            RegisterCommand command = mapper.toCommand(request);
            UserProfileResult result = commands.dispatch(command);
            return new RegistrationResult(RegistrationStatus.CREATED, result.id(), result.email(), result.status(), result.createdAt());
        } catch (IdentityServiceException exception) {
            if ("idt.service.user.email_already_exists".equals(exception.getMessageSource().code())) {
                return new RegistrationResult(RegistrationStatus.EMAIL_EXISTS, null, null, null, null);
            }
            throw exception;
        } catch (IdentityDomainValidationException exception) {
            return new RegistrationResult(RegistrationStatus.INVALID_INPUT, null, null, null, null);
        } catch (RuntimeException exception) {
            if (isUniqueConstraintViolation(exception)) {
                return new RegistrationResult(RegistrationStatus.EMAIL_EXISTS, null, null, null, null);
            }
            throw exception;
        }
    }

    private boolean isUniqueConstraintViolation(Throwable failure) {
        Throwable cause = failure;
        while (cause != null && cause.getCause() != cause) {
            if (cause instanceof SQLException sqlException && "23505".equals(sqlException.getSQLState())) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
