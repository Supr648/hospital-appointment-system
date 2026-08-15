package com.hospital.appointmentsystem.repository;

import com.hospital.appointmentsystem.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
}