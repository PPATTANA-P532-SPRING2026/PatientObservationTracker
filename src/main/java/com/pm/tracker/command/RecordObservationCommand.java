package com.pm.tracker.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.tracker.access.ObservationRepository;
import com.pm.tracker.event.ObservationRejectedEvent;
import com.pm.tracker.model.operational.CategoryObservation;
import com.pm.tracker.model.operational.Measurement;
import com.pm.tracker.model.operational.Observation;
import com.pm.tracker.model.operational.ObservationStatus;
import org.springframework.context.ApplicationEventPublisher;

import java.util.LinkedHashMap;
import java.util.Map;

public class RecordObservationCommand implements Command {

    private final Observation observation;
    private final ObservationRepository observationRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    public RecordObservationCommand(Observation observation,
                                    ObservationRepository observationRepository,
                                    ObjectMapper objectMapper,
                                    ApplicationEventPublisher eventPublisher) {
        this.observation           = observation;
        this.observationRepository = observationRepository;
        this.objectMapper          = objectMapper;
        this.eventPublisher        = eventPublisher;
    }

    @Override
    public void execute() {
        observationRepository.save(observation);
    }

    @Override
    public void undo() {
        observation.setStatus(ObservationStatus.REJECTED);
        observation.setRejectionReason("Undone by user");
        observationRepository.save(observation);
        // fire rejection event so listeners react
        eventPublisher.publishEvent(
                new ObservationRejectedEvent(observation, "Undone by user"));
    }

    @Override
    public String getCommandType() { return "RECORD_OBSERVATION"; }

    @Override
    public String toJson() {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("patientId",
                    observation.getPatient().getId().toString());
            payload.put("observationType",
                    observation.getClass().getSimpleName());
            payload.put("recordingTime",
                    observation.getRecordingTime().toString());
            payload.put("applicabilityTime",
                    observation.getApplicabilityTime().toString());
            if (observation.getProtocol() != null) {
                payload.put("protocolId",
                        observation.getProtocol().getId().toString());
            }
            if (observation instanceof Measurement m) {
                payload.put("phenomenonTypeId",
                        m.getPhenomenonType().getId().toString());
                payload.put("amount", m.getAmount());
                payload.put("unit",   m.getUnit());
            } else if (observation instanceof CategoryObservation co) {
                payload.put("phenomenonId",
                        co.getPhenomenon().getId().toString());
                payload.put("presence", co.getPresence().name());
            }
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            return "{\"error\":\"serialization failed\"}";
        }
    }

    public Observation getObservation() { return observation; }
}