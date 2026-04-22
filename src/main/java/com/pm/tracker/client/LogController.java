package com.pm.tracker.client;

import com.pm.tracker.access.AuditLogRepository;
import com.pm.tracker.access.CommandLogRepository;
import com.pm.tracker.access.UserRepository;
import com.pm.tracker.command.CommandLog;
import com.pm.tracker.model.log.AuditLogEntry;
import com.pm.tracker.model.log.CommandLogEntry;
import com.pm.tracker.model.operational.Role;
import com.pm.tracker.model.operational.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class LogController {

    private final CommandLogRepository commandLogRepository;
    private final AuditLogRepository   auditLogRepository;
    private final CommandLog           commandLog;
    private final UserRepository       userRepository;

    public LogController(CommandLogRepository commandLogRepository,
                         AuditLogRepository auditLogRepository,
                         CommandLog commandLog,
                         UserRepository userRepository) {
        this.commandLogRepository = commandLogRepository;
        this.auditLogRepository   = auditLogRepository;
        this.commandLog           = commandLog;
        this.userRepository       = userRepository;
    }

    // GET /api/command-log
    @GetMapping("/api/command-log")
    public List<CommandLogEntry> getCommandLog() {
        return commandLogRepository.findAllByOrderByExecutedAtDesc();
    }

    // GET /api/audit-log
    @GetMapping("/api/audit-log")
    public List<AuditLogEntry> getAuditLog() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }

    // POST /api/command-log/{id}/undo
    @PostMapping("/api/command-log/{id}/undo")
    public ResponseEntity<?> undo(@PathVariable UUID id,
                                  @RequestBody Map<String, String> body) {
        try {
            String requestingUser = body.getOrDefault("user", "staff");
            commandLog.undo(id, requestingUser);
            return ResponseEntity.ok(Map.of("message", "Command undone successfully"));
        } catch (IllegalArgumentException | IllegalStateException |
                 UnsupportedOperationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // GET /health
    @GetMapping("/health")
    public String health() { return "ok"; }

    // ── User management ───────────────────────────────────────────────

    // GET /api/users
    @GetMapping("/api/users")
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    // POST /api/users
    @PostMapping("/api/users")
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            Role role       = Role.valueOf(
                    body.getOrDefault("role", "CLINICIAN").toUpperCase());
            User user = new User(username, role);
            userRepository.save(user);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/users/{username} — used by UI login dropdown
    @GetMapping("/api/users/{username}")
    public ResponseEntity<?> findUser(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}