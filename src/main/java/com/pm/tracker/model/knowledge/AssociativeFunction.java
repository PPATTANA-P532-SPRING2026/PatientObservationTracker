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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "af_argument_concepts",
            joinColumns        = @JoinColumn(name = "af_id"),
            inverseJoinColumns = @JoinColumn(name = "phenomenon_type_id")
    )
    private List<PhenomenonType> argumentConcepts = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_concept_id", nullable = false)
    private PhenomenonType productConcept;

    // ── Change 1 additions ────────────────────────────────────────────

    // which strategy evaluates this rule
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StrategyType strategyType = StrategyType.CONJUNCTIVE;

    // weight per argument concept — keyed by PhenomenonType id as string
    // stored as serialized JSON string to avoid extra table complexity
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "af_argument_weights",
            joinColumns = @JoinColumn(name = "af_id"))
    @MapKeyColumn(name = "phenomenon_type_id")
    @Column(name = "weight")
    private Map<String, Double> argumentWeights = new HashMap<>();

    // threshold for WeightedScoringStrategy
    @Column
    private Double threshold = 0.5;

    // ── constructors ──────────────────────────────────────────────────
    public AssociativeFunction() {}

    public AssociativeFunction(String name,
                               List<PhenomenonType> argumentConcepts,
                               PhenomenonType productConcept) {
        this.name             = name;
        this.argumentConcepts = argumentConcepts;
        this.productConcept   = productConcept;
        this.strategyType     = StrategyType.CONJUNCTIVE;
    }

    // ── getters / setters ─────────────────────────────────────────────
    public UUID getId()                                      { return id; }
    public String getName()                                  { return name; }
    public void setName(String name)                         { this.name = name; }
    public List<PhenomenonType> getArgumentConcepts()        { return argumentConcepts; }
    public void setArgumentConcepts(List<PhenomenonType> a)  { this.argumentConcepts = a; }
    public PhenomenonType getProductConcept()                { return productConcept; }
    public void setProductConcept(PhenomenonType p)          { this.productConcept = p; }
    public StrategyType getStrategyType()                    { return strategyType; }
    public void setStrategyType(StrategyType st)             { this.strategyType = st; }
    public Map<String, Double> getArgumentWeights()          { return argumentWeights; }
    public void setArgumentWeights(Map<String, Double> w)    { this.argumentWeights = w; }
    public Double getThreshold()                             { return threshold; }
    public void setThreshold(Double threshold)               { this.threshold = threshold; }
}