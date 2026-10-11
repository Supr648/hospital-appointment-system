package com.hospital.appointmentsystem.controller;

import com.hospital.appointmentsystem.entity.Patient;
import com.hospital.appointmentsystem.repository.PatientRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientRepository patientRepository;

    public PatientController(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @GetMapping
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @PostMapping
    public Patient addPatient(@Valid @RequestBody Patient patient) {
        return patientRepository.save(patient);
    }
}