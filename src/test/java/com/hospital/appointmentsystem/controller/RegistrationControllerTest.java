package com.hospital.appointmentsystem.controller;

import com.hospital.appointmentsystem.config.SecurityConfig;
import com.hospital.appointmentsystem.service.RegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(RegistrationController.class)
@Import(SecurityConfig.class)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private com.hospital.appointmentsystem.repository.AppUserRepository appUserRepository;

    @Test
    void registrationFormIsPublic() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Create your workspace")));
    }

    @Test
    void createsAccountThenRedirectsToSignIn() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("organizationName", "Northside Clinic")
                        .param("username", "STAFF@example.com")
                        .param("password", "a-long-password-for-testing")
                        .param("confirmPassword", "a-long-password-for-testing"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        verify(registrationService).register(
                "STAFF@example.com", "a-long-password-for-testing", "Northside Clinic");
    }

    @Test
    void rejectsMismatchedPasswordsWithoutCreatingAccount() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("organizationName", "Northside Clinic")
                        .param("username", "staff@example.com")
                        .param("password", "a-long-password-for-testing")
                        .param("confirmPassword", "a-different-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("passwords do not match")));

        verify(registrationService, never()).register(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void redisplaysValidationErrorsInsteadOfFailingRequest() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("organizationName", "Northside Clinic")
                        .param("username", "not-an-email")
                        .param("password", "short")
                        .param("confirmPassword", "short"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "Please correct the highlighted registration details.")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("must be a well-formed email address")));

        verify(registrationService, never()).register(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }
}
