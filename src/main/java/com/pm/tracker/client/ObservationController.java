package com.pm.tracker.client;

import com.pm.tracker.manager.ObservationManager;
import com.pm.tracker.manager.PatientManager;
import com.pm.tracker.model.operational.Observation;
import com.pm.tracker.model.operational.Presence;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
public class ObservationController {

    private final ObservationManager observationManager;
    private final PatientManager patientManager;

    public ObservationController(ObservationManager observationManager,
                                 PatientManager patientManager) {
        this.observationManager = observationManager;
        this.patientManager     = patientManager;
    }

    // GET /api/patients/{id}/observations
    @GetMapping("/api/patients/{id}/observations")
    public ResponseEntity<?> listForPatient(@PathVariable UUID id) {
        try {
            List<Observation> obs = observationManager.listForPatient(id);
            return ResponseEntity.ok(obs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/observations/measurement
    @PostMapping("/api/observations/measurement")
    public ResponseEntity<?> recordMeasurement(@RequestBody Map<String, String> body) {
        try {
            UUID patientId        = UUID.fromString(body.get("patientId"));
            UUID phenomenonTypeId = UUID.fromString(body.get("phenomenonTypeId"));
            Double amount         = Double.parseDouble(body.get("amount"));
            String unit           = body.get("unit");
            UUID protocolId       = body.get("protocolId") != null
                    ? UUID.fromString(body.get("protocolId")) : null;
            LocalDateTime applicabilityTime = body.get("applicabilityTime") != null
                    ? LocalDateTime.parse(body.get("applicabilityTime"))
                    : null;

            return ResponseEntity.ok(observationManager.recordMeasurement(
                    patientId, phenomenonTypeId, amount, unit,
                    applicabilityTime, protocolId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/observations/category
    @PostMapping("/api/observations/category")
    public ResponseEntity<?> recordCategory(@RequestBody Map<String, String> body) {
        try {
            UUID patientId    = UUID.fromString(body.get("patientId"));
            UUID phenomenonId = UUID.fromString(body.get("phenomenonId"));
            Presence presence = Presence.valueOf(body.get("presence").toUpperCase());
            UUID protocolId   = body.get("protocolId") != null
                    ? UUID.fromString(body.get("protocolId")) : null;
            LocalDateTime applicabilityTime = body.get("applicabilityTime") != null
                    ? LocalDateTime.parse(body.get("applicabilityTime"))
                    : null;

            return ResponseEntity.ok(observationManager.recordCategoryObservation(
                    patientId, phenomenonId, presence,
                    applicabilityTime, protocolId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/observations/{id}/reject
    @PostMapping("/api/observations/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable UUID id,
                                    @RequestBody Map<String, String> body) {
        try {
            String reason = body.getOrDefault("reason", "No reason provided");
            return ResponseEntity.ok(
                    observationManager.rejectObservation(id, reason));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}