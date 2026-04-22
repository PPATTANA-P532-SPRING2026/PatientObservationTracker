package com.pm.tracker.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.tracker.access.PatientRepository;
import com.pm.tracker.model.operational.Patient;

import java.util.Map;

public class CreatePatientCommand implements Command {

    private final Patient patient;
    private final PatientRepository patientRepository;
    private final ObjectMapper objectMapper;

    public CreatePatientCommand(Patient patient,
                                PatientRepository patientRepository,
                                ObjectMapper objectMapper) {
        this.patient           = patient;
        this.patientRepository = patientRepository;
        this.objectMapper      = objectMapper;
    }

    @Override
    public void execute() {
        patientRepository.save(patient);
    }

    @Override
    public void undo() {
        throw new UnsupportedOperationException(
                "CreatePatientCommand cannot be undone.");
    }

    @Override
    public String getCommandType() { return "CREATE_PATIENT"; }

    @Override
    public String toJson() {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "fullName",    patient.getFullName(),
                    "dateOfBirth", patient.getDateOfBirth().toString(),
                    "note",        patient.getNote() != null
                            ? patient.getNote() : ""
            ));
        } catch (Exception e) {
            return "{\"error\":\"serialization failed\"}";
        }
    }

    public Patient getPatient() { return patient; }
}