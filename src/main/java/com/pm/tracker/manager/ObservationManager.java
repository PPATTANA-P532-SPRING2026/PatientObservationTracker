package com.pm.tracker.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.tracker.access.*;
import com.pm.tracker.command.*;
import com.pm.tracker.event.*;
import com.pm.tracker.factory.ObservationFactory;
import com.pm.tracker.handler.*;
import com.pm.tracker.model.knowledge.*;
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
    private final ObservationProcessor processorChain;

    public ObservationManager(ObservationFactory observationFactory,
                              ObservationRepository observationRepository,
                              PhenomenonTypeRepository phenomenonTypeRepository,
                              PhenomenonRepository phenomenonRepository,
                              ProtocolRepository protocolRepository,
                              PatientManager patientManager,
                              CommandLog commandLog,
                              ApplicationEventPublisher eventPublisher,
                              ObjectMapper objectMapper,
                              BaseObservationProcessor base) {
        this.observationFactory       = observationFactory;
        this.observationRepository    = observationRepository;
        this.phenomenonTypeRepository = phenomenonTypeRepository;
        this.phenomenonRepository     = phenomenonRepository;
        this.protocolRepository       = protocolRepository;
        this.patientManager           = patientManager;
        this.commandLog               = commandLog;
        this.eventPublisher           = eventPublisher;
        this.objectMapper             = objectMapper;

        this.processorChain = new AuditStampingDecorator(
                new AnomalyFlaggingDecorator(
                        new UnitValidationDecorator(base)),
                "staff"
        );
    }

    // ── Record Measurement ────────────────────────────────────────────
    public Observation recordMeasurement(UUID patientId,
                                         UUID phenomenonTypeId,
                                         Double amount,
                                         String unit,
                                         LocalDateTime applicabilityTime,
                                         UUID protocolId,
                                         String currentUser) {
        Patient patient = patientManager.findById(patientId);

        PhenomenonType phenomenonType = phenomenonTypeRepository
                .findById(phenomenonTypeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "PhenomenonType not found: " + phenomenonTypeId));

        Protocol protocol = resolveProtocol(protocolId);

        ObservationRequest request = new ObservationRequest();
        request.setPatient(patient);
        request.setPhenomenonType(phenomenonType);
        request.setAmount(amount);
        request.setUnit(unit);
        request.setApplicabilityTime(applicabilityTime);
        request.setProtocol(protocol);

        ObservationRequest processed = processorChain.process(request);
        Observation observation = observationFactory.createFromRequest(processed);

        RecordObservationCommand cmd = new RecordObservationCommand(
                observation, observationRepository, objectMapper, eventPublisher);
        commandLog.execute(cmd, currentUser);

        eventPublisher.publishEvent(new ObservationSavedEvent(observation));
        return observation;
    }

    // ── Record Category Observation ───────────────────────────────────
    public Observation recordCategoryObservation(UUID patientId,
                                                 UUID phenomenonId,
                                                 Presence presence,
                                                 LocalDateTime applicabilityTime,
                                                 UUID protocolId,
                                                 String currentUser) {
        Patient patient = patientManager.findById(patientId);

        Phenomenon phenomenon = phenomenonRepository
                .findById(phenomenonId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Phenomenon not found: " + phenomenonId));

        Protocol protocol = resolveProtocol(protocolId);

        ObservationRequest request = new ObservationRequest();
        request.setPatient(patient);
        request.setPhenomenon(phenomenon);
        request.setPhenomenonType(phenomenon.getPhenomenonType());
        request.setPresence(presence);
        request.setApplicabilityTime(applicabilityTime);
        request.setProtocol(protocol);

        ObservationRequest processed = processorChain.process(request);
        Observation observation = observationFactory.createFromRequest(processed);

        RecordObservationCommand cmd = new RecordObservationCommand(
                observation, observationRepository, objectMapper, eventPublisher);
        commandLog.execute(cmd, currentUser);

        eventPublisher.publishEvent(new ObservationSavedEvent(observation));
        return observation;
    }

    // ── Reject Observation ────────────────────────────────────────────
    public Observation rejectObservation(UUID observationId,
                                         String rejectionReason,
                                         String currentUser) {
        Observation observation = observationRepository
                .findById(observationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Observation not found: " + observationId));

        if (observation.getStatus() == ObservationStatus.REJECTED) {
            throw new IllegalStateException(
                    "Observation is already rejected: " + observationId);
        }

        RejectObservationCommand cmd = new RejectObservationCommand(
                observation, rejectionReason,
                observationRepository, objectMapper);
        commandLog.execute(cmd, currentUser);

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

    private Protocol resolveProtocol(UUID protocolId) {
        if (protocolId == null) return null;
        return protocolRepository.findById(protocolId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Protocol not found: " + protocolId));
    }
}