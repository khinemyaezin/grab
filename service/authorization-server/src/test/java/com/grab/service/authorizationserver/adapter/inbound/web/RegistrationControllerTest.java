package com.grab.service.authorizationserver.adapter.inbound.web;

import com.grab.service.authorizationserver.core.user.IamUser;
import com.grab.service.authorizationserver.core.user.IamUserRepository;
import com.grab.service.authorizationserver.port.outbound.ScimClientPort;
import com.grab.service.contract.message.account.scim.ScimEmail;
import com.grab.service.contract.message.account.scim.ScimName;
import com.grab.service.contract.message.account.scim.ScimUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RegistrationControllerTest {
    private IamUserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private ScimClientPort scimClient;
    private RegistrationController controller;

    @BeforeEach
    void setUp() {
        userRepository = mock(IamUserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        scimClient = mock(ScimClientPort.class);
        controller = new RegistrationController(userRepository, passwordEncoder, scimClient);
    }

    @Test
    void showsRegisterForm() {
        assertThat(controller.showRegisterForm()).isEqualTo("register");
    }

    @Test
    void rejectsInvalidEmail() {
        Model model = new ConcurrentModel();
        String view = controller.handleRegister("invalid-email", "user1", "ValidPass123!", "Jane", "Doe", model);

        assertThat(view).isEqualTo("register");
        assertThat(model.getAttribute("error")).isEqualTo("Please provide a valid email address.");
        verifyNoInteractions(userRepository, passwordEncoder, scimClient);
    }

    @Test
    void rejectsShortPassword() {
        Model model = new ConcurrentModel();
        String view = controller.handleRegister("jane@example.com", "user1", "short", "Jane", "Doe", model);

        assertThat(view).isEqualTo("register");
        assertThat(model.getAttribute("error")).isEqualTo("Password must be at least 8 characters long.");
        verifyNoInteractions(userRepository, passwordEncoder, scimClient);
    }

    @Test
    void rejectsExistingEmail() {
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        Model model = new ConcurrentModel();
        String view = controller.handleRegister("jane@example.com", "user1", "ValidPass123!", "Jane", "Doe", model);

        assertThat(view).isEqualTo("register");
        assertThat(model.getAttribute("error")).isEqualTo("An account with this email address already exists.");
        verifyNoInteractions(passwordEncoder, scimClient);
    }

    @Test
    void rejectsExistingUsername() {
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("user1")).thenReturn(true);

        Model model = new ConcurrentModel();
        String view = controller.handleRegister("jane@example.com", "user1", "ValidPass123!", "Jane", "Doe", model);

        assertThat(view).isEqualTo("register");
        assertThat(model.getAttribute("error")).isEqualTo("An account with this username already exists.");
        verifyNoInteractions(passwordEncoder, scimClient);
    }

    @Test
    void successfullyRegistersUserAndProvisionsViaScim() {
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("user1")).thenReturn(false);
        when(passwordEncoder.encode("ValidPass123!")).thenReturn("bcrypt-hash");

        ScimUser scimResponse = new ScimUser(
                List.of(ScimUser.SCHEMA_URI),
                "plat-user-uuid",
                "iam-user-id",
                "jane@example.com",
                new ScimName("Jane", "Doe"),
                List.of(new ScimEmail("jane@example.com", "work", true)),
                true,
                null
        );
        when(scimClient.provisionUser(any(ScimUser.class))).thenReturn(scimResponse);

        Model model = new ConcurrentModel();
        String view = controller.handleRegister("jane@example.com", "user1", "ValidPass123!", "Jane", "Doe", model);

        assertThat(view).isEqualTo("redirect:/login?registered");

        ArgumentCaptor<IamUser> userCaptor = ArgumentCaptor.forClass(IamUser.class);
        verify(userRepository).save(userCaptor.capture());
        IamUser saved = userCaptor.getValue();
        assertThat(saved.email()).isEqualTo("jane@example.com");
        assertThat(saved.username()).isEqualTo("user1");
        assertThat(saved.passwordHash()).isEqualTo("bcrypt-hash");
        assertThat(saved.firstName()).isEqualTo("Jane");
        assertThat(saved.lastName()).isEqualTo("Doe");
        assertThat(saved.enabled()).isTrue();

        verify(scimClient).provisionUser(any(ScimUser.class));
        verify(userRepository).updatePlatformUserId(saved.id(), "plat-user-uuid");
    }
}
