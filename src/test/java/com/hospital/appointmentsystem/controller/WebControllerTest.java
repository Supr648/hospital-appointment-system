package com.hospital.appointmentsystem.controller;

import com.hospital.appointmentsystem.entity.Appointment;
import com.hospital.appointmentsystem.entity.Doctor;
import com.hospital.appointmentsystem.entity.Patient;
import com.hospital.appointmentsystem.repository.AppointmentRepository;
import com.hospital.appointmentsystem.repository.DoctorRepository;
import com.hospital.appointmentsystem.repository.PatientRepository;
import com.hospital.appointmentsystem.service.AppointmentActivityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest(WebController.class)
@Import(com.hospital.appointmentsystem.config.SecurityConfig.class)
@WithMockUser(username = "test-admin")
@TestPropertySource(properties = {
        "app.admin.username=test-admin",
        "app.admin.password=test-password-with-at-least-16-chars"
})
class WebControllerTest {

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
    void dashboardPageRenders() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(content().string(containsString("Good care starts with a clear view.")));
    }

    @Test
    void publicWelcomeAndGuestDemoRenderWithoutRealRecords() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(content().string(containsString("Explore the demo")));

        mockMvc.perform(get("/guest-demo"))
                .andExpect(status().isOk())
                .andExpect(view().name("guest-demo"))
                .andExpect(content().string(containsString("Fictional sample data")));

        verify(doctorRepository, never()).findAll();
        verify(patientRepository, never()).findAll();
        verify(appointmentRepository, never()).findAll();
    }

    @Test
    void directoryPagesRenderWhenEmpty() throws Exception {
        mockMvc.perform(get("/doctors-page"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctors"))
                .andExpect(content().string(containsString("No doctors have been added yet.")));

        mockMvc.perform(get("/patients-page"))
                .andExpect(status().isOk())
                .andExpect(view().name("patients"))
                .andExpect(content().string(containsString("No patients have been registered yet.")));
    }

    @Test
    void appointmentsPageRendersWhenDependenciesAreEmpty() throws Exception {
        when(activityService.getRecentActivity()).thenReturn(List.of());

        mockMvc.perform(get("/appointments-page"))
                .andExpect(status().isOk())
                .andExpect(view().name("appointments"))
                .andExpect(content().string(containsString("No appointments match these filters.")));
    }

    @Test
    void doctorFormRejectsDuplicateEmail() throws Exception {
        when(doctorRepository.existsByEmailIgnoreCase("doctor@example.com")).thenReturn(true);

        mockMvc.perform(post("/doctors-page/add").with(user("test-admin")).with(csrf())
                        .param("name", "Dr Example")
                        .param("specialization", "Cardiology")
                        .param("email", "doctor@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctors"))
                .andExpect(model().attribute("error", "A doctor with this email already exists."));

        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    void patientFormRejectsDuplicateEmail() throws Exception {
        when(patientRepository.existsByEmailIgnoreCase("patient@example.com")).thenReturn(true);

        mockMvc.perform(post("/patients-page/add").with(user("test-admin")).with(csrf())
                        .param("name", "Taylor Example")
                        .param("email", "patient@example.com")
                        .param("phone", "555-0100"))
                .andExpect(status().isOk())
                .andExpect(view().name("patients"))
                .andExpect(model().attribute("error", "A patient with this email already exists."));

        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void bookingRejectsMalformedAppointmentTime() throws Exception {
        mockMvc.perform(post("/appointments-page/book").with(user("test-admin")).with(csrf())
                        .param("patientId", "1")
                        .param("doctorId", "2")
                        .param("appointmentTime", "not-a-date"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/appointments-page"))
                .andExpect(flash().attribute("error",
                        "Enter a valid appointment date and time."));

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void bookingRejectsPastAppointmentTime() throws Exception {
        mockMvc.perform(post("/appointments-page/book").with(user("test-admin")).with(csrf())
                        .param("patientId", "1")
                        .param("doctorId", "2")
                        .param("appointmentTime", LocalDateTime.now().minusDays(1).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error",
                        "Appointment time must be in the future."));

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void bookingRejectsUnknownPatient() throws Exception {
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/appointments-page/book").with(user("test-admin")).with(csrf())
                        .param("patientId", "1")
                        .param("doctorId", "2")
                        .param("appointmentTime", LocalDateTime.now().plusDays(1).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "Selected patient was not found."));

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void bookingRejectsUnknownDoctor() throws Exception {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(new Patient()));
        when(doctorRepository.findById(2L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/appointments-page/book").with(user("test-admin")).with(csrf())
                        .param("patientId", "1")
                        .param("doctorId", "2")
                        .param("appointmentTime", LocalDateTime.now().plusDays(1).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "Selected doctor was not found."));

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void bookingSavesAppointmentAndRecordsActivity() throws Exception {
        Patient patient = new Patient();
        patient.setId(1L);
        Doctor doctor = new Doctor();
        doctor.setId(2L);
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(1).withNano(0);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment saved = invocation.getArgument(0);
            saved.setId(17L);
            return saved;
        });

        mockMvc.perform(post("/appointments-page/book").with(user("test-admin")).with(csrf())
                        .param("patientId", "1")
                        .param("doctorId", "2")
                        .param("appointmentTime", appointmentTime.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/appointments-page"));

        verify(appointmentRepository).save(argThat(appointment ->
                appointment.getPatient() == patient
                        && appointment.getDoctor() == doctor
                        && appointmentTime.equals(appointment.getAppointmentTime())
                        && "SCHEDULED".equals(appointment.getStatus())));
        verify(activityService).recordAppointmentBooked(17L);
    }
}
