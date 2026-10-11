package com.hospital.appointmentsystem.repository;

import com.hospital.appointmentsystem.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    java.util.List<Doctor> findAllByWorkspaceId(Long workspaceId);
    java.util.Optional<Doctor> findByIdAndWorkspaceId(Long id, Long workspaceId);
    boolean existsByEmailIgnoreCaseAndWorkspaceId(String email, Long workspaceId);
}