package com.hospital.appointmentsystem.repository;

import com.hospital.appointmentsystem.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    boolean existsByEmailIgnoreCase(String email);
}