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
import java.security.Principal;
import com.hospital.appointmentsystem.service.WorkspaceAccessService;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentActivityService activityService;
    private final WorkspaceAccessService workspaceAccess;

    public AppointmentController(AppointmentRepository appointmentRepository,
                                 DoctorRepository doctorRepository,
                                 PatientRepository patientRepository,
                                 AppointmentActivityService activityService,
                                 WorkspaceAccessService workspaceAccess) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.activityService = activityService;
        this.workspaceAccess = workspaceAccess;
    }

    @GetMapping
    public List<Appointment> getAllAppointments(Principal principal) {
        return appointmentRepository.findAllByWorkspaceId(workspaceAccess.workspaceId(principal.getName()));
    }

    @PostMapping
    public ResponseEntity<?> addAppointment(@Valid @RequestBody Appointment appointment, Principal principal) {
        Long workspaceId = workspaceAccess.workspaceId(principal.getName());
        appointment.setId(null);
        Long patientId = appointment.getPatient().getId();
        Long doctorId = appointment.getDoctor().getId();
        if (patientId == null || doctorId == null) {
            return ResponseEntity.badRequest().body("Existing patient and doctor IDs are required.");
        }

        Patient patient = patientRepository.findByIdAndWorkspaceId(patientId, workspaceId).orElse(null);
        if (patient == null) {
            return ResponseEntity.badRequest().body("Patient not found.");
        }

        Doctor doctor = doctorRepository.findByIdAndWorkspaceId(doctorId, workspaceId).orElse(null);
        if (doctor == null) {
            return ResponseEntity.badRequest().body("Doctor not found.");
        }

        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setWorkspaceId(workspaceId);
        Appointment savedAppointment = appointmentRepository.save(appointment);
        try {
            activityService.recordAppointmentBooked(savedAppointment.getId(), workspaceId);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAppointment);
        } catch (DataAccessException exception) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Appointment #" + savedAppointment.getId()
                            + " was saved, but its activity record could not be saved. "
                            + "Do not submit the appointment again.");
        }
    }
}