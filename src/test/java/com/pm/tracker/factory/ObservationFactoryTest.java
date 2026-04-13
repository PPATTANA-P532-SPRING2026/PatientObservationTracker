package com.pm.tracker.factory;

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

        quantType = new PhenomenonType("Body Temperature", MeasurementKind.QUANTITATIVE);
        quantType.setAllowedUnits(Set.of("Celsius", "Fahrenheit"));

        qualType = new PhenomenonType("Blood Group", MeasurementKind.QUALITATIVE);
        phenomenon = new Phenomenon("Blood Group A", qualType);
    }

    // ── Measurement tests ─────────────────────────────────────────────

    @Test
    void createMeasurement_validInput_returnsMeasurement() {
        // Arrange
        Double amount = 38.5;
        String unit   = "Celsius";

        // Act
        Measurement m = factory.createMeasurement(
                patient, quantType, amount, unit, null, null);

        // Assert
        assertNotNull(m);
        assertEquals(patient,   m.getPatient());
        assertEquals(quantType, m.getPhenomenonType());
        assertEquals(38.5,      m.getAmount());
        assertEquals("Celsius", m.getUnit());
        assertEquals(ObservationStatus.ACTIVE, m.getStatus());
        assertNotNull(m.getRecordingTime());
    }

    @Test
    void createMeasurement_qualitativeType_throwsIllegalArgument() {
        // Arrange — passing a QUALITATIVE type to createMeasurement

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () ->
                factory.createMeasurement(patient, qualType, 1.0,
                        "Celsius", null, null));
    }

    @Test
    void createMeasurement_invalidUnit_throwsIllegalArgument() {
        // Arrange — "Kelvin" is not in allowedUnits

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () ->
                factory.createMeasurement(patient, quantType, 38.5,
                        "Kelvin", null, null));
    }

    @Test
    void createMeasurement_applicabilityTimeDefaultsToNow() {
        // Arrange + Act
        Measurement m = factory.createMeasurement(
                patient, quantType, 37.0, "Celsius", null, null);

        // Assert — applicabilityTime should be very close to now
        assertNotNull(m.getApplicabilityTime());
        assertTrue(m.getApplicabilityTime()
                .isAfter(LocalDateTime.now().minusSeconds(5)));
    }

    // ── CategoryObservation tests ─────────────────────────────────────

    @Test
    void createCategoryObservation_validInput_returnsCategoryObs() {
        // Arrange + Act
        CategoryObservation co = factory.createCategoryObservation(
                patient, phenomenon, Presence.PRESENT, null, null);

        // Assert
        assertNotNull(co);
        assertEquals(patient,    co.getPatient());
        assertEquals(phenomenon, co.getPhenomenon());
        assertEquals(Presence.PRESENT, co.getPresence());
        assertEquals(ObservationStatus.ACTIVE, co.getStatus());
    }

    @Test
    void createCategoryObservation_quantitativePhenomenon_throwsIllegalArgument() {
        // Arrange — phenomenon whose type is QUANTITATIVE
        Phenomenon badPhenomenon = new Phenomenon("Bad", quantType);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () ->
                factory.createCategoryObservation(
                        patient, badPhenomenon, Presence.PRESENT, null, null));
    }
}