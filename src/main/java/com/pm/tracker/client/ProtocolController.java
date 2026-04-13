package com.pm.tracker.client;

import com.pm.tracker.access.ProtocolRepository;
import com.pm.tracker.model.knowledge.AccuracyRating;
import com.pm.tracker.model.knowledge.Protocol;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ProtocolController — Layer 1 Client.
 * CRUD for Protocols (knowledge level).
 */
@RestController
@RequestMapping("/api/protocols")
public class ProtocolController {

    private final ProtocolRepository protocolRepository;

    public ProtocolController(ProtocolRepository protocolRepository) {
        this.protocolRepository = protocolRepository;
    }

    // GET /api/protocols
    @GetMapping
    public List<Protocol> listAll() {
        return protocolRepository.findAll();
    }

    // POST /api/protocols
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        try {
            String name           = body.get("name");
            String description    = body.get("description");
            AccuracyRating rating = AccuracyRating.valueOf(
                    body.get("accuracyRating").toUpperCase());

            Protocol protocol = new Protocol(name, description, rating);
            protocolRepository.save(protocol);
            return ResponseEntity.ok(protocol);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/protocols/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        return protocolRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}