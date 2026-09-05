package com.hospital.appointmentsystem.repository;

import com.hospital.appointmentsystem.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByEmailIgnoreCase(String email);
    List<Patient> findByNameContainingIgnoreCase(String name);
    List<Patient> findByPhoneContaining(String phone);

    @Query("SELECT p FROM Patient p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(p.email) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR p.phone LIKE CONCAT('%', :q, '%')")
    List<Patient> search(@Param("q") String query);
}
