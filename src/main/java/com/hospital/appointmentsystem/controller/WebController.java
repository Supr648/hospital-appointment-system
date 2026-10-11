package com.hospital.appointmentsystem.controller;

import com.hospital.appointmentsystem.entity.Appointment;
import com.hospital.appointmentsystem.entity.Doctor;
import com.hospital.appointmentsystem.entity.Patient;
import com.hospital.appointmentsystem.service.AppointmentActivityService;
import com.hospital.appointmentsystem.service.WorkspaceAccessService;
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
import java.security.Principal;

@Controller
public class WebController {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentActivityService activityService;
    private final WorkspaceAccessService workspaceAccess;

    public WebController(DoctorRepository doctorRepository,
                          PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          AppointmentActivityService activityService,
                          WorkspaceAccessService workspaceAccess) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.activityService = activityService;
        this.workspaceAccess = workspaceAccess;
    }

    @GetMapping("/")
    public String welcome() {
        return "welcome";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/guest-demo")
    public String guestDemo() {
        return "guest-demo";
    }

    @GetMapping("/dashboard")
    public String home(Model model, Principal principal) {
        Long workspaceId = workspaceAccess.workspaceId(principal.getName());
        model.addAttribute("doctorCount", doctorRepository.findAllByWorkspaceId(workspaceId).size());
        model.addAttribute("patientCount", patientRepository.findAllByWorkspaceId(workspaceId).size());
        model.addAttribute("appointmentCount", appointmentRepository.findAllByWorkspaceId(workspaceId).size());
        return "index";
    }

    @GetMapping("/doctors-page")
    public String doctorsPage(Model model, Principal principal) {
        model.addAttribute("doctors", doctorRepository.findAllByWorkspaceId(workspaceAccess.workspaceId(principal.getName())));
        return "doctors";
    }

    @PostMapping("/doctors-page/add")
    public String addDoctor(@RequestParam String name,
                             @RequestParam String specialization,
                             @RequestParam String email,
                             Principal principal,
                             Model model) {
        Long workspaceId = workspaceAccess.workspaceId(principal.getName());
        if (doctorRepository.existsByEmailIgnoreCaseAndWorkspaceId(email, workspaceId)) {
            model.addAttribute("doctors", doctorRepository.findAllByWorkspaceId(workspaceId));
            model.addAttribute("error", "A doctor with this email already exists.");
            return "doctors";
        }
        Doctor doctor = new Doctor();
        doctor.setName(name);
        doctor.setSpecialization(specialization);
        doctor.setEmail(email);
        doctor.setWorkspaceId(workspaceId);
        doctorRepository.save(doctor);
        return "redirect:/doctors-page";
    }

    @GetMapping("/patients-page")
    public String patientsPage(Model model, Principal principal) {
        model.addAttribute("patients", patientRepository.findAllByWorkspaceId(workspaceAccess.workspaceId(principal.getName())));
        return "patients";
    }

    @PostMapping("/patients-page/add")
    public String addPatient(@RequestParam String name,
                              @RequestParam String email,
                              @RequestParam String phone,
                              Principal principal,
                              Model model) {
        Long workspaceId = workspaceAccess.workspaceId(principal.getName());
        if (patientRepository.existsByEmailIgnoreCaseAndWorkspaceId(email, workspaceId)) {
            model.addAttribute("patients", patientRepository.findAllByWorkspaceId(workspaceId));
            model.addAttribute("error", "A patient with this email already exists.");
            return "patients";
        }
        Patient patient = new Patient();
        patient.setName(name);
        patient.setEmail(email);
        patient.setPhone(phone);
        patient.setWorkspaceId(workspaceId);
        patientRepository.save(patient);
        return "redirect:/patients-page";
    }

    @GetMapping("/appointments-page")
    public String appointmentsPage(@RequestParam(defaultValue = "") String search,
                                   @RequestParam(defaultValue = "ALL") String status,
                                   Model model,
                                   Principal principal) {
        Long workspaceId = workspaceAccess.workspaceId(principal.getName());
        String normalizedSearch = search.trim().toLowerCase(Locale.ROOT);
        String requestedStatus = status.trim().toUpperCase(Locale.ROOT);
        String normalizedStatus = List.of("ALL", "SCHEDULED", "COMPLETED", "CANCELLED")
                .contains(requestedStatus) ? requestedStatus : "ALL";

        List<Appointment> appointments = appointmentRepository.findAllByWorkspaceId(workspaceId).stream()
                .filter(appointment -> normalizedStatus.equals("ALL")
                        || normalizedStatus.equalsIgnoreCase(appointment.getStatus()))
                .filter(appointment -> normalizedSearch.isEmpty()
                        || appointment.getPatient().getName().toLowerCase(Locale.ROOT).contains(normalizedSearch)
                        || appointment.getDoctor().getName().toLowerCase(Locale.ROOT).contains(normalizedSearch)
                        || appointment.getDoctor().getSpecialization().toLowerCase(Locale.ROOT).contains(normalizedSearch))
                .sorted(Comparator.comparing(Appointment::getAppointmentTime))
                .toList();

        model.addAttribute("appointments", appointments);
        model.addAttribute("doctors", doctorRepository.findAllByWorkspaceId(workspaceId));
        model.addAttribute("patients", patientRepository.findAllByWorkspaceId(workspaceId));
        model.addAttribute("search", search);
        model.addAttribute("statusFilter", normalizedStatus);
        try {
            model.addAttribute("activity", activityService.getRecentActivity(workspaceId));
        } catch (DataAccessException exception) {
            model.addAttribute("activity", java.util.Collections.emptyList());
            model.addAttribute("activityError",
                    "Appointment activity history is temporarily unavailable. Check the database connection and try again.");
        }
        return "appointments";
    }

    @PostMapping("/appointments-page/status")
    public String updateAppointmentStatus(@RequestParam Long appointmentId,
                                          @RequestParam String status,
                                          Principal principal,
                                          RedirectAttributes redirectAttributes) {
        Long workspaceId = workspaceAccess.workspaceId(principal.getName());
        String normalizedStatus = status.trim().toUpperCase(Locale.ROOT);
        if (!List.of("SCHEDULED", "COMPLETED", "CANCELLED").contains(normalizedStatus)) {
            redirectAttributes.addFlashAttribute("error", "Choose a valid appointment status.");
            return "redirect:/appointments-page";
        }

        Appointment appointment = appointmentRepository.findByIdAndWorkspaceId(appointmentId, workspaceId).orElse(null);
        if (appointment == null) {
            redirectAttributes.addFlashAttribute("error", "Appointment not found.");
            return "redirect:/appointments-page";
        }

        appointment.setStatus(normalizedStatus);
        appointmentRepository.save(appointment);
        recordAppointmentActivity(appointmentId, workspaceId, "Appointment " + normalizedStatus.toLowerCase(Locale.ROOT),
                redirectAttributes);
        return "redirect:/appointments-page";
    }

    @PostMapping("/appointments-page/reschedule")
    public String rescheduleAppointment(@RequestParam Long appointmentId,
                                        @RequestParam String appointmentTime,
    Principal principal,
    RedirectAttributes redirectAttributes) {
        Long workspaceId = workspaceAccess.workspaceId(principal.getName());
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

        Appointment appointment = appointmentRepository.findByIdAndWorkspaceId(appointmentId, workspaceId).orElse(null);
        if (appointment == null) {
            redirectAttributes.addFlashAttribute("error", "Appointment not found.");
            return "redirect:/appointments-page";
        }

        appointment.setAppointmentTime(parsedTime);
        appointment.setStatus("SCHEDULED");
        appointmentRepository.save(appointment);
        recordAppointmentActivity(appointmentId, workspaceId, "Appointment rescheduled", redirectAttributes);
        return "redirect:/appointments-page";
    }

    private void recordAppointmentActivity(Long appointmentId, Long workspaceId, String eventType,
                                           RedirectAttributes redirectAttributes) {
        try {
            activityService.recordAppointmentEvent(appointmentId, workspaceId, eventType);
        } catch (DataAccessException exception) {
            redirectAttributes.addFlashAttribute("error",
                    "Appointment #" + appointmentId + " was updated, but its activity "
                            + "could not be saved. Check the database connection.");
        }
    }

    @PostMapping("/appointments-page/book")
    public String bookAppointment(@RequestParam Long patientId,
                                   @RequestParam Long doctorId,
                                   @RequestParam String appointmentTime,
    Principal principal,
    RedirectAttributes redirectAttributes) {
        Long workspaceId = workspaceAccess.workspaceId(principal.getName());
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

        Patient patient = patientRepository.findByIdAndWorkspaceId(patientId, workspaceId).orElse(null);
        if (patient == null) {
            redirectAttributes.addFlashAttribute("error", "Selected patient was not found.");
            return "redirect:/appointments-page";
        }

        Doctor doctor = doctorRepository.findByIdAndWorkspaceId(doctorId, workspaceId).orElse(null);
        if (doctor == null) {
            redirectAttributes.addFlashAttribute("error", "Selected doctor was not found.");
            return "redirect:/appointments-page";
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentTime(parsedTime);
        appointment.setStatus("SCHEDULED");
        appointment.setWorkspaceId(workspaceId);
        Appointment savedAppointment = appointmentRepository.save(appointment);
        try {
            activityService.recordAppointmentBooked(savedAppointment.getId(), workspaceId);
        } catch (DataAccessException exception) {
            redirectAttributes.addFlashAttribute("error",
                    "Appointment #" + savedAppointment.getId()
                            + " was saved, but its activity record could not be saved. "
                            + "Check the database connection; do not submit the appointment again.");
        }
        return "redirect:/appointments-page";
    }
}