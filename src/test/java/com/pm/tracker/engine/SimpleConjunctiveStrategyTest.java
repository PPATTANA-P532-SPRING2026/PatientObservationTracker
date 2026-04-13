package com.pm.tracker.engine;

import com.pm.tracker.model.knowledge.AssociativeFunction;
import com.pm.tracker.model.knowledge.MeasurementKind;
import com.pm.tracker.model.knowledge.Phenomenon;
import com.pm.tracker.model.knowledge.PhenomenonType;
import com.pm.tracker.model.operational.CategoryObservation;
import com.pm.tracker.model.operational.Measurement;
import com.pm.tracker.model.operational.Patient;
import com.pm.tracker.model.operational.Presence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class SimpleConjunctiveStrategyTest {

    private SimpleConjunctiveStrategy strategy;
    private Patient patient;

    private PhenomenonType thirstType;
    private PhenomenonType polyuriaType;
    private PhenomenonType diabetesType;
    private PhenomenonType glucoseType;

    private final LocalDateTime recordedAt = LocalDateTime.of(2026, 4, 13, 12, 0);
    private final LocalDateTime applicableAt = LocalDateTime.of(2026, 4, 13, 12, 0);

    @BeforeEach
    void setUp() {
        // Arrange — shared fixtures
        strategy = new SimpleConjunctiveStrategy();
        patient = new Patient("Jane Doe", LocalDate.of(1990, 6, 15), "test");

        thirstType = type("Thirst", MeasurementKind.QUALITATIVE);
        polyuriaType = type("Polyuria", MeasurementKind.QUALITATIVE);
        diabetesType = type("Diabetes", MeasurementKind.QUALITATIVE);
        glucoseType = type("Blood Glucose", MeasurementKind.QUANTITATIVE);
        glucoseType.setAllowedUnits(Set.of("mg/dL"));
    }

    @Test
    void evaluate_allArgumentsPresent_returnsTrue() {
        // Arrange
        Phenomenon thirst = new Phenomenon("Thirst Present", thirstType);
        Phenomenon polyuria = new Phenomenon("Polyuria Present", polyuriaType);

        CategoryObservation obs1 = categoryObs(patient, thirst, Presence.PRESENT);
        CategoryObservation obs2 = categoryObs(patient, polyuria, Presence.PRESENT);

        AssociativeFunction rule = new AssociativeFunction(
                "Diabetes Rule",
                List.of(thirstType, polyuriaType),
                diabetesType
        );

        // Act
        boolean result = strategy.evaluate(rule, List.of(obs1, obs2));

        // Assert
        assertTrue(result);
    }

    @Test
    void evaluate_oneArgumentMissing_returnsFalse() {
        // Arrange
        Phenomenon thirst = new Phenomenon("Thirst Present", thirstType);
        CategoryObservation obs1 = categoryObs(patient, thirst, Presence.PRESENT);

        AssociativeFunction rule = new AssociativeFunction(
                "Diabetes Rule",
                List.of(thirstType, polyuriaType),
                diabetesType
        );

        // Act
        boolean result = strategy.evaluate(rule, List.of(obs1));

        // Assert
        assertFalse(result);
    }

    @Test
    void evaluate_argumentPresentButAbsent_returnsFalse() {
        // Arrange
        Phenomenon thirst = new Phenomenon("Thirst", thirstType);
        Phenomenon polyuria = new Phenomenon("Polyuria", polyuriaType);

        CategoryObservation obs1 = categoryObs(patient, thirst, Presence.ABSENT);
        CategoryObservation obs2 = categoryObs(patient, polyuria, Presence.PRESENT);

        AssociativeFunction rule = new AssociativeFunction(
                "Diabetes Rule",
                List.of(thirstType, polyuriaType),
                diabetesType
        );

        // Act
        boolean result = strategy.evaluate(rule, List.of(obs1, obs2));

        // Assert
        assertFalse(result);
    }

    @Test
    void evaluate_emptyObservations_returnsFalse() {
        // Arrange
        AssociativeFunction rule = new AssociativeFunction(
                "Diabetes Rule",
                List.of(thirstType, polyuriaType),
                diabetesType
        );

        // Act
        boolean result = strategy.evaluate(rule, new ArrayList<>());

        // Assert
        assertFalse(result);
    }

    @Test
    void evaluate_measurementSatisfiesQuantitativeConcept_returnsTrue() {
        // Arrange
        Measurement m = new Measurement(
                patient,
                glucoseType,
                145.0,
                "mg/dL",
                recordedAt,
                applicableAt,
                null
        );

        AssociativeFunction rule = new AssociativeFunction(
                "High Glucose Rule",
                List.of(glucoseType),
                diabetesType
        );

        // Act
        boolean result = strategy.evaluate(rule, List.of(m));

        // Assert
        assertTrue(result);
    }

    private PhenomenonType type(String name, MeasurementKind kind) {
        PhenomenonType type = new PhenomenonType(name, kind);
        setId(type, UUID.randomUUID());
        return type;
    }

    private void setId(PhenomenonType type, UUID id) {
        try {
            Field field = PhenomenonType.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(type, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to set PhenomenonType id for test", e);
        }
    }

    private CategoryObservation categoryObs(Patient p, Phenomenon ph, Presence presence) {
        return new CategoryObservation(p, ph, presence, recordedAt, applicableAt, null);
    }
}