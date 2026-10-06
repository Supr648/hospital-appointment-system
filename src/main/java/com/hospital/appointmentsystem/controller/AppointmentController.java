package com.hospital.appointmentsystem.controller;

import com.hospital.appointmentsystem.entity.Appointment;
import com.hospital.appointmentsystem.repository.AppointmentRepository;
import com.hospital.appointmentsystem.service.AppointmentActivityService;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentActivityService activityService;

    public AppointmentController(AppointmentRepository appointmentRepository,
                                 AppointmentActivityService activityService) {
        this.appointmentRepository = appointmentRepository;
        this.activityService = activityService;
    }

    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> addAppointment(@RequestBody Appointment appointment) {
        Appointment savedAppointment = appointmentRepository.save(appointment);
        try {
            activityService.recordAppointmentBooked(savedAppointment.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAppointment);
        } catch (DataAccessException exception) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Appointment #" + savedAppointment.getId()
                            + " was saved in MySQL, but its activity record could not be saved in MongoDB. "
                            + "Check the MongoDB connection; do not submit the appointment again.");
        }
    }
}