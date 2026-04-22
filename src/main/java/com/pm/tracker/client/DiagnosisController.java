package com.pm.tracker.client;
import com.pm.tracker.model.knowledge.StrategyType;
import com.pm.tracker.access.AssociativeFunctionRepository;
import com.pm.tracker.access.PhenomenonTypeRepository;
import com.pm.tracker.engine.DiagnosisEngine;
import com.pm.tracker.manager.PatientManager;
import com.pm.tracker.model.knowledge.AssociativeFunction;
import com.pm.tracker.model.knowledge.PhenomenonType;
import com.pm.tracker.model.operational.Patient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
public class DiagnosisController {

    private final DiagnosisEngine diagnosisEngine;
    private final PatientManager patientManager;
    private final AssociativeFunctionRepository afRepository;
    private final PhenomenonTypeRepository phenomenonTypeRepository;

    public DiagnosisController(DiagnosisEngine diagnosisEngine,
                               PatientManager patientManager,
                               AssociativeFunctionRepository afRepository,
                               PhenomenonTypeRepository phenomenonTypeRepository) {
        this.diagnosisEngine          = diagnosisEngine;
        this.patientManager           = patientManager;
        this.afRepository             = afRepository;
        this.phenomenonTypeRepository = phenomenonTypeRepository;
    }

    // POST /api/patients/{id}/evaluate
    @PostMapping("/api/patients/{id}/evaluate")
    public ResponseEntity<?> evaluate(@PathVariable UUID id) {
        try {
            Patient patient = patientManager.findById(id);
            List<DiagnosisEngine.EvaluationResult> results =
                    diagnosisEngine.evaluate(patient);

            List<Map<String, Object>> response = results.stream().map(r -> {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("inferredConcept", r.getInferredConcept().getName());
                entry.put("strategyUsed",   r.getStrategyUsed());
                entry.put("evidenceCount",  r.getEvidence().size());
                return entry;
            }).toList();

            return ResponseEntity.ok(Map.of(
                    "patientId",  id,
                    "inferences", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/associative-functions
    @GetMapping("/api/associative-functions")
    public List<AssociativeFunction> listAll() {
        return afRepository.findAll();
    }

    // POST /api/associative-functions
    @PostMapping("/api/associative-functions")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            String name = (String) body.get("name");

            @SuppressWarnings("unchecked")
            List<String> argIds = (List<String>) body.get("argumentConceptIds");
            List<PhenomenonType> args = new ArrayList<>();
            for (String argId : argIds) {
                PhenomenonType pt = phenomenonTypeRepository
                        .findById(UUID.fromString(argId))
                        .orElseThrow(() -> new IllegalArgumentException(
                                "PhenomenonType not found: " + argId));
                args.add(pt);
            }

            UUID productId = UUID.fromString(
                    (String) body.get("productConceptId"));
            PhenomenonType product = phenomenonTypeRepository
                    .findById(productId)
                    .orElseThrow();

            AssociativeFunction af = new AssociativeFunction(name, args, product);

            // strategy type
            if (body.containsKey("strategyType")) {
                af.setStrategyType(StrategyType.valueOf(
                        (String) body.get("strategyType")));
            }

            // threshold for weighted strategy
            if (body.containsKey("threshold")) {
                af.setThreshold(((Number) body.get("threshold")).doubleValue());
            }

            // argument weights — map of phenomenonTypeId -> weight
            if (body.containsKey("argumentWeights")) {
                @SuppressWarnings("unchecked")
                Map<String, Number> weights =
                        (Map<String, Number>) body.get("argumentWeights");
                Map<String, Double> converted = new HashMap<>();
                weights.forEach((k, v) -> converted.put(k, v.doubleValue()));
                af.setArgumentWeights(converted);
            }

            afRepository.save(af);
            return ResponseEntity.ok(af);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}