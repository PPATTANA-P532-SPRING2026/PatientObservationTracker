package com.pm.tracker.model.operational;

import com.pm.tracker.model.knowledge.PhenomenonType;
import com.pm.tracker.model.knowledge.Protocol;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "measurements")
public class Measurement extends Observation {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "phenomenon_type_id", nullable = false)
    private PhenomenonType phenomenonType;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String unit;

    // ── constructors ──────────────────────────────────────────────────
    public Measurement() {}

    public Measurement(Patient patient,
                       PhenomenonType phenomenonType,
                       Double amount,
                       String unit,
                       LocalDateTime recordingTime,
                       LocalDateTime applicabilityTime,
                       Protocol protocol) {
        super(patient, recordingTime, applicabilityTime, protocol);
        this.phenomenonType = phenomenonType;
        this.amount         = amount;
        this.unit           = unit;
    }

    // ── getters / setters ─────────────────────────────────────────────
    public PhenomenonType getPhenomenonType()          { return phenomenonType; }
    public void setPhenomenonType(PhenomenonType pt)   { this.phenomenonType = pt; }
    public Double getAmount()                          { return amount; }
    public void setAmount(Double amount)               { this.amount = amount; }
    public String getUnit()                            { return unit; }
    public void setUnit(String unit)                   { this.unit = unit; }
}