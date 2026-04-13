package com.pm.tracker.access;

import com.pm.tracker.model.operational.Observation;
import com.pm.tracker.model.operational.ObservationStatus;
import com.pm.tracker.model.operational.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ObservationRepository extends JpaRepository<Observation, UUID> {

    List<Observation> findByPatientOrderByRecordingTimeDesc(Patient patient);

    List<Observation> findByPatientAndStatus(Patient patient, ObservationStatus status);
}