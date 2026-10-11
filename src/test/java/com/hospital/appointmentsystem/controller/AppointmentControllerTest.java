package com.hospital.appointmentsystem.controller;

import com.hospital.appointmentsystem.entity.Appointment;
import com.hospital.appointmentsystem.entity.Doctor;
import com.hospital.appointmentsystem.entity.Patient;
import com.hospital.appointmentsystem.repository.AppointmentRepository;
import com.hospital.appointmentsystem.repository.AppUserRepository;
import com.hospital.appointmentsystem.repository.DoctorRepository;
import com.hospital.appointmentsystem.repository.PatientRepository;
import com.hospital.appointmentsystem.service.AppointmentActivityService;
import com.hospital.appointmentsystem.service.WorkspaceAccessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
@Import(com.hospital.appointmentsystem.config.SecurityConfig.class)
@WithMockUser(username = "test-admin")
@TestPropertySource(properties = {
        "app.admin.username=test-admin",
        "app.admin.password=test-password-with-at-least-16-chars"
})
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentRepository appointmentRepository;

    @MockBean
    private DoctorRepository doctorRepository;

    @MockBean
    private PatientRepository patientRepository;

    @MockBean
    private AppointmentActivityService activityService;

    @MockBean
    private WorkspaceAccessService workspaceAccess;

    @MockBean
    private AppUserRepository appUserRepository;

    @BeforeEach
    void configureWorkspace() {
        when(workspaceAccess.workspaceId(anyString())).thenReturn(1L);
    }

    @Test
    void bookingRejectsMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/api/appointments").with(user("test-admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SCHEDULED\"}"))
                .andExpect(status().isBadRequest());

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void bookingRejectsPatientThatDoesNotExist() throws Exception {
        when(patientRepository.findByIdAndWorkspaceId(1L, 1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/appointments").with(user("test-admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validAppointmentJson()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Patient not found."));

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void bookingCannotUsePatientFromAnotherWorkspace() throws Exception {
        when(patientRepository.findByIdAndWorkspaceId(1L, 1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/appointments").with(user("test-admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validAppointmentJson()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Patient not found."));

        verify(patientRepository).findByIdAndWorkspaceId(1L, 1L);
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void bookingRejectsDoctorThatDoesNotExist() throws Exception {
        when(patientRepository.findByIdAndWorkspaceId(1L, 1L)).thenReturn(Optional.of(new Patient()));
        when(doctorRepository.findByIdAndWorkspaceId(2L, 1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/appointments").with(user("test-admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validAppointmentJson()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Doctor not found."));

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void bookingRejectsAppointmentInThePast() throws Exception {
        mockMvc.perform(post("/api/appointments").with(user("test-admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"patient":{"id":1},"doctor":{"id":2},
                                 "appointmentTime":"2020-01-01T10:00:00","status":"SCHEDULED"}
                                """))
                .andExpect(status().isBadRequest());

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void bookingUsesExistingPatientAndDoctor() throws Exception {
        Patient patient = new Patient();
        patient.setId(1L);
        Doctor doctor = new Doctor();
        doctor.setId(2L);
        when(patientRepository.findByIdAndWorkspaceId(1L, 1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdAndWorkspaceId(2L, 1L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment saved = invocation.getArgument(0);
            saved.setId(31L);
            return saved;
        });

        mockMvc.perform(post("/api/appointments").with(user("test-admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validAppointmentJson()))
                .andExpect(status().isCreated());

        verify(appointmentRepository).save(any(Appointment.class));
        verify(activityService).recordAppointmentBooked(31L, 1L);
    }

    private String validAppointmentJson() {
        return """
                {"patient":{"id":1},"doctor":{"id":2},
                 "appointmentTime":"2099-01-01T10:00:00","status":"SCHEDULED"}
                """;
    }
}
