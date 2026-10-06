package com.hospital.appointmentsystem.controller;

import com.hospital.appointmentsystem.entity.Appointment;
import com.hospital.appointmentsystem.entity.Doctor;
import com.hospital.appointmentsystem.entity.Patient;
import com.hospital.appointmentsystem.service.AppointmentActivityService;
import com.hospital.appointmentsystem.repository.AppointmentRepository;
import com.hospital.appointmentsystem.repository.DoctorRepository;
import com.hospital.appointmentsystem.repository.PatientRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.time.LocalDateTime;

@Controller
public class WebController {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentActivityService activityService;

    public WebController(DoctorRepository doctorRepository,
                          PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          AppointmentActivityService activityService) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.activityService = activityService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("doctorCount", doctorRepository.count());
        model.addAttribute("patientCount", patientRepository.count());
        model.addAttribute("appointmentCount", appointmentRepository.count());
        return "index";
    }

    @GetMapping("/doctors-page")
    public String doctorsPage(Model model) {
        model.addAttribute("doctors", doctorRepository.findAll());
        return "doctors";
    }

    @PostMapping("/doctors-page/add")
    public String addDoctor(@RequestParam String name,
                             @RequestParam String specialization,
                             @RequestParam String email,
                             Model model) {
        if (doctorRepository.findAll().stream().anyMatch(d -> d.getEmail().equalsIgnoreCase(email))) {
            model.addAttribute("doctors", doctorRepository.findAll());
            model.addAttribute("error", "A doctor with this email already exists.");
            return "doctors";
        }
        Doctor doctor = new Doctor();
        doctor.setName(name);
        doctor.setSpecialization(specialization);
        doctor.setEmail(email);
        doctorRepository.save(doctor);
        return "redirect:/doctors-page";
    }

    @GetMapping("/patients-page")
    public String patientsPage(Model model) {
        model.addAttribute("patients", patientRepository.findAll());
        return "patients";
    }

    @PostMapping("/patients-page/add")
    public String addPatient(@RequestParam String name,
                              @RequestParam String email,
                              @RequestParam String phone,
                              Model model) {
        if (patientRepository.findAll().stream().anyMatch(p -> p.getEmail().equalsIgnoreCase(email))) {
            model.addAttribute("patients", patientRepository.findAll());
            model.addAttribute("error", "A patient with this email already exists.");
            return "patients";
        }
        Patient patient = new Patient();
        patient.setName(name);
        patient.setEmail(email);
        patient.setPhone(phone);
        patientRepository.save(patient);
        return "redirect:/patients-page";
    }

    @GetMapping("/appointments-page")
    public String appointmentsPage(@RequestParam(defaultValue = "") String search,
                                   @RequestParam(defaultValue = "ALL") String status,
                                   Model model) {
        String normalizedSearch = search.trim().toLowerCase(Locale.ROOT);
        String requestedStatus = status.trim().toUpperCase(Locale.ROOT);
        String normalizedStatus = List.of("ALL", "SCHEDULED", "COMPLETED", "CANCELLED")
                .contains(requestedStatus) ? requestedStatus : "ALL";

        List<Appointment> appointments = appointmentRepository.findAll().stream()
                .filter(appointment -> normalizedStatus.equals("ALL")
                        || normalizedStatus.equalsIgnoreCase(appointment.getStatus()))
                .filter(appointment -> normalizedSearch.isEmpty()
                        || appointment.getPatient().getName().toLowerCase(Locale.ROOT).contains(normalizedSearch)
                        || appointment.getDoctor().getName().toLowerCase(Locale.ROOT).contains(normalizedSearch)
                        || appointment.getDoctor().getSpecialization().toLowerCase(Locale.ROOT).contains(normalizedSearch))
                .sorted(Comparator.comparing(Appointment::getAppointmentTime))
                .toList();

        model.addAttribute("appointments", appointments);
        model.addAttribute("doctors", doctorRepository.findAll());
        model.addAttribute("patients", patientRepository.findAll());
        model.addAttribute("search", search);
        model.addAttribute("statusFilter", normalizedStatus);
        try {
            model.addAttribute("activity", activityService.getRecentActivity());
        } catch (DataAccessException exception) {
            model.addAttribute("activity", java.util.Collections.emptyList());
            model.addAttribute("activityError",
                    "MongoDB activity history is unavailable. Check the MONGODB_URI connection and try again.");
        }
        return "appointments";
    }

    @PostMapping("/appointments-page/status")
    public String updateAppointmentStatus(@RequestParam Long appointmentId,
                                          @RequestParam String status,
                                          RedirectAttributes redirectAttributes) {
        String normalizedStatus = status.trim().toUpperCase(Locale.ROOT);
        if (!List.of("SCHEDULED", "COMPLETED", "CANCELLED").contains(normalizedStatus)) {
            redirectAttributes.addFlashAttribute("error", "Choose a valid appointment status.");
            return "redirect:/appointments-page";
        }

        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment == null) {
            redirectAttributes.addFlashAttribute("error", "Appointment not found.");
            return "redirect:/appointments-page";
        }

        appointment.setStatus(normalizedStatus);
        appointmentRepository.save(appointment);
        recordAppointmentActivity(appointmentId, "Appointment " + normalizedStatus.toLowerCase(Locale.ROOT),
                redirectAttributes);
        return "redirect:/appointments-page";
    }

    @PostMapping("/appointments-page/reschedule")
    public String rescheduleAppointment(@RequestParam Long appointmentId,
                                        @RequestParam String appointmentTime,
                                        RedirectAttributes redirectAttributes) {
        LocalDateTime parsedTime;
        try {
            parsedTime = LocalDateTime.parse(appointmentTime);
        } catch (DateTimeParseException exception) {
            redirectAttributes.addFlashAttribute("error", "Enter a valid appointment date and time.");
            return "redirect:/appointments-page";
        }

        if (!parsedTime.isAfter(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "Appointment time must be in the future.");
            return "redirect:/appointments-page";
        }

        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment == null) {
            redirectAttributes.addFlashAttribute("error", "Appointment not found.");
            return "redirect:/appointments-page";
        }

        appointment.setAppointmentTime(parsedTime);
        appointment.setStatus("SCHEDULED");
        appointmentRepository.save(appointment);
        recordAppointmentActivity(appointmentId, "Appointment rescheduled", redirectAttributes);
        return "redirect:/appointments-page";
    }

    private void recordAppointmentActivity(Long appointmentId, String eventType,
                                           RedirectAttributes redirectAttributes) {
        try {
            activityService.recordAppointmentEvent(appointmentId, eventType);
        } catch (DataAccessException exception) {
            redirectAttributes.addFlashAttribute("error",
                    "Appointment #" + appointmentId + " was updated in MySQL, but its activity "
                            + "could not be saved in MongoDB. Check the MongoDB connection.");
        }
    }

    @PostMapping("/appointments-page/book")
    public String bookAppointment(@RequestParam Long patientId,
                                   @RequestParam Long doctorId,
                                   @RequestParam String appointmentTime,
    Model model,
    RedirectAttributes redirectAttributes) {
        LocalDateTime parsedTime = LocalDateTime.parse(appointmentTime);

        if (parsedTime.isBefore(LocalDateTime.now())) {
            model.addAttribute("appointments", appointmentRepository.findAll());
            model.addAttribute("doctors", doctorRepository.findAll());
            model.addAttribute("patients", patientRepository.findAll());
            model.addAttribute("error", "Appointment time cannot be in the past.");
            return "appointments";
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patientRepository.findById(patientId).orElseThrow());
        appointment.setDoctor(doctorRepository.findById(doctorId).orElseThrow());
        appointment.setAppointmentTime(parsedTime);
        appointment.setStatus("SCHEDULED");
        Appointment savedAppointment = appointmentRepository.save(appointment);
        try {
            activityService.recordAppointmentBooked(savedAppointment.getId());
        } catch (DataAccessException exception) {
            redirectAttributes.addFlashAttribute("error",
                    "Appointment #" + savedAppointment.getId()
                            + " was saved in MySQL, but its activity record could not be saved in MongoDB. "
                            + "Check the MongoDB connection; do not submit the appointment again.");
        }
        return "redirect:/appointments-page";
    }
}