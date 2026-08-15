package com.hospital.appointmentsystem.controller;

import com.hospital.appointmentsystem.entity.Appointment;
import com.hospital.appointmentsystem.entity.Doctor;
import com.hospital.appointmentsystem.entity.Patient;
import com.hospital.appointmentsystem.repository.AppointmentRepository;
import com.hospital.appointmentsystem.repository.DoctorRepository;
import com.hospital.appointmentsystem.repository.PatientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
public class WebController {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    public WebController(DoctorRepository doctorRepository,
                          PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/doctors-page")
    public String doctorsPage(Model model) {
        model.addAttribute("doctors", doctorRepository.findAll());
        return "doctors";
    }

    @GetMapping("/patients-page")
    public String patientsPage(Model model) {
        model.addAttribute("patients", patientRepository.findAll());
        return "patients";
    }

    @GetMapping("/appointments-page")
    public String appointmentsPage(Model model) {
        model.addAttribute("appointments", appointmentRepository.findAll());
        model.addAttribute("doctors", doctorRepository.findAll());
        model.addAttribute("patients", patientRepository.findAll());
        return "appointments";
    }

    @PostMapping("/appointments-page/book")
    public String bookAppointment(@RequestParam Long patientId,
                                   @RequestParam Long doctorId,
                                   @RequestParam String appointmentTime) {
        Appointment appointment = new Appointment();
        appointment.setPatient(patientRepository.findById(patientId).orElseThrow());
        appointment.setDoctor(doctorRepository.findById(doctorId).orElseThrow());
        appointment.setAppointmentTime(LocalDateTime.parse(appointmentTime));
        appointment.setStatus("SCHEDULED");
        appointmentRepository.save(appointment);
        return "redirect:/appointments-page";
    }
}