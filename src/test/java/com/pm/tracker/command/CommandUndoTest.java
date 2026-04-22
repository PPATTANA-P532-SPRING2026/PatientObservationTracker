package com.pm.tracker.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pm.tracker.access.ObservationRepository;
import com.pm.tracker.access.PatientRepository;
import com.pm.tracker.model.knowledge.MeasurementKind;
import com.pm.tracker.model.knowledge.Phenomenon;
import com.pm.tracker.model.knowledge.PhenomenonType;
import com.pm.tracker.model.operational.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandUndoTest {

    @Mock private PatientRepository patientRepository;
    @Mock private ObservationRepository observationRepository;

    private ObjectMapper objectMapper;
    private Patient patient;
    private CategoryObservation observation;

    @BeforeEach
    void setUp() {
        // Arrange — shared fixtures
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        patient = new Patient("Alice",
                LocalDate.of(1985, 3, 10), "note");

        PhenomenonType qualType = new PhenomenonType(
                "Blood Group", MeasurementKind.QUALITATIVE);
        Phenomenon ph = new Phenomenon("Blood Group A", qualType);

        observation = new CategoryObservation(
                patient, ph, Presence.PRESENT,
                LocalDateTime.now(), LocalDateTime.now(), null);
        observation.setSource(ObservationSource.MANUAL);
    }

    @Test
    void recordObservationCommand_undo_setsStatusRejected() {
        // Arrange
        RecordObservationCommand cmd = new RecordObservationCommand(
                observation, observationRepository, objectMapper);

        // Act
        cmd.undo();

        // Assert
        assertEquals(ObservationStatus.REJECTED, observation.getStatus());
        assertEquals("Undone by user", observation.getRejectionReason());
        verify(observationRepository).save(observation);
    }

    @Test
    void rejectObservationCommand_undo_restoresActive() {
        // Arrange — first reject it
        observation.setStatus(ObservationStatus.REJECTED);
        observation.setRejectionReason("Wrong entry");

        RejectObservationCommand cmd = new RejectObservationCommand(
                observation, "Wrong entry",
                observationRepository, objectMapper);

        // Act
        cmd.undo();

        // Assert
        assertEquals(ObservationStatus.ACTIVE, observation.getStatus());
        assertNull(observation.getRejectionReason());
        verify(observationRepository).save(observation);
    }

    @Test
    void createPatientCommand_undo_throwsUnsupportedOperation() {
        // Arrange
        CreatePatientCommand cmd = new CreatePatientCommand(
                patient, patientRepository, objectMapper);

        // Act + Assert
        assertThrows(UnsupportedOperationException.class, cmd::undo);
    }
}