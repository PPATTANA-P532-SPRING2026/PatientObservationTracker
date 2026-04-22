package com.pm.tracker.engine;

import com.pm.tracker.model.knowledge.AssociativeFunction;
import com.pm.tracker.model.knowledge.MeasurementKind;
import com.pm.tracker.model.knowledge.Phenomenon;
import com.pm.tracker.model.knowledge.PhenomenonType;
import com.pm.tracker.model.operational.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WeightedScoringStrategyTest {

    private WeightedScoringStrategy strategy;
    private Patient patient;

    private PhenomenonType thirstType;
    private PhenomenonType polyuriaType;
    private PhenomenonType weightLossType;
    private PhenomenonType diabetesType;

    @BeforeEach
    void setUp() {
        // Arrange — shared fixtures
        strategy = new WeightedScoringStrategy();
        patient  = new Patient("Jane Doe",
                LocalDate.of(1990, 6, 15), "test");

        thirstType     = new PhenomenonType("Thirst",
                MeasurementKind.QUALITATIVE);
        polyuriaType   = new PhenomenonType("Polyuria",
                MeasurementKind.QUALITATIVE);
        weightLossType = new PhenomenonType("Weight Loss",
                MeasurementKind.QUALITATIVE);
        diabetesType   = new PhenomenonType("Diabetes",
                MeasurementKind.QUALITATIVE);
    }

    @Test
    void evaluate_scoreMeetsThreshold_returnsTrue() {
        // Arrange
        Phenomenon thirst   = new Phenomenon("Thirst Present",   thirstType);
        Phenomenon polyuria = new Phenomenon("Polyuria Present",  polyuriaType);

        CategoryObservation obs1 = categoryObs(thirst,   Presence.PRESENT);
        CategoryObservation obs2 = categoryObs(polyuria, Presence.PRESENT);

        AssociativeFunction rule = new AssociativeFunction(
                "Diabetes Rule",
                List.of(thirstType, polyuriaType, weightLossType),
                diabetesType);
        rule.setThreshold(0.7);
        rule.setArgumentWeights(Map.of(
                thirstType.getName(),     0.4,
                polyuriaType.getName(),   0.4,
                weightLossType.getName(), 0.3
        ));

        // Act — score = 0.4 + 0.4 = 0.8 >= 0.7
        boolean result = strategy.evaluate(rule, List.of(obs1, obs2));

        // Assert
        assertTrue(result);
    }

    @Test
    void evaluate_scoreBelowThreshold_returnsFalse() {
        // Arrange — only weight loss present, score = 0.3 < 0.7
        Phenomenon weightLoss = new Phenomenon("Weight Loss", weightLossType);
        CategoryObservation obs = categoryObs(weightLoss, Presence.PRESENT);

        AssociativeFunction rule = new AssociativeFunction(
                "Diabetes Rule",
                List.of(thirstType, polyuriaType, weightLossType),
                diabetesType);
        rule.setThreshold(0.7);
        rule.setArgumentWeights(Map.of(
                thirstType.getName(),     0.4,
                polyuriaType.getName(),   0.4,
                weightLossType.getName(), 0.3
        ));

        // Act
        boolean result = strategy.evaluate(rule, List.of(obs));

        // Assert
        assertFalse(result);
    }

    @Test
    void evaluate_partialEvidence_scoresCorrectly() {
        // Arrange — two concepts present, one absent
        Phenomenon thirst     = new Phenomenon("Thirst",     thirstType);
        Phenomenon weightLoss = new Phenomenon("Weight Loss", weightLossType);

        CategoryObservation obs1 = categoryObs(thirst,     Presence.PRESENT);
        CategoryObservation obs2 = categoryObs(weightLoss, Presence.PRESENT);

        AssociativeFunction rule = new AssociativeFunction(
                "Diabetes Rule",
                List.of(thirstType, polyuriaType, weightLossType),
                diabetesType);
        rule.setThreshold(0.5);
        rule.setArgumentWeights(Map.of(
                thirstType.getName(),     0.3,
                polyuriaType.getName(),   0.4,
                weightLossType.getName(), 0.3
        ));

        // Act — score = 0.3 + 0.3 = 0.6 >= 0.5
        boolean result = strategy.evaluate(rule, List.of(obs1, obs2));

        // Assert
        assertTrue(result);
    }

    @Test
    void evaluate_inferredObservationsExcluded_returnsFalse() {
        // Arrange — observation is INFERRED, should not count as evidence
        Phenomenon thirst   = new Phenomenon("Thirst",   thirstType);
        Phenomenon polyuria = new Phenomenon("Polyuria",  polyuriaType);

        CategoryObservation inferred = categoryObs(thirst, Presence.PRESENT);
        inferred.setSource(ObservationSource.INFERRED);

        CategoryObservation manual = categoryObs(polyuria, Presence.PRESENT);
        manual.setSource(ObservationSource.MANUAL);

        AssociativeFunction rule = new AssociativeFunction(
                "Diabetes Rule",
                List.of(thirstType, polyuriaType),
                diabetesType);
        rule.setThreshold(0.7);
        rule.setArgumentWeights(Map.of(
                thirstType.getName(),   0.4,
                polyuriaType.getName(), 0.4
        ));

        // Act — inferred thirst excluded, score = 0.4 < 0.7
        boolean result = strategy.evaluate(rule, List.of(inferred, manual));

        // Assert
        assertFalse(result);
    }

    // ── helper ────────────────────────────────────────────────────────
    private CategoryObservation categoryObs(Phenomenon ph, Presence presence) {
        CategoryObservation co = new CategoryObservation(
                patient, ph, presence,
                LocalDateTime.now(), LocalDateTime.now(), null);
        co.setSource(ObservationSource.MANUAL);
        return co;
    }
}