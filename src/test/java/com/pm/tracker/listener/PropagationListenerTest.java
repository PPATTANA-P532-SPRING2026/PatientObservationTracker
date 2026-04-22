package com.pm.tracker.listener;

import com.pm.tracker.access.ObservationRepository;
import com.pm.tracker.access.PhenomenonRepository;
import com.pm.tracker.event.ObservationSavedEvent;
import com.pm.tracker.factory.ObservationFactory;
import com.pm.tracker.model.knowledge.MeasurementKind;
import com.pm.tracker.model.knowledge.Phenomenon;
import com.pm.tracker.model.knowledge.PhenomenonType;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PropagationListenerTest {

    @Mock private ObservationRepository observationRepository;
    @Mock private PhenomenonRepository  phenomenonRepository;

    private PropagationListener listener;
    private ObservationFactory factory;
    private Patient patient;

    private PhenomenonType qualType;
    private Phenomenon parent;
    private Phenomenon child;

    @BeforeEach
    void setUp() {
        // Arrange — shared fixtures
        factory  = new ObservationFactory();
        listener = new PropagationListener(
                factory, observationRepository, phenomenonRepository);

        patient  = new Patient("Bob",
                LocalDate.of(1975, 5, 20), "");

        qualType = new PhenomenonType("Diabetes Type",
                MeasurementKind.QUALITATIVE);

        parent = new Phenomenon("Diabetes",          qualType);
        child  = new Phenomenon("Type I Diabetes",   qualType);
        child.setParentConcept(parent);

        // default stubs
        when(observationRepository.findByPatientOrderByRecordingTimeDesc(any()))
                .thenReturn(List.of());
        when(phenomenonRepository.findByParentConcept(any()))
                .thenReturn(List.of());
    }

    @Test
    void presentObservation_propagatesPresentToAncestor() {
        // Arrange — child is PRESENT, parent should be inferred PRESENT
        CategoryObservation co = new CategoryObservation(
                patient, child, Presence.PRESENT,
                LocalDateTime.now(), LocalDateTime.now(), null);
        co.setSource(ObservationSource.MANUAL);

        ObservationSavedEvent event = new ObservationSavedEvent(co);

        // Act
        listener.onObservationSaved(event);

        // Assert — an inferred observation was saved for the parent
        ArgumentCaptor<CategoryObservation> captor =
                ArgumentCaptor.forClass(CategoryObservation.class);
        verify(observationRepository, atLeastOnce()).save(captor.capture());

        CategoryObservation saved = captor.getAllValues().stream()
                .filter(o -> o.getSource() == ObservationSource.INFERRED)
                .findFirst()
                .orElse(null);

        assertNotNull(saved);
        assertEquals(Presence.PRESENT,             saved.getPresence());
        assertEquals(ObservationSource.INFERRED,   saved.getSource());
        assertEquals(parent.getName(),
                saved.getPhenomenon().getName());
    }

    @Test
    void absentObservation_propagatesAbsentToDescendants() {
        // Arrange — parent is ABSENT, child should be inferred ABSENT
        when(phenomenonRepository.findByParentConcept(parent))
                .thenReturn(List.of(child));
        when(phenomenonRepository.findByParentConcept(child))
                .thenReturn(List.of());

        CategoryObservation co = new CategoryObservation(
                patient, parent, Presence.ABSENT,
                LocalDateTime.now(), LocalDateTime.now(), null);
        co.setSource(ObservationSource.MANUAL);

        ObservationSavedEvent event = new ObservationSavedEvent(co);

        // Act
        listener.onObservationSaved(event);

        // Assert — inferred ABSENT saved for child
        ArgumentCaptor<CategoryObservation> captor =
                ArgumentCaptor.forClass(CategoryObservation.class);
        verify(observationRepository, atLeastOnce()).save(captor.capture());

        CategoryObservation saved = captor.getAllValues().stream()
                .filter(o -> o.getSource() == ObservationSource.INFERRED)
                .findFirst()
                .orElse(null);

        assertNotNull(saved);
        assertEquals(Presence.ABSENT,            saved.getPresence());
        assertEquals(ObservationSource.INFERRED, saved.getSource());
        assertEquals(child.getName(),
                saved.getPhenomenon().getName());
    }

    @Test
    void inferredObservation_doesNotPropagate() {
        // Arrange — INFERRED observation should not trigger propagation
        CategoryObservation co = new CategoryObservation(
                patient, child, Presence.PRESENT,
                LocalDateTime.now(), LocalDateTime.now(), null);
        co.setSource(ObservationSource.INFERRED);  // already inferred

        ObservationSavedEvent event = new ObservationSavedEvent(co);

        // Act
        listener.onObservationSaved(event);

        // Assert — no new observations saved
        verify(observationRepository, never()).save(any());
    }
}