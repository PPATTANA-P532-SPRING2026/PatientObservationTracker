package com.pm.tracker.handler;

import com.pm.tracker.model.knowledge.Phenomenon;
import com.pm.tracker.model.knowledge.PhenomenonType;
import com.pm.tracker.model.knowledge.Protocol;
import com.pm.tracker.model.operational.Patient;
import com.pm.tracker.model.operational.Presence;

import java.time.LocalDateTime;


public class ObservationRequest {

    // ── Input fields ──────────────────────────────────────────────────
    private Patient patient;
    private PhenomenonType phenomenonType;    // for measurements
    private Phenomenon phenomenon;            // for category observations
    private Double amount;
    private String unit;
    private Presence presence;
    private Protocol protocol;
    private LocalDateTime applicabilityTime;

    // ── Fields attached by decorators ─────────────────────────────────
    private LocalDateTime recordingTime;     // set by AuditStampingDecorator
    private String actingUser;               // set by AuditStampingDecorator
    private boolean anomalyFlagged = false;  // set by AnomalyFlaggingDecorator
    private String anomalyDetail;            // set by AnomalyFlaggingDecorator

    // ── constructors ──────────────────────────────────────────────────
    public ObservationRequest() {}

    // ── getters / setters ─────────────────────────────────────────────
    public Patient getPatient()                          { return patient; }
    public void setPatient(Patient p)                    { this.patient = p; }

    public PhenomenonType getPhenomenonType()             { return phenomenonType; }
    public void setPhenomenonType(PhenomenonType pt)      { this.phenomenonType = pt; }

    public Phenomenon getPhenomenon()                    { return phenomenon; }
    public void setPhenomenon(Phenomenon ph)             { this.phenomenon = ph; }

    public Double getAmount()                            { return amount; }
    public void setAmount(Double amount)                 { this.amount = amount; }

    public String getUnit()                              { return unit; }
    public void setUnit(String unit)                     { this.unit = unit; }

    public Presence getPresence()                        { return presence; }
    public void setPresence(Presence p)                  { this.presence = p; }

    public Protocol getProtocol()                        { return protocol; }
    public void setProtocol(Protocol p)                  { this.protocol = p; }

    public LocalDateTime getApplicabilityTime()          { return applicabilityTime; }
    public void setApplicabilityTime(LocalDateTime t)    { this.applicabilityTime = t; }

    public LocalDateTime getRecordingTime()              { return recordingTime; }
    public void setRecordingTime(LocalDateTime t)        { this.recordingTime = t; }

    public String getActingUser()                        { return actingUser; }
    public void setActingUser(String user)               { this.actingUser = user; }

    public boolean isAnomalyFlagged()                    { return anomalyFlagged; }
    public void setAnomalyFlagged(boolean flagged)       { this.anomalyFlagged = flagged; }

    public String getAnomalyDetail()                     { return anomalyDetail; }
    public void setAnomalyDetail(String detail)          { this.anomalyDetail = detail; }
}