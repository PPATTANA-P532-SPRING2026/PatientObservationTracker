package com.pm.tracker.model.operational;

import com.pm.tracker.model.knowledge.Protocol;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "observations")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Observation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false)
    private LocalDateTime recordingTime;

    @Column(nullable = false)
    private LocalDateTime applicabilityTime;

    // Optional — which protocol was used to make this observation
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "protocol_id")
    private Protocol protocol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ObservationStatus status = ObservationStatus.ACTIVE;

    @Column(length = 1000)
    private String rejectionReason;
    @Column(nullable = false)
    private boolean anomalyFlagged = false;

    @Column(length = 500)
    private String anomalyDetail;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ObservationSource source = ObservationSource.MANUAL;

    // ── constructors ──────────────────────────────────────────────────
    protected Observation() {}

    protected Observation(Patient patient,
                          LocalDateTime recordingTime,
                          LocalDateTime applicabilityTime,
                          Protocol protocol) {
        this.patient           = patient;
        this.recordingTime     = recordingTime;
        this.applicabilityTime = applicabilityTime;
        this.protocol          = protocol;
        this.status            = ObservationStatus.ACTIVE;
    }

    // ── getters / setters ─────────────────────────────────────────────
    public UUID getId()                                  { return id; }
    public Patient getPatient()                          { return patient; }
    public void setPatient(Patient p)                    { this.patient = p; }
    public LocalDateTime getRecordingTime()              { return recordingTime; }
    public void setRecordingTime(LocalDateTime t)        { this.recordingTime = t; }
    public LocalDateTime getApplicabilityTime()          { return applicabilityTime; }
    public void setApplicabilityTime(LocalDateTime t)    { this.applicabilityTime = t; }
    public Protocol getProtocol()                        { return protocol; }
    public void setProtocol(Protocol p)                  { this.protocol = p; }
    public ObservationStatus getStatus()                 { return status; }
    public void setStatus(ObservationStatus s)           { this.status = s; }
    public String getRejectionReason()                   { return rejectionReason; }
    public void setRejectionReason(String r)             { this.rejectionReason = r; }
    public boolean isAnomalyFlagged()              { return anomalyFlagged; }
    public void setAnomalyFlagged(boolean flagged) { this.anomalyFlagged = flagged; }
    public String getAnomalyDetail()               { return anomalyDetail; }
    public void setAnomalyDetail(String detail)    { this.anomalyDetail = detail; }
    public ObservationSource getSource()             { return source; }
    public void setSource(ObservationSource source)  { this.source = source; }
}