package com.pm.tracker.handler;

import com.pm.tracker.model.knowledge.MeasurementKind;
import com.pm.tracker.model.knowledge.Phenomenon;
import com.pm.tracker.model.knowledge.PhenomenonType;
import com.pm.tracker.model.operational.Patient;
import com.pm.tracker.model.operational.Presence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DecoratorChainTest {

    private Patient patient;
    private PhenomenonType quantType;
    private PhenomenonType qualType;
    private Phenomenon phenomenon;

    @BeforeEach
    void setUp() {
        // Arrange — shared fixtures
        patient = new Patient("John Smith",
                LocalDate.of(1980, 1, 1), "test");

        quantType = new PhenomenonType("Blood Glucose",
                MeasurementKind.QUANTITATIVE);
        quantType.setAllowedUnits(Set.of("mg/dL"));
        quantType.setNormalMin(70.0);
        quantType.setNormalMax(140.0);

        qualType   = new PhenomenonType("Blood Group",
                MeasurementKind.QUALITATIVE);
        phenomenon = new Phenomenon("Blood Group A", qualType);
    }

    @Test
    void unitValidationDecorator_invalidUnit_throwsValidationException() {
        // Arrange
        ObservationProcessor chain = new UnitValidationDecorator(
                new BaseObservationProcessor());

        ObservationRequest request = new ObservationRequest();
        request.setPatient(patient);
        request.setPhenomenonType(quantType);
        request.setAmount(100.0);
        request.setUnit("Kelvin");   // not in allowedUnits

        // Act + Assert
        assertThrows(ValidationException.class,
                () -> chain.process(request));
    }

    @Test
    void unitValidationDecorator_validUnit_passes() {
        // Arrange
        ObservationProcessor chain = new UnitValidationDecorator(
                new BaseObservationProcessor());

        ObservationRequest request = new ObservationRequest();
        request.setPatient(patient);
        request.setPhenomenonType(quantType);
        request.setAmount(100.0);
        request.setUnit("mg/dL");    // valid unit

        // Act + Assert
        assertDoesNotThrow(() -> chain.process(request));
    }

    @Test
    void anomalyFlaggingDecorator_valueOutsideRange_flagsRequest() {
        // Arrange — normal range 70-140, value 200 is anomalous
        ObservationProcessor chain = new AnomalyFlaggingDecorator(
                new BaseObservationProcessor());

        ObservationRequest request = new ObservationRequest();
        request.setPatient(patient);
        request.setPhenomenonType(quantType);
        request.setAmount(200.0);
        request.setUnit("mg/dL");

        // Act
        ObservationRequest result = chain.process(request);

        // Assert
        assertTrue(result.isAnomalyFlagged());
        assertNotNull(result.getAnomalyDetail());
    }

    @Test
    void anomalyFlaggingDecorator_valueInsideRange_doesNotFlag() {
        // Arrange — value 100 is within 70-140
        ObservationProcessor chain = new AnomalyFlaggingDecorator(
                new BaseObservationProcessor());

        ObservationRequest request = new ObservationRequest();
        request.setPatient(patient);
        request.setPhenomenonType(quantType);
        request.setAmount(100.0);
        request.setUnit("mg/dL");

        // Act
        ObservationRequest result = chain.process(request);

        // Assert
        assertFalse(result.isAnomalyFlagged());
    }

    @Test
    void auditStampingDecorator_attachesTimestampAndUser() {
        // Arrange
        ObservationProcessor chain = new AuditStampingDecorator(
                new BaseObservationProcessor(), "nurse1");

        ObservationRequest request = new ObservationRequest();
        request.setPatient(patient);
        request.setPhenomenonType(quantType);
        request.setAmount(100.0);
        request.setUnit("mg/dL");

        // Act
        ObservationRequest result = chain.process(request);

        // Assert
        assertNotNull(result.getRecordingTime());
        assertEquals("nurse1", result.getActingUser());
    }
}