package com.grab.service.authorizationserver.adapter.inbound.web;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.service.authorizationserver.core.user.IamUser;
import com.grab.service.authorizationserver.core.user.IamUserRepository;
import com.grab.service.authorizationserver.port.outbound.ScimClientPort;
import com.grab.service.contract.message.account.scim.ScimEmail;
import com.grab.service.contract.message.account.scim.ScimName;
import com.grab.service.contract.message.account.scim.ScimUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Controller
public class RegistrationController {
    private static final Logger LOG = Loggers.getLogger(RegistrationController.class);

    private final IamUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ScimClientPort scimClient;

    public RegistrationController(IamUserRepository userRepository,
                                  PasswordEncoder passwordEncoder,
                                  ScimClientPort scimClient) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.scimClient = scimClient;
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(@RequestParam("email") String email,
                                 @RequestParam(value = "username", required = false) String username,
                                 @RequestParam("password") String password,
                                 @RequestParam(value = "firstName", required = false) String firstName,
                                 @RequestParam(value = "lastName", required = false) String lastName,
                                 Model model) {
        String trimmedEmail = email != null ? email.trim().toLowerCase() : "";
        String trimmedUsername = (username != null && !username.isBlank()) ? username.trim() : trimmedEmail;

        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            model.addAttribute("error", "Please provide a valid email address.");
            return "register";
        }

        if (password == null || password.length() < 8) {
            model.addAttribute("error", "Password must be at least 8 characters long.");
            return "register";
        }

        if (userRepository.existsByEmail(trimmedEmail)) {
            model.addAttribute("error", "An account with this email address already exists.");
            return "register";
        }

        if (userRepository.existsByUsername(trimmedUsername)) {
            model.addAttribute("error", "An account with this username already exists.");
            return "register";
        }

        String iamUserId = UUID.randomUUID().toString();
        String passwordHash = passwordEncoder.encode(password);

        IamUser newUser = new IamUser(
                iamUserId,
                trimmedUsername,
                trimmedEmail,
                passwordHash,
                firstName,
                lastName,
                true,
                null,
                1L,
                Set.of("ROLE_USER"),
                Instant.now(),
                Instant.now()
        );

        userRepository.save(newUser);

        try {
            ScimUser scimRequest = new ScimUser(
                    iamUserId,
                    trimmedEmail,
                    new ScimName(firstName, lastName),
                    List.of(new ScimEmail(trimmedEmail, "work", true)),
                    true
            );
            ScimUser scimResponse = scimClient.provisionUser(scimRequest);
            if (scimResponse != null && scimResponse.id() != null) {
                userRepository.updatePlatformUserId(iamUserId, scimResponse.id());
                LOG.info("Provisioned user {} via SCIM with platform ID {}", iamUserId, scimResponse.id());
            }
        } catch (Exception e) {
            LOG.warn("Failed to provision user {} to account-service via SCIM during registration: {}", iamUserId, e.getMessage());
        }

        return "redirect:/login?registered";
    }
}
