package com.hospital.appointmentsystem.controller;

import com.hospital.appointmentsystem.entity.Appointment;
import com.hospital.appointmentsystem.entity.Doctor;
import com.hospital.appointmentsystem.entity.Patient;
import com.hospital.appointmentsystem.repository.AppointmentRepository;
import com.hospital.appointmentsystem.repository.DoctorRepository;
import com.hospital.appointmentsystem.repository.PatientRepository;
import com.hospital.appointmentsystem.service.AppointmentActivityService;
import jakarta.validation.Valid;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentActivityService activityService;

    public AppointmentController(AppointmentRepository appointmentRepository,
                                 DoctorRepository doctorRepository,
                                 PatientRepository patientRepository,
                                 AppointmentActivityService activityService) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.activityService = activityService;
    }

    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> addAppointment(@Valid @RequestBody Appointment appointment) {
        Long patientId = appointment.getPatient().getId();
        Long doctorId = appointment.getDoctor().getId();
        if (patientId == null || doctorId == null) {
            return ResponseEntity.badRequest().body("Existing patient and doctor IDs are required.");
        }

        Patient patient = patientRepository.findById(patientId).orElse(null);
        if (patient == null) {
            return ResponseEntity.badRequest().body("Patient not found.");
        }

        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) {
            return ResponseEntity.badRequest().body("Doctor not found.");
        }

        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        Appointment savedAppointment = appointmentRepository.save(appointment);
        try {
            activityService.recordAppointmentBooked(savedAppointment.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAppointment);
        } catch (DataAccessException exception) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Appointment #" + savedAppointment.getId()
                            + " was saved, but its activity record could not be saved. "
                            + "Do not submit the appointment again.");
        }
    }
}