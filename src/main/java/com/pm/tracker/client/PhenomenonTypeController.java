package com.pm.tracker.client;

import com.pm.tracker.access.PhenomenonRepository;
import com.pm.tracker.access.PhenomenonTypeRepository;
import com.pm.tracker.model.knowledge.MeasurementKind;
import com.pm.tracker.model.knowledge.Phenomenon;
import com.pm.tracker.model.knowledge.PhenomenonType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * PhenomenonTypeController — Layer 1 Client.
 * CRUD for PhenomenonType and Phenomenon (knowledge level).
 * Change 2: accepts normalMin/normalMax for anomaly detection.
 * Change 4: accepts parentConceptId when adding a phenomenon.
 */
@RestController
@RequestMapping("/api/phenomenon-types")
public class PhenomenonTypeController {

    private final PhenomenonTypeRepository phenomenonTypeRepository;
    private final PhenomenonRepository phenomenonRepository;

    public PhenomenonTypeController(
            PhenomenonTypeRepository phenomenonTypeRepository,
            PhenomenonRepository phenomenonRepository) {
        this.phenomenonTypeRepository = phenomenonTypeRepository;
        this.phenomenonRepository     = phenomenonRepository;
    }

    // GET /api/phenomenon-types
    @GetMapping
    public List<PhenomenonType> listAll() {
        return phenomenonTypeRepository.findAll();
    }

    // GET /api/phenomenon-types/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        return phenomenonTypeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/phenomenon-types
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            String name = (String) body.get("name");
            MeasurementKind kind = MeasurementKind.valueOf(
                    ((String) body.get("kind")).toUpperCase());

            PhenomenonType pt = new PhenomenonType(name, kind);

            // allowedUnits for QUANTITATIVE
            if (body.containsKey("allowedUnits")) {
                @SuppressWarnings("unchecked")
                List<String> units = (List<String>) body.get("allowedUnits");
                pt.setAllowedUnits(new HashSet<>(units));
            }

            // Change 2 — normal range for AnomalyFlaggingDecorator
            if (body.containsKey("normalMin")) {
                pt.setNormalMin(((Number) body.get("normalMin")).doubleValue());
            }
            if (body.containsKey("normalMax")) {
                pt.setNormalMax(((Number) body.get("normalMax")).doubleValue());
            }

            phenomenonTypeRepository.save(pt);

            // phenomena for QUALITATIVE
            if (body.containsKey("phenomena")) {
                @SuppressWarnings("unchecked")
                List<String> phenNames = (List<String>) body.get("phenomena");
                for (String pname : phenNames) {
                    Phenomenon p = new Phenomenon(pname, pt);
                    phenomenonRepository.save(p);
                }
                pt = phenomenonTypeRepository.findById(pt.getId()).orElse(pt);
            }

            return ResponseEntity.ok(pt);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/phenomenon-types/{id}/phenomena
    // Change 4 — accepts optional parentConceptId
    @PostMapping("/{id}/phenomena")
    public ResponseEntity<?> addPhenomenon(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        try {
            PhenomenonType pt = phenomenonTypeRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "PhenomenonType not found: " + id));

            if (pt.getKind() != MeasurementKind.QUALITATIVE) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Can only add phenomena to QUALITATIVE types"));
            }

            Phenomenon p = new Phenomenon(body.get("name"), pt);

            // Change 4 — optional parent concept for hierarchy
            if (body.containsKey("parentConceptId")
                    && body.get("parentConceptId") != null
                    && !body.get("parentConceptId").isBlank()) {
                UUID parentId = UUID.fromString(body.get("parentConceptId"));
                phenomenonRepository.findById(parentId)
                        .ifPresent(p::setParentConcept);
            }

            phenomenonRepository.save(p);
            return ResponseEntity.ok(p);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/phenomenon-types/{id}/phenomena
    @GetMapping("/{id}/phenomena")
    public ResponseEntity<?> getPhenomena(@PathVariable UUID id) {
        try {
            PhenomenonType pt = phenomenonTypeRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "PhenomenonType not found: " + id));
            return ResponseEntity.ok(
                    phenomenonRepository.findByPhenomenonType(pt));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}