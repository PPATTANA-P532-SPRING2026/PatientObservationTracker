package com.pm.tracker.engine;

import com.pm.tracker.access.AssociativeFunctionRepository;
import com.pm.tracker.access.ObservationRepository;
import com.pm.tracker.model.knowledge.AssociativeFunction;
import com.pm.tracker.model.knowledge.PhenomenonType;
import com.pm.tracker.model.knowledge.StrategyType;
import com.pm.tracker.model.operational.Observation;
import com.pm.tracker.model.operational.ObservationStatus;
import com.pm.tracker.model.operational.Patient;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DiagnosisEngine {

    // Change 1 — map of all strategies keyed by StrategyType
    private final Map<StrategyType, DiagnosisStrategy> strategies;
    private final AssociativeFunctionRepository afRepository;
    private final ObservationRepository observationRepository;

    public DiagnosisEngine(SimpleConjunctiveStrategy conjunctiveStrategy,
                           WeightedScoringStrategy weightedStrategy,
                           AssociativeFunctionRepository afRepository,
                           ObservationRepository observationRepository) {
        this.strategies = new EnumMap<>(StrategyType.class);
        this.strategies.put(StrategyType.CONJUNCTIVE, conjunctiveStrategy);
        this.strategies.put(StrategyType.WEIGHTED, weightedStrategy);
        this.afRepository          = afRepository;
        this.observationRepository = observationRepository;
    }

    /**
     * Evaluates all rules for a patient.
     * Returns a list of EvaluationResult — each holding the inferred
     * concept, the strategy used, and the contributing observations.
     */
    public List<EvaluationResult> evaluate(Patient patient) {
        List<Observation> activeObs = observationRepository
                .findByPatientAndStatus(patient, ObservationStatus.ACTIVE);

        List<AssociativeFunction> rules = afRepository.findAll();

        List<EvaluationResult> results = new ArrayList<>();
        for (AssociativeFunction rule : rules) {
            StrategyType type     = rule.getStrategyType() != null
                    ? rule.getStrategyType()
                    : StrategyType.CONJUNCTIVE;
            DiagnosisStrategy strategy = strategies.get(type);

            if (strategy.evaluate(rule, activeObs)) {
                // collect the observations that contributed as evidence
                List<Observation> evidence = activeObs.stream()
                        .filter(o -> isEvidence(o, rule))
                        .toList();

                results.add(new EvaluationResult(
                        rule.getProductConcept(),
                        type.name(),
                        evidence
                ));
            }
        }
        return results;
    }

    private boolean isEvidence(Observation obs, AssociativeFunction rule) {
        return rule.getArgumentConcepts().stream().anyMatch(concept -> {
            if (obs instanceof com.pm.tracker.model.operational.Measurement m) {
                return m.getPhenomenonType().getName().equals(concept.getName());
            } else if (obs instanceof com.pm.tracker.model.operational.CategoryObservation co) {
                return co.getPhenomenon().getPhenomenonType().getName()
                        .equals(concept.getName());
            }
            return false;
        });
    }

    public String getStrategyName() {
        return "CONJUNCTIVE + WEIGHTED";
    }

    // ── EvaluationResult inner class ──────────────────────────────────
    public static class EvaluationResult {
        private final PhenomenonType inferredConcept;
        private final String strategyUsed;
        private final List<Observation> evidence;

        public EvaluationResult(PhenomenonType inferredConcept,
                                String strategyUsed,
                                List<Observation> evidence) {
            this.inferredConcept = inferredConcept;
            this.strategyUsed    = strategyUsed;
            this.evidence        = evidence;
        }

        public PhenomenonType getInferredConcept() { return inferredConcept; }
        public String getStrategyUsed()            { return strategyUsed; }
        public List<Observation> getEvidence()     { return evidence; }
    }
}