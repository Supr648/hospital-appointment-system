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

    public void record(Long appointmentId, String eventType) {
        activityRepository.save(new AppointmentActivity(appointmentId, eventType, Instant.now()));
    }

    public List<AppointmentActivity> getRecentActivity() {
        return activityRepository.findTop10ByOrderByOccurredAtDesc();
    }
}
