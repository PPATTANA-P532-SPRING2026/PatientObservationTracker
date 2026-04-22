package com.pm.tracker.client;

import com.pm.tracker.manager.ObservationManager;
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

    public ObservationController(ObservationManager observationManager) {
        this.observationManager = observationManager;
    }

    @GetMapping("/api/patients/{id}/observations")
    public ResponseEntity<?> listForPatient(@PathVariable UUID id) {
        try {
            List<Observation> obs = observationManager.listForPatient(id);
            return ResponseEntity.ok(obs);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/observations/measurement")
    public ResponseEntity<?> recordMeasurement(
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Current-User",
                    defaultValue = "staff") String currentUser) {
        try {
            UUID patientId        = UUID.fromString(body.get("patientId"));
            UUID phenomenonTypeId = UUID.fromString(body.get("phenomenonTypeId"));
            Double amount         = Double.parseDouble(body.get("amount"));
            String unit           = body.get("unit");
            UUID protocolId       = body.get("protocolId") != null
                    ? UUID.fromString(body.get("protocolId"))
                    : null;
            LocalDateTime applicabilityTime =
                    body.get("applicabilityTime") != null
                            ? LocalDateTime.parse(body.get("applicabilityTime"))
                            : null;

            return ResponseEntity.ok(
                    observationManager.recordMeasurement(
                            patientId, phenomenonTypeId, amount, unit,
                            applicabilityTime, protocolId, currentUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/observations/category")
    public ResponseEntity<?> recordCategory(
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Current-User",
                    defaultValue = "staff") String currentUser) {
        try {
            UUID patientId    = UUID.fromString(body.get("patientId"));
            UUID phenomenonId = UUID.fromString(body.get("phenomenonId"));
            Presence presence = Presence.valueOf(
                    body.get("presence").toUpperCase());
            UUID protocolId   = body.get("protocolId") != null
                    ? UUID.fromString(body.get("protocolId"))
                    : null;
            LocalDateTime applicabilityTime =
                    body.get("applicabilityTime") != null
                            ? LocalDateTime.parse(body.get("applicabilityTime"))
                            : null;

            return ResponseEntity.ok(
                    observationManager.recordCategoryObservation(
                            patientId, phenomenonId, presence,
                            applicabilityTime, protocolId, currentUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/observations/{id}/reject")
    public ResponseEntity<?> reject(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Current-User",
                    defaultValue = "staff") String currentUser) {
        try {
            String reason = body.getOrDefault("reason", "No reason provided");
            return ResponseEntity.ok(
                    observationManager.rejectObservation(id, reason, currentUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}