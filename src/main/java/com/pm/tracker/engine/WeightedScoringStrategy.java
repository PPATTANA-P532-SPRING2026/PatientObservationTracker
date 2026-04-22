package com.pm.tracker.engine;

import com.pm.tracker.model.knowledge.AssociativeFunction;
import com.pm.tracker.model.knowledge.PhenomenonType;
import com.pm.tracker.model.operational.*;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class WeightedScoringStrategy implements DiagnosisStrategy {

    @Override
    public boolean evaluate(AssociativeFunction rule,
                            List<Observation> patientObservations) {
        // Change 4 — exclude INFERRED observations from evidence
        List<Observation> manualObs = patientObservations.stream()
                .filter(o -> o.getSource() == ObservationSource.MANUAL)
                .toList();

        double score     = 0.0;
        double threshold = rule.getThreshold() != null
                ? rule.getThreshold() : 0.5;

        for (PhenomenonType concept : rule.getArgumentConcepts()) {
            if (isConceptPresent(concept, manualObs)) {
                double weight = rule.getArgumentWeights()
                        .getOrDefault(
                                concept.getId() != null
                                        ? concept.getId().toString()
                                        : concept.getName(),
                                1.0);
                score += weight;
            }
        }

        return score >= threshold;
    }

    private boolean isConceptPresent(PhenomenonType concept,
                                     List<Observation> observations) {
        for (Observation obs : observations) {
            if (obs instanceof Measurement m) {
                if (m.getPhenomenonType().getName().equals(concept.getName())) {
                    return true;
                }
            } else if (obs instanceof CategoryObservation co) {
                if (co.getPhenomenon().getPhenomenonType().getName()
                        .equals(concept.getName())
                        && co.getPresence() == Presence.PRESENT) {
                    return true;
                }
            }
        }
        return false;
    }
}