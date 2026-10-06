package com.hospital.appointmentsystem.service;

import com.hospital.appointmentsystem.entity.Appointment;
import com.hospital.appointmentsystem.entity.Doctor;
import com.hospital.appointmentsystem.repository.AppointmentRepository;
import com.hospital.appointmentsystem.repository.DoctorRepository;
import com.hospital.appointmentsystem.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    void searchAndFilterCombinesQueryAndStatus() {
        appointmentService.searchAndFilter("cardio", "completed");

        verify(appointmentRepository).searchAndFilter("cardio", "COMPLETED");
    }

    @Test
    void searchAndFilterNormalizesUnknownStatusToAll() {
        appointmentService.searchAndFilter(null, "not-a-status");

        verify(appointmentRepository).searchAndFilter(null, "ALL");
    }

    @Test
    void updateStatusRejectsUnknownStatus() {
        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.updateStatus(42L, "IN_PROGRESS"));

        verify(appointmentRepository, never()).findById(42L);
    }

    @Test
    void rescheduleRejectsAConflictWithAnotherScheduledVisit() {
        Appointment appointment = appointmentWithDoctor(42L, 7L);
        LocalDateTime requestedTime = LocalDateTime.now().plusDays(1);
        when(appointmentRepository.findById(42L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.existsByDoctorIdAndAppointmentTimeAndStatusAndIdNot(
                7L, requestedTime, "SCHEDULED", 42L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.reschedule(42L, requestedTime));

        verify(appointmentRepository, never()).save(appointment);
    }

    @Test
    void rescheduleUpdatesTimeAndReturnsAppointmentToScheduled() {
        Appointment appointment = appointmentWithDoctor(42L, 7L);
        LocalDateTime requestedTime = LocalDateTime.now().plusDays(1);
        when(appointmentRepository.findById(42L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.existsByDoctorIdAndAppointmentTimeAndStatusAndIdNot(
                7L, requestedTime, "SCHEDULED", 42L)).thenReturn(false);
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        Appointment updated = appointmentService.reschedule(42L, requestedTime);

        assertEquals(requestedTime, updated.getAppointmentTime());
        assertEquals("SCHEDULED", updated.getStatus());
        verify(appointmentRepository).save(appointment);
    }

    private Appointment appointmentWithDoctor(Long appointmentId, Long doctorId) {
        Doctor doctor = new Doctor();
        doctor.setId(doctorId);

        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setDoctor(doctor);
        appointment.setStatus("SCHEDULED");
        return appointment;
    }
}
