package com.pm.tracker.factory;

import com.pm.tracker.model.knowledge.MeasurementKind;
import com.pm.tracker.model.knowledge.Phenomenon;
import com.pm.tracker.model.knowledge.PhenomenonType;
import com.pm.tracker.model.knowledge.Protocol;
import com.pm.tracker.model.operational.CategoryObservation;
import com.pm.tracker.model.operational.Measurement;
import com.pm.tracker.model.operational.Patient;
import com.pm.tracker.model.operational.Presence;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * ObservationFactory — Factory pattern.
 *
 * All observation objects MUST be created through this factory.
 * Controllers must never call new Measurement(...) or
 * new CategoryObservation(...) directly.
 *
 * Responsibilities:
 *   - Validate phenomenonType kind matches the observation type
 *   - Validate unit is in the allowed set (for measurements)
 *   - Validate phenomenon belongs to the given phenomenonType (for category)
 *   - Construct and return the validated observation
 *
 * The manager trusts everything the factory produces is valid.
 */
@Component
public class ObservationFactory {

    /**
     * Creates a validated Measurement.
     *
     * @throws IllegalArgumentException if validation fails
     */
    public Measurement createMeasurement(Patient patient,
                                         PhenomenonType phenomenonType,
                                         Double amount,
                                         String unit,
                                         LocalDateTime applicabilityTime,
                                         Protocol protocol) {

        // Validate: phenomenonType must be QUANTITATIVE
        if (phenomenonType.getKind() != MeasurementKind.QUANTITATIVE) {
            throw new IllegalArgumentException(
                    "PhenomenonType '" + phenomenonType.getName()
                            + "' is " + phenomenonType.getKind()
                            + " — cannot create a Measurement for a QUALITATIVE type.");
        }

        // Validate: unit must be in the allowed set
        if (!phenomenonType.getAllowedUnits().contains(unit)) {
            throw new IllegalArgumentException(
                    "Unit '" + unit + "' is not allowed for phenomenon type '"
                            + phenomenonType.getName() + "'. Allowed units: "
                            + phenomenonType.getAllowedUnits());
        }

        // Validate: amount must not be null
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null.");
        }

        LocalDateTime recordingTime    = LocalDateTime.now();
        LocalDateTime applicabilityTs  = (applicabilityTime != null)
                ? applicabilityTime
                : recordingTime;

        return new Measurement(patient, phenomenonType, amount, unit,
                recordingTime, applicabilityTs, protocol);
    }

    /**
     * Creates a validated CategoryObservation.
     *
     * @throws IllegalArgumentException if validation fails
     */
    public CategoryObservation createCategoryObservation(Patient patient,
                                                         Phenomenon phenomenon,
                                                         Presence presence,
                                                         LocalDateTime applicabilityTime,
                                                         Protocol protocol) {

        // Validate: phenomenon's type must be QUALITATIVE
        if (phenomenon.getPhenomenonType().getKind() != MeasurementKind.QUALITATIVE) {
            throw new IllegalArgumentException(
                    "Phenomenon '" + phenomenon.getName()
                            + "' belongs to a QUANTITATIVE type — cannot create "
                            + "a CategoryObservation for a QUANTITATIVE type.");
        }

        // Validate: presence must not be null
        if (presence == null) {
            throw new IllegalArgumentException("Presence (PRESENT/ABSENT) cannot be null.");
        }

        LocalDateTime recordingTime   = LocalDateTime.now();
        LocalDateTime applicabilityTs = (applicabilityTime != null)
                ? applicabilityTime
                : recordingTime;

        return new CategoryObservation(patient, phenomenon, presence,
                recordingTime, applicabilityTs, protocol);
    }
}