package com.hospital.appointmentsystem.repository;

import com.hospital.appointmentsystem.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByEmailIgnoreCase(String email);
    List<Doctor> findBySpecializationContainingIgnoreCase(String specialization);
    List<Doctor> findByNameContainingIgnoreCase(String name);
    List<Doctor> findByAvailableTrue();
    List<Doctor> findByDepartmentContainingIgnoreCase(String department);

    @Query("SELECT d FROM Doctor d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(d.department) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Doctor> search(@Param("q") String query);
}
