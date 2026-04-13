package com.pm.tracker.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.tracker.access.PatientRepository;
import com.pm.tracker.command.CommandLog;
import com.pm.tracker.command.CreatePatientCommand;
import com.pm.tracker.model.operational.Patient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * PatientManager — Layer 2 Manager.
 * Orchestrates patient-related use cases.
 * All persistence goes through commands via CommandLog.
 */
@Service
public class PatientManager {

    private final PatientRepository patientRepository;
    private final CommandLog commandLog;
    private final ObjectMapper objectMapper;

    public PatientManager(PatientRepository patientRepository,
                          CommandLog commandLog,
                          ObjectMapper objectMapper) {
        this.patientRepository = patientRepository;
        this.commandLog        = commandLog;
        this.objectMapper      = objectMapper;
    }

    // ── Create ────────────────────────────────────────────────────────
    public Patient createPatient(String fullName,
                                 LocalDate dateOfBirth,
                                 String note) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Patient full name cannot be blank.");
        }
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Date of birth cannot be null.");
        }

        Patient patient = new Patient(fullName, dateOfBirth, note);

        CreatePatientCommand cmd = new CreatePatientCommand(
                patient, patientRepository, objectMapper);
        commandLog.execute(cmd);

        return patient;
    }

    // ── Query ─────────────────────────────────────────────────────────
    public List<Patient> listAll() {
        return patientRepository.findAll();
    }

    public Patient findById(UUID id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Patient not found: " + id));
    }
}