package com.pm.tracker.model.operational;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(length = 2000)
    private String note;

    // ── constructors ──────────────────────────────────────────────────
    public Patient() {}

    public Patient(String fullName, LocalDate dateOfBirth, String note) {
        this.fullName    = fullName;
        this.dateOfBirth = dateOfBirth;
        this.note        = note;
    }

    // ── getters / setters ─────────────────────────────────────────────
    public UUID getId()                       { return id; }
    public String getFullName()               { return fullName; }
    public void setFullName(String fullName)  { this.fullName = fullName; }
    public LocalDate getDateOfBirth()         { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dob) { this.dateOfBirth = dob; }
    public String getNote()                   { return note; }
    public void setNote(String note)          { this.note = note; }
}