package com.hospital.appointmentsystem.repository;

import com.hospital.appointmentsystem.entity.AppointmentActivity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AppointmentActivityRepository extends MongoRepository<AppointmentActivity, String> {
    List<AppointmentActivity> findTop10ByOrderByOccurredAtDesc();
}
