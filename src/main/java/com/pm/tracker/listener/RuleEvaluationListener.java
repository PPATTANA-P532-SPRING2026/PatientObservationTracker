package com.pm.tracker.listener;

import com.pm.tracker.access.AuditLogRepository;
import com.pm.tracker.engine.DiagnosisEngine;
import com.pm.tracker.event.ObservationSavedEvent;
import com.pm.tracker.model.knowledge.PhenomenonType;
import com.pm.tracker.model.log.AuditLogEntry;
import com.pm.tracker.model.operational.Patient;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class RuleEvaluationListener {

    private final DiagnosisEngine diagnosisEngine;
    private final AuditLogRepository auditLogRepository;

    public RuleEvaluationListener(DiagnosisEngine diagnosisEngine,
                                  AuditLogRepository auditLogRepository) {
        this.diagnosisEngine    = diagnosisEngine;
        this.auditLogRepository = auditLogRepository;
    }

    @EventListener
    public void onObservationSaved(ObservationSavedEvent event) {
        Patient patient = event.getObservation().getPatient();

        List<DiagnosisEngine.EvaluationResult> results =
                diagnosisEngine.evaluate(patient);

        if (!results.isEmpty()) {
            String inferredNames = results.stream()
                    .map(r -> r.getInferredConcept().getName()
                            + " [" + r.getStrategyUsed() + "]")
                    .collect(Collectors.joining(", "));

            auditLogRepository.save(new AuditLogEntry(
                    "RULE_EVALUATION_INFERRED",
                    event.getObservation().getId(),
                    patient.getId(),
                    "Inferred: " + inferredNames
            ));
        }
    }
}