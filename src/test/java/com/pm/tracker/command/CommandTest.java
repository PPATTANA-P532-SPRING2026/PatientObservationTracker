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
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandTest {

    @Mock private PatientRepository patientRepository;
    @Mock private ObservationRepository observationRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    private ObjectMapper objectMapper;
    private Patient patient;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        patient = new Patient("Alice", LocalDate.of(1985, 3, 10), "note");
    }

    @Test
    void createPatientCommand_execute_savesPatient() {
        // Arrange
        CreatePatientCommand cmd = new CreatePatientCommand(
                patient, patientRepository, objectMapper);

        // Act
        cmd.execute();

        // Assert
        verify(patientRepository).save(patient);
    }

    @Test
    void createPatientCommand_toJson_containsFullName() {
        // Arrange
        CreatePatientCommand cmd = new CreatePatientCommand(
                patient, patientRepository, objectMapper);

        // Act
        String json = cmd.toJson();

        // Assert
        assertTrue(json.contains("Alice"));
        assertEquals("CREATE_PATIENT", cmd.getCommandType());
    }

    @Test
    void recordObservationCommand_execute_savesObservation() {
        // Arrange
        PhenomenonType pt = new PhenomenonType("Body Temp", MeasurementKind.QUANTITATIVE);
        pt.setAllowedUnits(Set.of("Celsius"));

        Measurement m = new Measurement(patient, pt, 37.0, "Celsius",
                LocalDateTime.now(), LocalDateTime.now(), null);

        RecordObservationCommand cmd = new RecordObservationCommand(
                m, observationRepository, objectMapper, eventPublisher); // ← add eventPublisher

        // Act
        cmd.execute();

        // Assert
        verify(observationRepository).save(m);
    }

    @Test
    void rejectObservationCommand_execute_setsStatusRejected() {
        // Arrange
        PhenomenonType qualType = new PhenomenonType("Blood Group",
                MeasurementKind.QUALITATIVE);
        Phenomenon ph = new Phenomenon("Blood Group A", qualType);

        CategoryObservation co = new CategoryObservation(
                patient, ph, Presence.PRESENT,
                LocalDateTime.now(), LocalDateTime.now(), null);

        RejectObservationCommand cmd = new RejectObservationCommand(
                co, "Incorrect entry", observationRepository, objectMapper);

        // Act
        cmd.execute();

        // Assert
        assertEquals(ObservationStatus.REJECTED, co.getStatus());
        assertEquals("Incorrect entry", co.getRejectionReason());
        verify(observationRepository).save(co);
    }
}