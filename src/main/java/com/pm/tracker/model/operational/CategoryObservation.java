package com.pm.tracker.model.operational;

import com.pm.tracker.model.knowledge.Phenomenon;
import com.pm.tracker.model.knowledge.Protocol;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "category_observations")
public class CategoryObservation extends Observation {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "phenomenon_id", nullable = false)
    private Phenomenon phenomenon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Presence presence;

    // ── constructors ──────────────────────────────────────────────────
    public CategoryObservation() {}

    public CategoryObservation(Patient patient,
                               Phenomenon phenomenon,
                               Presence presence,
                               LocalDateTime recordingTime,
                               LocalDateTime applicabilityTime,
                               Protocol protocol) {
        super(patient, recordingTime, applicabilityTime, protocol);
        this.phenomenon = phenomenon;
        this.presence   = presence;
    }

    // ── getters / setters ─────────────────────────────────────────────
    public Phenomenon getPhenomenon()              { return phenomenon; }
    public void setPhenomenon(Phenomenon p)        { this.phenomenon = p; }
    public Presence getPresence()                  { return presence; }
    public void setPresence(Presence presence)     { this.presence = presence; }
}