package com.hospital.appointmentsystem.controller;

import com.hospital.appointmentsystem.entity.Doctor;
import com.hospital.appointmentsystem.repository.DoctorRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorRepository doctorRepository;

    public DoctorController(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @GetMapping
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @PostMapping
    public Doctor addDoctor(@Valid @RequestBody Doctor doctor) {
        return doctorRepository.save(doctor);
    }
}