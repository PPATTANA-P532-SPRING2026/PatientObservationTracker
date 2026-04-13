package com.pm.tracker.model.log;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_log_entries")
public class AuditLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String event;

    private UUID observationId;

    private UUID patientId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    // Extra detail — e.g. inferred concepts from rule evaluation
    @Column(columnDefinition = "TEXT")
    private String detail;

    // ── constructors ──────────────────────────────────────────────────
    public AuditLogEntry() {}

    public AuditLogEntry(String event, UUID observationId,
                         UUID patientId, String detail) {
        this.event         = event;
        this.observationId = observationId;
        this.patientId     = patientId;
        this.detail        = detail;
        this.timestamp     = LocalDateTime.now();
    }

    // ── getters ───────────────────────────────────────────────────────
    public UUID getId()                  { return id; }
    public String getEvent()             { return event; }
    public UUID getObservationId()       { return observationId; }
    public UUID getPatientId()           { return patientId; }
    public LocalDateTime getTimestamp()  { return timestamp; }
    public String getDetail()            { return detail; }
}