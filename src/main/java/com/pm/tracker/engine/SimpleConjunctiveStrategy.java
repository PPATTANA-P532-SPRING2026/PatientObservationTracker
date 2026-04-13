package com.pm.tracker.engine;

import com.pm.tracker.model.knowledge.AssociativeFunction;
import com.pm.tracker.model.knowledge.PhenomenonType;
import com.pm.tracker.model.operational.CategoryObservation;
import com.pm.tracker.model.operational.Measurement;
import com.pm.tracker.model.operational.Observation;
import com.pm.tracker.model.operational.Presence;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@Primary
public class SimpleConjunctiveStrategy implements DiagnosisStrategy {

    @Override
    public boolean evaluate(AssociativeFunction rule, List<Observation> patientObservations) {
        if (rule == null || rule.getArgumentConcepts() == null || patientObservations == null) {
            return false;
        }

        for (PhenomenonType concept : rule.getArgumentConcepts()) {
            if (!isConceptPresent(concept, patientObservations)) {
                return false;
            }
        }
        return true;
    }

    private boolean isConceptPresent(PhenomenonType concept, List<Observation> observations) {
        if (concept == null) {
            return false;
        }

        for (Observation obs : observations) {
            if (obs instanceof Measurement m) {
                PhenomenonType type = m.getPhenomenonType();
                if (type != null && Objects.equals(type.getId(), concept.getId())) {
                    return true;
                }
            } else if (obs instanceof CategoryObservation co) {
                if (co.getPresence() == Presence.PRESENT
                        && co.getPhenomenon() != null
                        && co.getPhenomenon().getPhenomenonType() != null
                        && Objects.equals(co.getPhenomenon().getPhenomenonType().getId(), concept.getId())) {
                    return true;
                }
            }
        }
        return false;
    }
}