package com.pm.tracker.listener;

import com.pm.tracker.access.AuditLogRepository;
import com.pm.tracker.access.AssociativeFunctionRepository;
import com.pm.tracker.access.ObservationRepository;
import com.pm.tracker.engine.DiagnosisEngine;
import com.pm.tracker.engine.SimpleConjunctiveStrategy;
import com.pm.tracker.event.ObservationRejectedEvent;
import com.pm.tracker.event.ObservationSavedEvent;
import com.pm.tracker.model.knowledge.MeasurementKind;
import com.pm.tracker.model.knowledge.PhenomenonType;
import com.pm.tracker.model.log.AuditLogEntry;
import com.pm.tracker.model.operational.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ListenerTest {

    @Mock private AuditLogRepository auditLogRepository;
    @Mock private AssociativeFunctionRepository afRepository;
    @Mock private ObservationRepository observationRepository;

    private AuditLogListener auditLogListener;
    private RuleEvaluationListener ruleEvaluationListener;
    private DiagnosisEngine diagnosisEngine;

    private Patient patient;
    private Measurement measurement;

    @BeforeEach
    void setUp() {
        auditLogListener = new AuditLogListener(auditLogRepository);

        diagnosisEngine = new DiagnosisEngine(
                new SimpleConjunctiveStrategy(),
                afRepository,
                observationRepository);

        ruleEvaluationListener = new RuleEvaluationListener(
                diagnosisEngine, auditLogRepository);

        patient = new Patient("Bob", LocalDate.of(1975, 5, 20), "");

        PhenomenonType pt = new PhenomenonType("Blood Glucose",
                MeasurementKind.QUANTITATIVE);
        pt.setAllowedUnits(Set.of("mg/dL"));

        measurement = new Measurement(patient, pt, 120.0, "mg/dL",
                LocalDateTime.now(), LocalDateTime.now(), null);

        // Default stubs
        when(afRepository.findAll()).thenReturn(List.of());
        when(observationRepository.findByPatientAndStatus(any(), any()))
                .thenReturn(List.of());
    }

    @Test
    void auditLogListener_onObservationSaved_writesAuditEntry() {
        // Arrange
        ObservationSavedEvent event = new ObservationSavedEvent(measurement);

        // Act
        auditLogListener.onObservationSaved(event);

        // Assert
        ArgumentCaptor<AuditLogEntry> captor =
                ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(auditLogRepository).save(captor.capture());
        assertEquals("OBSERVATION_SAVED", captor.getValue().getEvent());
    }

    @Test
    void auditLogListener_onObservationRejected_writesRejectedEntry() {
        // Arrange
        ObservationRejectedEvent event = new ObservationRejectedEvent(
                measurement, "Data entry error");

        // Act
        auditLogListener.onObservationRejected(event);

        // Assert
        ArgumentCaptor<AuditLogEntry> captor =
                ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(auditLogRepository).save(captor.capture());
        assertEquals("OBSERVATION_REJECTED", captor.getValue().getEvent());
        assertTrue(captor.getValue().getDetail().contains("Data entry error"));
    }

    @Test
    void ruleEvaluationListener_noRules_doesNotSaveAuditEntry() {
        // Arrange — no rules in DB, no inferences
        when(afRepository.findAll()).thenReturn(List.of());
        ObservationSavedEvent event = new ObservationSavedEvent(measurement);

        // Act
        ruleEvaluationListener.onObservationSaved(event);

        // Assert — no inference entry written
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    void ruleEvaluationListener_ruleFiresInference_writesInferenceEntry() {
        // Arrange — set up a rule whose argument is satisfied by measurement
        PhenomenonType glucoseType = measurement.getPhenomenonType();
        PhenomenonType diabetesType = new PhenomenonType("Diabetes",
                MeasurementKind.QUALITATIVE);

        com.pm.tracker.model.knowledge.AssociativeFunction rule =
                new com.pm.tracker.model.knowledge.AssociativeFunction(
                        "Diabetes Rule",
                        List.of(glucoseType),
                        diabetesType);

        when(afRepository.findAll()).thenReturn(List.of(rule));
        when(observationRepository.findByPatientAndStatus(eq(patient),
                eq(ObservationStatus.ACTIVE)))
                .thenReturn(List.of(measurement));

        ObservationSavedEvent event = new ObservationSavedEvent(measurement);

        // Act
        ruleEvaluationListener.onObservationSaved(event);

        // Assert — inference entry saved
        ArgumentCaptor<AuditLogEntry> captor =
                ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(auditLogRepository).save(captor.capture());
        assertEquals("RULE_EVALUATION_INFERRED", captor.getValue().getEvent());
        assertTrue(captor.getValue().getDetail().contains("Diabetes"));
    }
}