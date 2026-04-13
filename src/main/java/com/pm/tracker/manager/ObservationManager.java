package com.pm.tracker.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.tracker.access.ObservationRepository;
import com.pm.tracker.access.PhenomenonRepository;
import com.pm.tracker.access.PhenomenonTypeRepository;
import com.pm.tracker.access.ProtocolRepository;
import com.pm.tracker.command.CommandLog;
import com.pm.tracker.command.RecordObservationCommand;
import com.pm.tracker.command.RejectObservationCommand;
import com.pm.tracker.event.ObservationRejectedEvent;
import com.pm.tracker.event.ObservationSavedEvent;
import com.pm.tracker.factory.ObservationFactory;
import com.pm.tracker.model.knowledge.Phenomenon;
import com.pm.tracker.model.knowledge.PhenomenonType;
import com.pm.tracker.model.knowledge.Protocol;
import com.pm.tracker.model.operational.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Service
public class ObservationManager {

    private final ObservationFactory observationFactory;
    private final ObservationRepository observationRepository;
    private final PhenomenonTypeRepository phenomenonTypeRepository;
    private final PhenomenonRepository phenomenonRepository;
    private final ProtocolRepository protocolRepository;
    private final PatientManager patientManager;
    private final CommandLog commandLog;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public ObservationManager(ObservationFactory observationFactory,
                              ObservationRepository observationRepository,
                              PhenomenonTypeRepository phenomenonTypeRepository,
                              PhenomenonRepository phenomenonRepository,
                              ProtocolRepository protocolRepository,
                              PatientManager patientManager,
                              CommandLog commandLog,
                              ApplicationEventPublisher eventPublisher,
                              ObjectMapper objectMapper) {
        this.observationFactory       = observationFactory;
        this.observationRepository    = observationRepository;
        this.phenomenonTypeRepository = phenomenonTypeRepository;
        this.phenomenonRepository     = phenomenonRepository;
        this.protocolRepository       = protocolRepository;
        this.patientManager           = patientManager;
        this.commandLog               = commandLog;
        this.eventPublisher           = eventPublisher;
        this.objectMapper             = objectMapper;
    }

    // ── Record Measurement ────────────────────────────────────────────
    public Measurement recordMeasurement(UUID patientId,
                                         UUID phenomenonTypeId,
                                         Double amount,
                                         String unit,
                                         LocalDateTime applicabilityTime,
                                         UUID protocolId) {
        Patient patient = patientManager.findById(patientId);

        PhenomenonType phenomenonType = phenomenonTypeRepository
                .findById(phenomenonTypeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "PhenomenonType not found: " + phenomenonTypeId));

        Protocol protocol = resolveProtocol(protocolId);

        // Factory validates kind, unit, amount — throws if invalid
        Measurement measurement = observationFactory.createMeasurement(
                patient, phenomenonType, amount, unit,
                applicabilityTime, protocol);

        // Command wraps save + persists JSON payload to command log
        RecordObservationCommand cmd = new RecordObservationCommand(
                measurement, observationRepository, objectMapper);
        commandLog.execute(cmd);

        // Observer pattern — publish event; listeners handle the rest
        eventPublisher.publishEvent(new ObservationSavedEvent(measurement));

        return measurement;
    }

    // ── Record Category Observation ───────────────────────────────────
    public CategoryObservation recordCategoryObservation(UUID patientId,
                                                         UUID phenomenonId,
                                                         Presence presence,
                                                         LocalDateTime applicabilityTime,
                                                         UUID protocolId) {
        Patient patient = patientManager.findById(patientId);

        Phenomenon phenomenon = phenomenonRepository
                .findById(phenomenonId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Phenomenon not found: " + phenomenonId));

        Protocol protocol = resolveProtocol(protocolId);

        // Factory validates kind and presence — throws if invalid
        CategoryObservation catObs = observationFactory.createCategoryObservation(
                patient, phenomenon, presence, applicabilityTime, protocol);

        RecordObservationCommand cmd = new RecordObservationCommand(
                catObs, observationRepository, objectMapper);
        commandLog.execute(cmd);

        eventPublisher.publishEvent(new ObservationSavedEvent(catObs));

        return catObs;
    }

    // ── Reject Observation ────────────────────────────────────────────
    public Observation rejectObservation(UUID observationId,
                                         String rejectionReason) {
        Observation observation = observationRepository
                .findById(observationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Observation not found: " + observationId));

        if (observation.getStatus() == ObservationStatus.REJECTED) {
            throw new IllegalStateException(
                    "Observation is already rejected: " + observationId);
        }

        RejectObservationCommand cmd = new RejectObservationCommand(
                observation, rejectionReason, observationRepository, objectMapper);
        commandLog.execute(cmd);

        eventPublisher.publishEvent(
                new ObservationRejectedEvent(observation, rejectionReason));

        return observation;
    }

    // ── Query ─────────────────────────────────────────────────────────
    public List<Observation> listForPatient(UUID patientId) {
        Patient patient = patientManager.findById(patientId);
        return observationRepository
                .findByPatientOrderByRecordingTimeDesc(patient);
    }

    // ── helpers ───────────────────────────────────────────────────────
    private Protocol resolveProtocol(UUID protocolId) {
        if (protocolId == null) return null;
        return protocolRepository.findById(protocolId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Protocol not found: " + protocolId));
    }
}