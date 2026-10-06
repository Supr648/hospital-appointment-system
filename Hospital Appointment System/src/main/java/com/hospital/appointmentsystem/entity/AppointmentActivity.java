package com.hospital.appointmentsystem.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "appointment_activity")
public class AppointmentActivity {

    @Id
    private String id;

    private Long appointmentId;
    private String eventType;
    private Instant occurredAt;

    public AppointmentActivity() {
    }

    public AppointmentActivity(Long appointmentId, String eventType, Instant occurredAt) {
        this.appointmentId = appointmentId;
        this.eventType = eventType;
        this.occurredAt = occurredAt;
    }

    public String getId() { return id; }
    public Long getAppointmentId() { return appointmentId; }
    public String getEventType() { return eventType; }
    public Instant getOccurredAt() { return occurredAt; }
}
