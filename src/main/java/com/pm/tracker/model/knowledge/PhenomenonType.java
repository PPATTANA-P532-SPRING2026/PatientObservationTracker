package com.pm.tracker.model.knowledge;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Entity
@Table(name = "phenomenon_types")
public class PhenomenonType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeasurementKind kind;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "phenomenon_type_units",
            joinColumns = @JoinColumn(name = "phenomenon_type_id")
    )
    @Column(name = "unit")
    private Set<String> allowedUnits = new HashSet<>();

    @OneToMany(mappedBy = "phenomenonType",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER)
    private List<Phenomenon> phenomena = new ArrayList<>();
    @Column
    private Double normalMin;

    @Column
    private Double normalMax;

    public PhenomenonType() {}

    public PhenomenonType(String name, MeasurementKind kind) {
        this.name = name;
        this.kind = kind;
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

    public MeasurementKind getKind() {
        return kind;
    }

    public void setKind(MeasurementKind kind) {
        this.kind = kind;
    }

    public Set<String> getAllowedUnits() {
        return allowedUnits;
    }

    public void setAllowedUnits(Set<String> u) {
        this.allowedUnits = u;
    }

    public List<Phenomenon> getPhenomena() {
        return phenomena;
    }

    public void setPhenomena(List<Phenomenon> p) {
        this.phenomena = p;
    }
    public Double getNormalMin()             { return normalMin; }
    public void setNormalMin(Double min)     { this.normalMin = min; }
    public Double getNormalMax()             { return normalMax; }
    public void setNormalMax(Double max)     { this.normalMax = max; }
}