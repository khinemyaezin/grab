package com.grab.store.identity.internal.exception;

import com.grab.framework.exception.*;
import java.util.Map;

public sealed interface IdentityServiceError extends MessageSource permits IdentityServiceError.EmailExists, IdentityServiceError.InvalidRole, IdentityServiceError.UserNotFound, IdentityServiceError.RoleNotFound, IdentityServiceError.RoleExists {
    record EmailExists(String email) implements IdentityServiceError { public ErrorCategory kind(){return ErrorCategory.CONFLICT;} public String code(){return "idt.service.user.email_already_exists";} public Map<String,Object> args(){return Map.of("email",email);} }
    record InvalidRole(String role) implements IdentityServiceError { public ErrorCategory kind(){return ErrorCategory.BAD_REQUEST;} public String code(){return "idt.service.user.invalid_role";} public Map<String,Object> args(){return Map.of("role",role);} }
    record UserNotFound(String id) implements IdentityServiceError { public ErrorCategory kind(){return ErrorCategory.NOT_FOUND;} public String code(){return "idt.service.user.not_found";} public Map<String,Object> args(){return Map.of("userId",id);} }
    record RoleNotFound(String codeValue) implements IdentityServiceError { public ErrorCategory kind(){return ErrorCategory.NOT_FOUND;} public String code(){return "idt.service.role.not_found";} public Map<String,Object> args(){return Map.of("role",codeValue);} }
    record RoleExists(String codeValue) implements IdentityServiceError { public ErrorCategory kind(){return ErrorCategory.CONFLICT;} public String code(){return "idt.service.role.already_exists";} public Map<String,Object> args(){return Map.of("role",codeValue);} }
}
