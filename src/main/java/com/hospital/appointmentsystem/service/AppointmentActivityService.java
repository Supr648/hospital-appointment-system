package com.hospital.appointmentsystem.service;

import com.hospital.appointmentsystem.entity.AppointmentActivity;
import com.hospital.appointmentsystem.repository.AppointmentActivityRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class AppointmentActivityService {

    private final AppointmentActivityRepository activityRepository;

    public AppointmentActivityService(AppointmentActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public void recordAppointmentBooked(Long appointmentId, Long workspaceId) {
        recordAppointmentEvent(appointmentId, workspaceId, "Appointment booked");
    }

    public void recordAppointmentEvent(Long appointmentId, Long workspaceId, String eventType) {
        activityRepository.save(new AppointmentActivity(appointmentId, workspaceId, eventType, Instant.now()));
    }

    public List<AppointmentActivity> getRecentActivity(Long workspaceId) {
        return activityRepository.findTop10ByWorkspaceIdOrderByOccurredAtDesc(workspaceId);
    }
}
