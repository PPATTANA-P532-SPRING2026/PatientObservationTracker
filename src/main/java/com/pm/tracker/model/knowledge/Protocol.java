package com.pm.tracker.model.knowledge;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "protocols")
public class Protocol {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccuracyRating accuracyRating;

    // ── constructors ──────────────────────────────────────────────────
    public Protocol() {}

    public Protocol(String name, String description,
                    AccuracyRating accuracyRating) {
        this.name           = name;
        this.description    = description;
        this.accuracyRating = accuracyRating;
    }

    // ── getters / setters ─────────────────────────────────────────────
    public UUID getId()                              { return id; }
    public String getName()                          { return name; }
    public void setName(String name)                 { this.name = name; }
    public String getDescription()                   { return description; }
    public void setDescription(String d)             { this.description = d; }
    public AccuracyRating getAccuracyRating()        { return accuracyRating; }
    public void setAccuracyRating(AccuracyRating ar) { this.accuracyRating = ar; }
}