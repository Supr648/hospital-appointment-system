package com.hospital.appointmentsystem.repository;

import com.hospital.appointmentsystem.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
}