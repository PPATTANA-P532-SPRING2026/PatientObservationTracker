package com.pm.tracker.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.tracker.access.ObservationRepository;
import com.pm.tracker.model.operational.Observation;
import com.pm.tracker.model.operational.ObservationStatus;

import java.util.Map;

/**
 * Command — wraps the "reject observation" action.
 * Sets status to REJECTED and records the rejection reason.
 * Observation remains in the database for audit trail purposes.
 */
public class RejectObservationCommand implements Command {

    private final Observation observation;
    private final String rejectionReason;
    private final ObservationRepository observationRepository;
    private final ObjectMapper objectMapper;

    public RejectObservationCommand(Observation observation,
                                    String rejectionReason,
                                    ObservationRepository observationRepository,
                                    ObjectMapper objectMapper) {
        this.observation           = observation;
        this.rejectionReason       = rejectionReason;
        this.observationRepository = observationRepository;
        this.objectMapper          = objectMapper;
    }

    @Override
    public void execute() {
        observation.setStatus(ObservationStatus.REJECTED);
        observation.setRejectionReason(rejectionReason);
        observationRepository.save(observation);
    }

    @Override
    public String getCommandType() {
        return "REJECT_OBSERVATION";
    }

    @Override
    public String toJson() {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "observationId",  observation.getId().toString(),
                    "patientId",      observation.getPatient().getId().toString(),
                    "rejectionReason",rejectionReason != null ? rejectionReason : ""
            ));
        } catch (Exception e) {
            return "{\"error\":\"serialization failed\"}";
        }
    }

    public Observation getObservation() {
        return observation;
    }
}