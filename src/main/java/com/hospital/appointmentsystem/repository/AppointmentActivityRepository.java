package com.hospital.appointmentsystem.repository;

import com.hospital.appointmentsystem.entity.AppointmentActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentActivityRepository extends JpaRepository<AppointmentActivity, Long> {
    List<AppointmentActivity> findTop10ByWorkspaceIdOrderByOccurredAtDesc(Long workspaceId);
}
