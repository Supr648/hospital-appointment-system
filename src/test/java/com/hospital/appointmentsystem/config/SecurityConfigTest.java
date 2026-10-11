package com.hospital.appointmentsystem.config;

import com.hospital.appointmentsystem.controller.AppointmentController;
import com.hospital.appointmentsystem.controller.WebController;
import com.hospital.appointmentsystem.repository.AppointmentRepository;
import com.hospital.appointmentsystem.repository.DoctorRepository;
import com.hospital.appointmentsystem.repository.PatientRepository;
import com.hospital.appointmentsystem.service.AppointmentActivityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {WebController.class, AppointmentController.class})
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.admin.username=deploy-admin",
        "app.admin.password=test-password-with-at-least-16-chars"
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DoctorRepository doctorRepository;

    @MockBean
    private PatientRepository patientRepository;

    @MockBean
    private AppointmentRepository appointmentRepository;

    @MockBean
    private AppointmentActivityService activityService;

    @Test
    void browserPagesRequireSignIn() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void apiRequiresBasicAuthentication() throws Exception {
        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isUnauthorized());

        when(appointmentRepository.findAll()).thenReturn(List.of());
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
    }
}
