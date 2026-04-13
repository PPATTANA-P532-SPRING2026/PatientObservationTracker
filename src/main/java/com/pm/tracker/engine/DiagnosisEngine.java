package com.pm.tracker.engine;

import com.pm.tracker.access.AssociativeFunctionRepository;
import com.pm.tracker.access.ObservationRepository;
import com.pm.tracker.model.knowledge.AssociativeFunction;
import com.pm.tracker.model.knowledge.PhenomenonType;
import com.pm.tracker.model.operational.Observation;
import com.pm.tracker.model.operational.ObservationStatus;
import com.pm.tracker.model.operational.Patient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class DiagnosisEngine {

    private DiagnosisStrategy strategy;
    private final AssociativeFunctionRepository afRepository;
    private final ObservationRepository observationRepository;

    public DiagnosisEngine(DiagnosisStrategy strategy,
                           AssociativeFunctionRepository afRepository,
                           ObservationRepository observationRepository) {
        this.strategy              = strategy;
        this.afRepository          = afRepository;
        this.observationRepository = observationRepository;
    }


    public List<PhenomenonType> evaluate(Patient patient) {
        // Load all active observations for this patient
        List<Observation> activeObs = observationRepository
                .findByPatientAndStatus(patient, ObservationStatus.ACTIVE);

        // Load all diagnostic rules
        List<AssociativeFunction> rules = afRepository.findAll();

        // Collect inferences
        List<PhenomenonType> inferences = new ArrayList<>();
        for (AssociativeFunction rule : rules) {
            if (strategy.evaluate(rule, activeObs)) {
                inferences.add(rule.getProductConcept());
            }
        }

        return inferences;
    }


    public String getStrategyName() {
        return strategy.getClass().getSimpleName();
    }

    // Package-level setter for testing — allows injecting a different strategy
    void setStrategy(DiagnosisStrategy strategy) {
       this.strategy = strategy;
    }
}