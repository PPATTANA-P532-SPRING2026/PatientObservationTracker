package com.pm.tracker.listener;

import com.pm.tracker.access.ObservationRepository;
import com.pm.tracker.access.PhenomenonRepository;
import com.pm.tracker.event.ObservationSavedEvent;
import com.pm.tracker.factory.ObservationFactory;
import com.pm.tracker.model.knowledge.Phenomenon;
import com.pm.tracker.model.operational.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * PropagationListener — Change 4 Week 2.
 *
 * Reacts to ObservationSavedEvent and propagates presence/absence
 * through the concept hierarchy per Fowler Section 3.6:
 *
 *   PRESENT observation → infer PRESENT for all ancestors
 *                          not already present for this patient
 *
 *   ABSENT observation  → infer ABSENT for all descendants
 *                          not already absent for this patient
 *
 * Inferred observations have source = INFERRED and are excluded
 * from rule evaluation by both strategy implementations.
 *
 * This listener is added with ZERO changes to existing listeners,
 * ObservationManager, or any other class — pure Observer pattern.
 */
@Component
public class PropagationListener {

    private final ObservationFactory observationFactory;
    private final ObservationRepository observationRepository;
    private final PhenomenonRepository phenomenonRepository;

    public PropagationListener(ObservationFactory observationFactory,
                               ObservationRepository observationRepository,
                               PhenomenonRepository phenomenonRepository) {
        this.observationFactory    = observationFactory;
        this.observationRepository = observationRepository;
        this.phenomenonRepository  = phenomenonRepository;
    }

    @EventListener
    public void onObservationSaved(ObservationSavedEvent event) {
        Observation obs = event.getObservation();

        // only propagate CategoryObservations — Measurements have no hierarchy
        if (!(obs instanceof CategoryObservation co)) return;

        // only propagate MANUAL observations — avoid circular inference
        if (co.getSource() == ObservationSource.INFERRED) return;

        Patient patient        = co.getPatient();
        Phenomenon phenomenon  = co.getPhenomenon();
        Presence presence      = co.getPresence();

        if (presence == Presence.PRESENT) {
            propagatePresenceUp(patient, phenomenon);
        } else {
            propagateAbsenceDown(patient, phenomenon);
        }
    }

    // ── Propagate PRESENT upward through ancestors ────────────────────
    private void propagatePresenceUp(Patient patient,
                                     Phenomenon phenomenon) {
        Phenomenon current = phenomenon.getParentConcept();

        while (current != null) {
            if (!isAlreadyPresent(patient, current)) {
                CategoryObservation inferred =
                        observationFactory.createInferredCategoryObservation(
                                patient, current, Presence.PRESENT);
                observationRepository.save(inferred);
            }
            current = current.getParentConcept();
        }
    }

    // ── Propagate ABSENT downward through descendants ─────────────────
    private void propagateAbsenceDown(Patient patient,
                                      Phenomenon phenomenon) {
        List<Phenomenon> children =
                phenomenonRepository.findByParentConcept(phenomenon);

        for (Phenomenon child : children) {
            if (!isAlreadyAbsent(patient, child)) {
                CategoryObservation inferred =
                        observationFactory.createInferredCategoryObservation(
                                patient, child, Presence.ABSENT);
                observationRepository.save(inferred);
            }
            // recurse into grandchildren
            propagateAbsenceDown(patient, child);
        }
    }

    // ── helpers ───────────────────────────────────────────────────────
    private boolean isAlreadyPresent(Patient patient, Phenomenon phenomenon) {
        return observationRepository
                .findByPatientOrderByRecordingTimeDesc(patient)
                .stream()
                .filter(o -> o instanceof CategoryObservation)
                .map(o -> (CategoryObservation) o)
                .anyMatch(co ->
                        co.getPhenomenon().getId().equals(phenomenon.getId())
                                && co.getPresence() == Presence.PRESENT
                                && co.getStatus() == ObservationStatus.ACTIVE);
    }

    private boolean isAlreadyAbsent(Patient patient, Phenomenon phenomenon) {
        return observationRepository
                .findByPatientOrderByRecordingTimeDesc(patient)
                .stream()
                .filter(o -> o instanceof CategoryObservation)
                .map(o -> (CategoryObservation) o)
                .anyMatch(co ->
                        co.getPhenomenon().getId().equals(phenomenon.getId())
                                && co.getPresence() == Presence.ABSENT
                                && co.getStatus() == ObservationStatus.ACTIVE);
    }
}