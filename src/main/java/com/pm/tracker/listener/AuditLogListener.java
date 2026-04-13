package com.pm.tracker.listener;

import com.pm.tracker.access.AuditLogRepository;
import com.pm.tracker.event.ObservationRejectedEvent;
import com.pm.tracker.event.ObservationSavedEvent;
import com.pm.tracker.model.log.AuditLogEntry;
import com.pm.tracker.model.operational.Observation;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Component
public class AuditLogListener {

    private final AuditLogRepository auditLogRepository;

    public AuditLogListener(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @EventListener
    public void onObservationSaved(ObservationSavedEvent event) {
        Observation obs = event.getObservation();
        AuditLogEntry entry = new AuditLogEntry(
                "OBSERVATION_SAVED",
                obs.getId(),
                obs.getPatient().getId(),
                "Type: " + obs.getClass().getSimpleName()
                        + " | Status: " + obs.getStatus()
        );
        auditLogRepository.save(entry);
    }

    @EventListener
    public void onObservationRejected(ObservationRejectedEvent event) {
        Observation obs = event.getObservation();
        AuditLogEntry entry = new AuditLogEntry(
                "OBSERVATION_REJECTED",
                obs.getId(),
                obs.getPatient().getId(),
                "Reason: " + event.getRejectionReason()
        );
        auditLogRepository.save(entry);
    }
}