package com.hospital.appointmentsystem.controller;

import com.hospital.appointmentsystem.entity.Appointment;
import com.hospital.appointmentsystem.service.AppointmentActivityService;
import com.hospital.appointmentsystem.service.AppointmentService;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentActivityService activityService;

    public AppointmentController(AppointmentService appointmentService,
                                 AppointmentActivityService activityService) {
        this.appointmentService = appointmentService;
        this.activityService = activityService;
    }

    @GetMapping
    public List<Appointment> getAll(@RequestParam(required = false) String q,
                                    @RequestParam(required = false) String status) {
        return appointmentService.searchAndFilter(q, status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getById(@PathVariable Long id) {
        return appointmentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            Appointment appointment = appointmentService.updateStatus(id, body.get("status"));
            try {
                activityService.record(id, "Appointment " + appointment.getStatus().toLowerCase(Locale.ROOT));
            } catch (DataAccessException exception) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Appointment status was updated in MySQL, but MongoDB activity could not be recorded.");
            }
            return ResponseEntity.ok(appointment);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    @PostMapping("/{id}/reschedule")
    public ResponseEntity<?> reschedule(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String requestedTime = body.get("appointmentTime");
            if (requestedTime == null || requestedTime.isBlank()) {
                return ResponseEntity.badRequest().body("Appointment time is required.");
            }
            Appointment appointment = appointmentService.reschedule(id, LocalDateTime.parse(requestedTime));
            try {
                activityService.record(id, "Appointment rescheduled");
            } catch (DataAccessException exception) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Appointment was rescheduled in MySQL, but MongoDB activity could not be recorded.");
            }
            return ResponseEntity.ok(appointment);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        } catch (DateTimeParseException exception) {
            return ResponseEntity.badRequest().body("Enter a valid appointment date and time.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            appointmentService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
            appointmentService.deleteById(id);
            try {
                activityService.record(id, "Appointment record deleted");
            } catch (DataAccessException exception) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Appointment was deleted from MySQL, but MongoDB activity could not be recorded.");
            }
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.notFound().build();
        }
    }
}
