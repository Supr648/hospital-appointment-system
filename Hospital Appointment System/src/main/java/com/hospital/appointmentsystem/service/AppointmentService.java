package com.hospital.appointmentsystem.service;

import com.hospital.appointmentsystem.entity.Appointment;
import com.hospital.appointmentsystem.entity.Doctor;
import com.hospital.appointmentsystem.entity.Patient;
import com.hospital.appointmentsystem.repository.AppointmentRepository;
import com.hospital.appointmentsystem.repository.DoctorRepository;
import com.hospital.appointmentsystem.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              DoctorRepository doctorRepository,
                              PatientRepository patientRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    public List<Appointment> findAll() {
        return appointmentRepository.findAllByOrderByAppointmentTimeDesc();
    }

    public Optional<Appointment> findById(Long id) {
        return appointmentRepository.findById(id);
    }

    public long count() {
        return appointmentRepository.count();
    }

    public long countByStatus(String status) {
        return appointmentRepository.countByStatus(status);
    }

    public List<Appointment> findByStatus(String status) {
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            return findAll();
        }
        return appointmentRepository.findByStatus(status);
    }

    public List<Appointment> search(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        return appointmentRepository.search(query.trim());
    }

    public List<Appointment> findToday() {
        LocalDate today = LocalDate.now();
        return appointmentRepository.findBetween(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
    }

    public List<Appointment> findUpcoming(int limit) {
        List<Appointment> scheduled = appointmentRepository.findByStatusOrderByAppointmentTimeAsc("SCHEDULED");
        return scheduled.stream().limit(limit).toList();
    }

    public Appointment book(Long patientId, Long doctorId, LocalDateTime time, String reason, String notes) {
        if (time.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment time cannot be in the past.");
        }
        if (appointmentRepository.existsByDoctorIdAndAppointmentTime(doctorId, time)) {
            throw new IllegalArgumentException("This doctor already has an appointment at that time. Please choose another slot.");
        }

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        if (Boolean.FALSE.equals(doctor.getAvailable())) {
            throw new IllegalArgumentException("This doctor is currently not available for appointments.");
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentTime(time);
        appointment.setStatus("SCHEDULED");
        appointment.setReason(reason);
        appointment.setNotes(notes);
        appointment.setCreatedAt(LocalDateTime.now());
        return appointmentRepository.save(appointment);
    }

    public Appointment updateStatus(Long id, String status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        appointment.setStatus(status);
        return appointmentRepository.save(appointment);
    }

    public void deleteById(Long id) {
        appointmentRepository.deleteById(id);
    }

    public List<Appointment> findByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorIdOrderByAppointmentTimeDesc(doctorId);
    }

    public List<Appointment> findByPatient(Long patientId) {
        return appointmentRepository.findByPatientIdOrderByAppointmentTimeDesc(patientId);
    }
}
