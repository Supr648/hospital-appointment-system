package com.hospital.appointmentsystem;

import com.hospital.appointmentsystem.entity.Doctor;
import com.hospital.appointmentsystem.entity.Patient;
import com.hospital.appointmentsystem.repository.AppUserRepository;
import com.hospital.appointmentsystem.repository.AppointmentRepository;
import com.hospital.appointmentsystem.repository.DoctorRepository;
import com.hospital.appointmentsystem.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:workspace-integration;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.admin.username=integration-admin",
        "app.admin.password=integration-admin-password-123"
})
@AutoConfigureMockMvc
class RegistrationWorkspaceIntegrationTest {

    private static final String PASSWORD = "workspace-integration-password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Test
    void registersAccountAndSignsIntoItsWorkspace() throws Exception {
        String email = uniqueEmail();
        register(email);
        MockHttpSession session = signIn(email);

        mockMvc.perform(get("/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        mockMvc.perform(SecurityMockMvcRequestBuilders.formLogin()
                        .user(email)
                        .password("wrong-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void persistsDoctorPatientAndAppointmentForSignedInAccount() throws Exception {
        String email = uniqueEmail();
        register(email);
        MockHttpSession session = signIn(email);
        String suffix = UUID.randomUUID().toString();

        mockMvc.perform(post("/doctors-page/add").session(session).with(csrf())
                        .param("name", "Dr. " + suffix)
                        .param("specialization", "Cardiology")
                        .param("email", "doctor-" + suffix + "@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctors-page"));

        mockMvc.perform(post("/patients-page/add").session(session).with(csrf())
                        .param("name", "Patient " + suffix)
                        .param("email", "patient-" + suffix + "@example.com")
                        .param("phone", "+1 555 0100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patients-page"));

        mockMvc.perform(post("/appointments-page/book").session(session).with(csrf())
                        .param("patientId", patientId(email).toString())
                        .param("doctorId", doctorId(email).toString())
                        .param("appointmentTime", LocalDateTime.now().plusDays(3)
                                .withSecond(0).withNano(0)
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/appointments-page"));

        mockMvc.perform(get("/appointments-page").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Patient " + suffix)))
                .andExpect(content().string(containsString("Dr. " + suffix)))
                .andExpect(content().string(containsString("Appointment booked")));

        Long workspaceId = workspaceId(email);
        assertEquals(1, appointmentRepository.findAllByWorkspaceId(workspaceId).size());
    }

    @Test
    void separateAccountsCannotSeeOrBookEachOthersRecords() throws Exception {
        String firstEmail = uniqueEmail();
        register(firstEmail);
        MockHttpSession firstSession = signIn(firstEmail);
        String suffix = UUID.randomUUID().toString();

        mockMvc.perform(post("/doctors-page/add").session(firstSession).with(csrf())
                        .param("name", "Private Doctor " + suffix)
                        .param("specialization", "Neurology")
                        .param("email", "private-doctor-" + suffix + "@example.com"))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(post("/patients-page/add").session(firstSession).with(csrf())
                        .param("name", "Private Patient " + suffix)
                        .param("email", "private-patient-" + suffix + "@example.com")
                        .param("phone", "+1 555 0102"))
                .andExpect(status().is3xxRedirection());

        String secondEmail = uniqueEmail();
        register(secondEmail);
        MockHttpSession secondSession = signIn(secondEmail);

        mockMvc.perform(get("/doctors-page").session(secondSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No doctors have been added yet.")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("private-doctor-" + suffix))));
        mockMvc.perform(get("/patients-page").session(secondSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No patients have been registered yet.")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("private-patient-" + suffix))));

        Patient foreignPatient = patientRepository.findAllByWorkspaceId(workspaceId(firstEmail)).get(0);
        Doctor foreignDoctor = doctorRepository.findAllByWorkspaceId(workspaceId(firstEmail)).get(0);
        String appointmentJson = """
                {"patient":{"id":%d},"doctor":{"id":%d},
                 "appointmentTime":"%s","status":"SCHEDULED"}
                """.formatted(foreignPatient.getId(), foreignDoctor.getId(),
                LocalDateTime.now().plusDays(3).withSecond(0).withNano(0));

        mockMvc.perform(post("/api/appointments")
                        .with(httpBasic(secondEmail, PASSWORD))
                        .contentType("application/json")
                        .content(appointmentJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Patient not found."));

        assertTrue(appointmentRepository.findAllByWorkspaceId(workspaceId(secondEmail)).isEmpty());
        assertEquals(0, appointmentRepository.findAllByWorkspaceId(workspaceId(firstEmail)).size());
    }

    private void register(String email) throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("organizationName", "Integration Clinic")
                        .param("username", email)
                        .param("password", PASSWORD)
                        .param("confirmPassword", PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));
    }

    private MockHttpSession signIn(String email) throws Exception {
        MvcResult result = mockMvc.perform(SecurityMockMvcRequestBuilders.formLogin()
                        .user(email)
                        .password(PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private String uniqueEmail() {
        return UUID.randomUUID() + "@example.com";
    }

    private Long workspaceId(String email) {
        return userRepository.findByUsernameIgnoreCase(email).orElseThrow().getId();
    }

    private Long doctorId(String email) {
        return doctorRepository.findAllByWorkspaceId(workspaceId(email)).get(0).getId();
    }

    private Long patientId(String email) {
        return patientRepository.findAllByWorkspaceId(workspaceId(email)).get(0).getId();
    }
}
