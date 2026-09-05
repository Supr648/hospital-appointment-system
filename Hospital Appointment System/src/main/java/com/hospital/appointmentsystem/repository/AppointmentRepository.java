package com.hospital.appointmentsystem.repository;

import com.hospital.appointmentsystem.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    boolean existsByDoctorIdAndAppointmentTime(Long doctorId, LocalDateTime appointmentTime);
    List<Appointment> findByStatusOrderByAppointmentTimeAsc(String status);
    List<Appointment> findAllByOrderByAppointmentTimeDesc();
    List<Appointment> findByStatus(String status);
    List<Appointment> findByDoctorIdOrderByAppointmentTimeDesc(Long doctorId);
    List<Appointment> findByPatientIdOrderByAppointmentTimeDesc(Long patientId);
    long countByStatus(String status);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentTime >= :from AND a.appointmentTime < :to ORDER BY a.appointmentTime ASC")
    List<Appointment> findBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT a FROM Appointment a WHERE " +
           "LOWER(a.patient.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(a.doctor.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(a.reason) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(a.status) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "ORDER BY a.appointmentTime DESC")
    List<Appointment> search(@Param("q") String query);
}
