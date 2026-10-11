package com.hospital.appointmentsystem.repository;

import com.hospital.appointmentsystem.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    java.util.List<Patient> findAllByWorkspaceId(Long workspaceId);
    java.util.Optional<Patient> findByIdAndWorkspaceId(Long id, Long workspaceId);
    boolean existsByEmailIgnoreCaseAndWorkspaceId(String email, Long workspaceId);
}