package com.hospital.appointmentsystem.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "appointment_activity",
        indexes = @Index(name = "idx_appointment_activity_occurred_at", columnList = "occurred_at"))
public class AppointmentActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long appointmentId;
    private Long workspaceId;
    private String eventType;
    private Instant occurredAt;

    public AppointmentActivity() {
    }

    public AppointmentActivity(Long appointmentId, String eventType, Instant occurredAt) {
        this.appointmentId = appointmentId;
        this.eventType = eventType;
        this.occurredAt = occurredAt;
    }

    public AppointmentActivity(Long appointmentId, Long workspaceId, String eventType, Instant occurredAt) {
        this.appointmentId = appointmentId;
        this.workspaceId = workspaceId;
        this.eventType = eventType;
        this.occurredAt = occurredAt;
    }

    public Long getId() { return id; }
    public Long getAppointmentId() { return appointmentId; }
    public Long getWorkspaceId() { return workspaceId; }
    public String getEventType() { return eventType; }
    public Instant getOccurredAt() { return occurredAt; }
}
