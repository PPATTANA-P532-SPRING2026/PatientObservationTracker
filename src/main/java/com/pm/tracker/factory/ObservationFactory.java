package com.pm.tracker.factory;

import com.pm.tracker.handler.ObservationRequest;
import com.pm.tracker.model.knowledge.MeasurementKind;
import com.pm.tracker.model.knowledge.Phenomenon;
import com.pm.tracker.model.knowledge.Protocol;
import com.pm.tracker.model.operational.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ObservationFactory {

    public Observation createFromRequest(ObservationRequest request) {
        if (request.getAmount() != null) {
            return createMeasurement(request);
        } else {
            return createCategoryObservation(request);
        }
    }

    private Measurement createMeasurement(ObservationRequest request) {
        if (request.getPhenomenonType().getKind() != MeasurementKind.QUANTITATIVE) {
            throw new IllegalArgumentException(
                    "PhenomenonType '" + request.getPhenomenonType().getName()
                            + "' is not QUANTITATIVE.");
        }

        Measurement m = new Measurement(
                request.getPatient(),
                request.getPhenomenonType(),
                request.getAmount(),
                request.getUnit(),
                request.getRecordingTime(),
                request.getApplicabilityTime() != null
                        ? request.getApplicabilityTime()
                        : request.getRecordingTime(),
                request.getProtocol()
        );
        m.setAnomalyFlagged(request.isAnomalyFlagged());
        m.setAnomalyDetail(request.getAnomalyDetail());
        m.setSource(ObservationSource.MANUAL);
        return m;
    }

    private CategoryObservation createCategoryObservation(
            ObservationRequest request) {
        if (request.getPhenomenon().getPhenomenonType().getKind()
                != MeasurementKind.QUALITATIVE) {
            throw new IllegalArgumentException(
                    "Phenomenon '" + request.getPhenomenon().getName()
                            + "' does not belong to a QUALITATIVE type.");
        }

        CategoryObservation co = new CategoryObservation(
                request.getPatient(),
                request.getPhenomenon(),
                request.getPresence(),
                request.getRecordingTime(),
                request.getApplicabilityTime() != null
                        ? request.getApplicabilityTime()
                        : request.getRecordingTime(),
                request.getProtocol()
        );
        co.setAnomalyFlagged(false);
        co.setSource(ObservationSource.MANUAL);
        return co;
    }

    // ── Change 4 — create an INFERRED category observation ────────────

    public CategoryObservation createInferredCategoryObservation(
            Patient patient,
            Phenomenon phenomenon,
            Presence presence) {
        LocalDateTime now = LocalDateTime.now();
        CategoryObservation co = new CategoryObservation(
                patient, phenomenon, presence, now, now, null);
        co.setSource(ObservationSource.INFERRED);
        co.setAnomalyFlagged(false);
        return co;
    }
}