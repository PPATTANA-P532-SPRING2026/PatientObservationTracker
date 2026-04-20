package com.pm.tracker.model.knowledge;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;

import java.util.UUID;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
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

    public Phenomenon() {}

    public Phenomenon(String name, PhenomenonType phenomenonType) {
        this.name = name;
        this.phenomenonType = phenomenonType;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PhenomenonType getPhenomenonType() {
        return phenomenonType;
    }

    public void setPhenomenonType(PhenomenonType pt) {
        this.phenomenonType = pt;
    }
}