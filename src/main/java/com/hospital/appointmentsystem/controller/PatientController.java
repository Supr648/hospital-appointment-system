package com.hospital.appointmentsystem.controller;

import com.hospital.appointmentsystem.entity.Patient;
import com.hospital.appointmentsystem.repository.PatientRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.security.Principal;
import com.hospital.appointmentsystem.service.WorkspaceAccessService;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientRepository patientRepository;
    private final WorkspaceAccessService workspaceAccess;

    public PatientController(PatientRepository patientRepository, WorkspaceAccessService workspaceAccess) {
        this.patientRepository = patientRepository;
        this.workspaceAccess = workspaceAccess;
    }

    @GetMapping
    public List<Patient> getAllPatients(Principal principal) {
        return patientRepository.findAllByWorkspaceId(workspaceAccess.workspaceId(principal.getName()));
    }

    @PostMapping
    public Patient addPatient(@Valid @RequestBody Patient patient, Principal principal) {
        patient.setId(null);
        patient.setWorkspaceId(workspaceAccess.workspaceId(principal.getName()));
        return patientRepository.save(patient);
    }
}