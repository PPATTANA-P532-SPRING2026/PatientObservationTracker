package com.pm.tracker.factory;

import com.pm.tracker.handler.ObservationRequest;
import com.pm.tracker.handler.ValidationException;
import com.pm.tracker.handler.UnitValidationDecorator;
import com.pm.tracker.handler.BaseObservationProcessor;
import com.pm.tracker.model.knowledge.MeasurementKind;
import com.pm.tracker.model.knowledge.Phenomenon;
import com.pm.tracker.model.knowledge.PhenomenonType;
import com.pm.tracker.model.operational.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ObservationFactoryTest {

    private ObservationFactory factory;
    private Patient patient;
    private PhenomenonType quantType;
    private PhenomenonType qualType;
    private Phenomenon phenomenon;

    @BeforeEach
    void setUp() {
        // Arrange — shared fixtures
        factory = new ObservationFactory();

        patient = new Patient("John Smith",
                LocalDate.of(1980, 1, 1), "test patient");

        quantType = new PhenomenonType("Body Temperature",
                MeasurementKind.QUANTITATIVE);
        quantType.setAllowedUnits(Set.of("Celsius", "Fahrenheit"));

        qualType   = new PhenomenonType("Blood Group",
                MeasurementKind.QUALITATIVE);
        phenomenon = new Phenomenon("Blood Group A", qualType);
    }

    // ── helpers ───────────────────────────────────────────────────────

    private ObservationRequest measurementRequest(PhenomenonType pt,
                                                  Double amount,
                                                  String unit) {
        ObservationRequest r = new ObservationRequest();
        r.setPatient(patient);
        r.setPhenomenonType(pt);
        r.setAmount(amount);
        r.setUnit(unit);
        r.setRecordingTime(LocalDateTime.now());
        r.setApplicabilityTime(LocalDateTime.now());
        return r;
    }

    private ObservationRequest categoryRequest(Phenomenon ph,
                                               Presence presence) {
        ObservationRequest r = new ObservationRequest();
        r.setPatient(patient);
        r.setPhenomenon(ph);
        r.setPhenomenonType(ph.getPhenomenonType());
        r.setPresence(presence);
        r.setRecordingTime(LocalDateTime.now());
        r.setApplicabilityTime(LocalDateTime.now());
        return r;
    }

    // ── Measurement tests ─────────────────────────────────────────────

    @Test
    void createFromRequest_validMeasurement_returnsMeasurement() {
        // Arrange
        ObservationRequest request = measurementRequest(
                quantType, 38.5, "Celsius");

        // Act
        Observation obs = factory.createFromRequest(request);

        // Assert
        assertInstanceOf(Measurement.class, obs);
        Measurement m = (Measurement) obs;
        assertEquals(patient,   m.getPatient());
        assertEquals(quantType, m.getPhenomenonType());
        assertEquals(38.5,      m.getAmount());
        assertEquals("Celsius", m.getUnit());
        assertEquals(ObservationStatus.ACTIVE, m.getStatus());
        assertEquals(ObservationSource.MANUAL, m.getSource());
        assertNotNull(m.getRecordingTime());
    }

    @Test
    void createFromRequest_qualitativeTypeForMeasurement_throwsIllegalArgument() {
        // Arrange — passing a QUALITATIVE type for a measurement request
        ObservationRequest request = measurementRequest(
                qualType, 1.0, "Celsius");

        // Act + Assert
        assertThrows(IllegalArgumentException.class,
                () -> factory.createFromRequest(request));
    }

    @Test
    void unitValidationDecorator_invalidUnit_throwsValidationException() {
        // Arrange — unit validation is now in the decorator, not factory
        ObservationRequest request = measurementRequest(
                quantType, 38.5, "Kelvin");

        UnitValidationDecorator chain = new UnitValidationDecorator(
                new BaseObservationProcessor());

        // Act + Assert
        assertThrows(ValidationException.class,
                () -> chain.process(request));
    }

    @Test
    void createFromRequest_applicabilityTimeSetFromRequest() {
        // Arrange
        LocalDateTime appTime = LocalDateTime.now().minusHours(1);
        ObservationRequest request = measurementRequest(
                quantType, 37.0, "Celsius");
        request.setApplicabilityTime(appTime);

        // Act
        Observation obs = factory.createFromRequest(request);
        Measurement m   = (Measurement) obs;

        // Assert
        assertEquals(appTime, m.getApplicabilityTime());
    }

    // ── CategoryObservation tests ─────────────────────────────────────

    @Test
    void createFromRequest_validCategoryObservation_returnsCategoryObs() {
        // Arrange
        ObservationRequest request = categoryRequest(
                phenomenon, Presence.PRESENT);

        // Act
        Observation obs = factory.createFromRequest(request);

        // Assert
        assertInstanceOf(CategoryObservation.class, obs);
        CategoryObservation co = (CategoryObservation) obs;
        assertEquals(patient,         co.getPatient());
        assertEquals(phenomenon,      co.getPhenomenon());
        assertEquals(Presence.PRESENT,co.getPresence());
        assertEquals(ObservationStatus.ACTIVE,    co.getStatus());
        assertEquals(ObservationSource.MANUAL,    co.getSource());
    }

    @Test
    void createFromRequest_quantitativePhenomenon_throwsIllegalArgument() {
        // Arrange — phenomenon whose type is QUANTITATIVE
        Phenomenon badPhenomenon = new Phenomenon("Bad", quantType);
        ObservationRequest request = categoryRequest(
                badPhenomenon, Presence.PRESENT);

        // Act + Assert
        assertThrows(IllegalArgumentException.class,
                () -> factory.createFromRequest(request));
    }
}