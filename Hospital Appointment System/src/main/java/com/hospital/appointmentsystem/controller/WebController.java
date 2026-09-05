package com.hospital.appointmentsystem.controller;

import com.hospital.appointmentsystem.entity.Appointment;
import com.hospital.appointmentsystem.entity.Doctor;
import com.hospital.appointmentsystem.entity.Patient;
import com.hospital.appointmentsystem.service.AppointmentService;
import com.hospital.appointmentsystem.service.DoctorService;
import com.hospital.appointmentsystem.service.PatientService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class WebController {

    private final DoctorService doctorService;
    private final PatientService patientService;
    private final AppointmentService appointmentService;

    public WebController(DoctorService doctorService,
                         PatientService patientService,
                         AppointmentService appointmentService) {
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.appointmentService = appointmentService;
    }

    // ---------- DASHBOARD ----------

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("doctorCount", doctorService.count());
        model.addAttribute("patientCount", patientService.count());
        model.addAttribute("appointmentCount", appointmentService.count());
        model.addAttribute("scheduledCount", appointmentService.countByStatus("SCHEDULED"));
        model.addAttribute("completedCount", appointmentService.countByStatus("COMPLETED"));
        model.addAttribute("cancelledCount", appointmentService.countByStatus("CANCELLED"));
        model.addAttribute("todayAppointments", appointmentService.findToday());
        model.addAttribute("upcoming", appointmentService.findUpcoming(5));
        model.addAttribute("availableDoctors", doctorService.findAvailable().size());
        return "index";
    }

    // ---------- DOCTORS ----------

    @GetMapping("/doctors-page")
    public String doctorsPage(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("doctors", doctorService.search(q));
        model.addAttribute("q", q);
        model.addAttribute("editDoctor", null);
        return "doctors";
    }

    @GetMapping("/doctors-page/edit/{id}")
    public String editDoctorForm(@PathVariable Long id, Model model) {
        Doctor doctor = doctorService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        model.addAttribute("doctors", doctorService.findAll());
        model.addAttribute("editDoctor", doctor);
        return "doctors";
    }

    @PostMapping("/doctors-page/add")
    public String addDoctor(@RequestParam String name,
                            @RequestParam String specialization,
                            @RequestParam String email,
                            @RequestParam(required = false) String phone,
                            @RequestParam(required = false) String department,
                            @RequestParam(defaultValue = "0") Integer experienceYears,
                            @RequestParam(required = false) String qualification,
                            @RequestParam(required = false) Double consultationFee,
                            @RequestParam(defaultValue = "true") Boolean available,
                            RedirectAttributes ra) {
        if (doctorService.emailExists(email, null)) {
            ra.addFlashAttribute("error", "A doctor with this email already exists.");
            return "redirect:/doctors-page";
        }
        Doctor doctor = new Doctor();
        doctor.setName(name);
        doctor.setSpecialization(specialization);
        doctor.setEmail(email);
        doctor.setPhone(phone);
        doctor.setDepartment(department);
        doctor.setExperienceYears(experienceYears);
        doctor.setQualification(qualification);
        doctor.setAvailable(available);
        doctorService.save(doctor);
        ra.addFlashAttribute("success", "Doctor added successfully.");
        return "redirect:/doctors-page";
    }

    @PostMapping("/doctors-page/update/{id}")
    public String updateDoctor(@PathVariable Long id,
                               @RequestParam String name,
                               @RequestParam String specialization,
                               @RequestParam String email,
                               @RequestParam(required = false) String phone,
                               @RequestParam(required = false) String department,
                               @RequestParam(defaultValue = "0") Integer experienceYears,
                               @RequestParam(required = false) String qualification,
                               @RequestParam(required = false) Double consultationFee,
                               @RequestParam(defaultValue = "true") Boolean available,
                               RedirectAttributes ra) {
        if (doctorService.emailExists(email, id)) {
            ra.addFlashAttribute("error", "Another doctor already uses this email.");
            return "redirect:/doctors-page/edit/" + id;
        }
        Doctor updated = new Doctor();
        updated.setName(name);
        updated.setSpecialization(specialization);
        updated.setEmail(email);
        updated.setPhone(phone);
        updated.setDepartment(department);
        updated.setExperienceYears(experienceYears);
        updated.setQualification(qualification);
        updated.setAvailable(available);
        doctorService.update(id, updated);
        ra.addFlashAttribute("success", "Doctor updated successfully.");
        return "redirect:/doctors-page";
    }

    @PostMapping("/doctors-page/toggle/{id}")
    public String toggleDoctor(@PathVariable Long id, RedirectAttributes ra) {
        doctorService.toggleAvailability(id);
        ra.addFlashAttribute("success", "Doctor availability updated.");
        return "redirect:/doctors-page";
    }

    @PostMapping("/doctors-page/delete/{id}")
    public String deleteDoctor(@PathVariable Long id, RedirectAttributes ra) {
        doctorService.deleteById(id);
        ra.addFlashAttribute("success", "Doctor removed successfully.");
        return "redirect:/doctors-page";
    }

    // ---------- PATIENTS ----------

    @GetMapping("/patients-page")
    public String patientsPage(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("patients", patientService.search(q));
        model.addAttribute("q", q);
        model.addAttribute("editPatient", null);
        return "patients";
    }

    @GetMapping("/patients-page/edit/{id}")
    public String editPatientForm(@PathVariable Long id, Model model) {
        Patient patient = patientService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        model.addAttribute("patients", patientService.findAll());
        model.addAttribute("editPatient", patient);
        return "patients";
    }

    @PostMapping("/patients-page/add")
    public String addPatient(@RequestParam String name,
                             @RequestParam String email,
                             @RequestParam String phone,
                             @RequestParam Integer age,
                             @RequestParam(required = false) String gender,
                             @RequestParam(required = false) String address,
                             @RequestParam(required = false) String bloodGroup,
                             RedirectAttributes ra) {
        if (patientService.emailExists(email, null)) {
            ra.addFlashAttribute("error", "A patient with this email already exists.");
            return "redirect:/patients-page";
        }
        Patient patient = new Patient();
        patient.setName(name);
        patient.setEmail(email);
        patient.setPhone(phone);
        patient.setAge(age);
        patient.setGender(gender);
        patient.setAddress(address);
        patient.setBloodGroup(bloodGroup);
        patientService.save(patient);
        ra.addFlashAttribute("success", "Patient registered successfully.");
        return "redirect:/patients-page";
    }

    @PostMapping("/patients-page/update/{id}")
    public String updatePatient(@PathVariable Long id,
                                @RequestParam String name,
                                @RequestParam String email,
                                @RequestParam String phone,
                                @RequestParam Integer age,
                                @RequestParam(required = false) String gender,
                                @RequestParam(required = false) String address,
                                @RequestParam(required = false) String bloodGroup,
                                RedirectAttributes ra) {
        if (patientService.emailExists(email, id)) {
            ra.addFlashAttribute("error", "Another patient already uses this email.");
            return "redirect:/patients-page/edit/" + id;
        }
        Patient updated = new Patient();
        updated.setName(name);
        updated.setEmail(email);
        updated.setPhone(phone);
        updated.setAge(age);
        updated.setGender(gender);
        updated.setAddress(address);
        updated.setBloodGroup(bloodGroup);
        patientService.update(id, updated);
        ra.addFlashAttribute("success", "Patient updated successfully.");
        return "redirect:/patients-page";
    }

    @PostMapping("/patients-page/delete/{id}")
    public String deletePatient(@PathVariable Long id, RedirectAttributes ra) {
        patientService.deleteById(id);
        ra.addFlashAttribute("success", "Patient removed successfully.");
        return "redirect:/patients-page";
    }

    // ---------- APPOINTMENTS ----------

    @GetMapping("/appointments-page")
    public String appointmentsPage(@RequestParam(required = false) String q,
                                   @RequestParam(required = false) String status,
                                   Model model) {
        if (q != null && !q.isBlank()) {
            model.addAttribute("appointments", appointmentService.search(q));
        } else if (status != null && !status.isBlank()) {
            model.addAttribute("appointments", appointmentService.findByStatus(status));
        } else {
            model.addAttribute("appointments", appointmentService.findAll());
        }
        model.addAttribute("doctors", doctorService.findAvailable());
        model.addAttribute("patients", patientService.findAll());
        model.addAttribute("q", q);
        model.addAttribute("statusFilter", status);
        return "appointments";
    }

    @PostMapping("/appointments-page/book")
    public String bookAppointment(@RequestParam Long patientId,
                                  @RequestParam Long doctorId,
                                  @RequestParam String appointmentTime,
                                  @RequestParam(required = false) String reason,
                                  @RequestParam(required = false) String notes,
                                  RedirectAttributes ra) {
        try {
            LocalDateTime parsedTime = LocalDateTime.parse(appointmentTime);
            appointmentService.book(patientId, doctorId, parsedTime, reason, notes);
            ra.addFlashAttribute("success", "Appointment booked successfully.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not book appointment. Check the date/time format.");
        }
        return "redirect:/appointments-page";
    }

    @PostMapping("/appointments-page/status/{id}")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               RedirectAttributes ra) {
        appointmentService.updateStatus(id, status);
        ra.addFlashAttribute("success", "Appointment status updated to " + status + ".");
        return "redirect:/appointments-page";
    }

    @PostMapping("/appointments-page/delete/{id}")
    public String deleteAppointment(@PathVariable Long id, RedirectAttributes ra) {
        appointmentService.deleteById(id);
        ra.addFlashAttribute("success", "Appointment record deleted.");
        return "redirect:/appointments-page";
    }
}
