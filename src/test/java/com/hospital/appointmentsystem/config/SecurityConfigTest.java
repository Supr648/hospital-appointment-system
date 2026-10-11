package com.hospital.appointmentsystem.config;

import com.hospital.appointmentsystem.controller.AppointmentController;
import com.hospital.appointmentsystem.controller.WebController;
import com.hospital.appointmentsystem.repository.AppointmentRepository;
import com.hospital.appointmentsystem.repository.DoctorRepository;
import com.hospital.appointmentsystem.repository.PatientRepository;
import com.hospital.appointmentsystem.service.AppointmentActivityService;
import com.hospital.appointmentsystem.service.WorkspaceAccessService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.hospital.appointmentsystem.entity.AppUser;
import java.util.Optional;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = {WebController.class, AppointmentController.class})
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.admin.username=deploy-admin",
        "app.admin.password=test-password-with-at-least-16-chars"
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private DoctorRepository doctorRepository;

    @MockBean
    private PatientRepository patientRepository;

    @MockBean
    private AppointmentRepository appointmentRepository;

    @MockBean
    private AppointmentActivityService activityService;

    @MockBean
    private WorkspaceAccessService workspaceAccess;

    @MockBean
    private com.hospital.appointmentsystem.repository.AppUserRepository appUserRepository;

    @BeforeEach
    void configureWorkspace() {
        when(workspaceAccess.workspaceId(anyString())).thenReturn(1L);
        when(appUserRepository.findByUsernameIgnoreCase("deploy-admin"))
                .thenReturn(Optional.of(new AppUser("deploy-admin",
                        passwordEncoder.encode("test-password-with-at-least-16-chars"),
                        "Test hospital", true)));
    }

    @Test
    void publicWelcomeAndGuestDemoDoNotRequireSignIn() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Create an account")));

        mockMvc.perform(get("/guest-demo"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Fictional sample data")));

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void signInPageIsPublicAndProvidesGuestOption() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Guest demo")));
    }

    @Test
    void apiRequiresBasicAuthentication() throws Exception {
        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isUnauthorized());

        when(appointmentRepository.findAllByWorkspaceId(1L)).thenReturn(List.of());
        mockMvc.perform(get("/api/appointments")
                        .with(httpBasic("deploy-admin", "test-password-with-at-least-16-chars")))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void browserLoginAllowsAccessToProtectedPages() throws Exception {
        MvcResult login = mockMvc.perform(formLogin()
                        .user("deploy-admin")
                        .password("test-password-with-at-least-16-chars"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        mockMvc.perform(get("/").session((org.springframework.mock.web.MockHttpSession)
                        login.getRequest().getSession(false)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/dashboard").session((org.springframework.mock.web.MockHttpSession)
                        login.getRequest().getSession(false)))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }
}
