package com.pm.tracker.model.knowledge;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "phenomena")
public class Phenomenon {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "phenomenon_type_id", nullable = false)
    private PhenomenonType phenomenonType;

    // ── Change 4 addition ─────────────────────────────────────────────
    // nullable — top-level phenomena have no parent
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_concept_id")
    private Phenomenon parentConcept;

    // ── constructors ──────────────────────────────────────────────────
    public Phenomenon() {}

    public Phenomenon(String name, PhenomenonType phenomenonType) {
        this.name           = name;
        this.phenomenonType = phenomenonType;
    }

    // ── getters / setters ─────────────────────────────────────────────
    public UUID getId()                             { return id; }
    public String getName()                         { return name; }
    public void setName(String name)                { this.name = name; }
    public PhenomenonType getPhenomenonType()        { return phenomenonType; }
    public void setPhenomenonType(PhenomenonType pt) { this.phenomenonType = pt; }
    public Phenomenon getParentConcept()             { return parentConcept; }
    public void setParentConcept(Phenomenon p)       { this.parentConcept = p; }
}