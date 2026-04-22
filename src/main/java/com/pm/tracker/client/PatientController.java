package com.pm.tracker.client;

import com.pm.tracker.manager.PatientManager;
import com.pm.tracker.model.operational.Patient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientManager patientManager;

    public PatientController(PatientManager patientManager) {
        this.patientManager = patientManager;
    }

    @GetMapping
    public List<Patient> listAll() {
        return patientManager.listAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(patientManager.findById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Current-User",
                    defaultValue = "staff") String currentUser) {
        try {
            String fullName = body.get("fullName");
            LocalDate dob   = LocalDate.parse(body.get("dateOfBirth"));
            String note     = body.get("note");
            Patient patient = patientManager.createPatient(
                    fullName, dob, note, currentUser);
            return ResponseEntity.ok(patient);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}