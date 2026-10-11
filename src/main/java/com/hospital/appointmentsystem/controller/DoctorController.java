package com.hospital.appointmentsystem.controller;

import com.hospital.appointmentsystem.entity.Doctor;
import com.hospital.appointmentsystem.repository.DoctorRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.security.Principal;
import com.hospital.appointmentsystem.service.WorkspaceAccessService;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorRepository doctorRepository;
    private final WorkspaceAccessService workspaceAccess;

    public DoctorController(DoctorRepository doctorRepository, WorkspaceAccessService workspaceAccess) {
        this.doctorRepository = doctorRepository;
        this.workspaceAccess = workspaceAccess;
    }

    @GetMapping
    public List<Doctor> getAllDoctors(Principal principal) {
        return doctorRepository.findAllByWorkspaceId(workspaceAccess.workspaceId(principal.getName()));
    }

    @PostMapping
    public Doctor addDoctor(@Valid @RequestBody Doctor doctor, Principal principal) {
        doctor.setId(null);
        doctor.setWorkspaceId(workspaceAccess.workspaceId(principal.getName()));
        return doctorRepository.save(doctor);
    }
}