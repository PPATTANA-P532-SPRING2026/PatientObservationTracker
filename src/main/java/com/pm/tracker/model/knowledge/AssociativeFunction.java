package com.pm.tracker.model.knowledge;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "associative_functions")
public class AssociativeFunction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    // The observation concepts that must ALL be present to fire this rule.
    // We use PhenomenonType as the "observation concept" per our design:
    //   - a Measurement existing = its PhenomenonType is "present"
    //   - a CategoryObservation PRESENT = its phenomenon's type is "present"
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "af_argument_concepts",
            joinColumns        = @JoinColumn(name = "af_id"),
            inverseJoinColumns = @JoinColumn(name = "phenomenon_type_id")
    )
    private List<PhenomenonType> argumentConcepts = new ArrayList<>();

    // The inferred concept if all arguments are present
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_concept_id", nullable = false)
    private PhenomenonType productConcept;

    // ── constructors ──────────────────────────────────────────────────
    public AssociativeFunction() {}

    public AssociativeFunction(String name,
                               List<PhenomenonType> argumentConcepts,
                               PhenomenonType productConcept) {
        this.name             = name;
        this.argumentConcepts = argumentConcepts;
        this.productConcept   = productConcept;
    }

    // ── getters / setters ─────────────────────────────────────────────
    public UUID getId()                                    { return id; }
    public String getName()                                { return name; }
    public void setName(String name)                       { this.name = name; }
    public List<PhenomenonType> getArgumentConcepts()      { return argumentConcepts; }
    public void setArgumentConcepts(List<PhenomenonType> a){ this.argumentConcepts = a; }
    public PhenomenonType getProductConcept()              { return productConcept; }
    public void setProductConcept(PhenomenonType p)        { this.productConcept = p; }
}