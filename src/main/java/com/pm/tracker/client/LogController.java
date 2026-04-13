package com.pm.tracker.client;

import com.pm.tracker.access.AuditLogRepository;
import com.pm.tracker.access.CommandLogRepository;
import com.pm.tracker.model.log.AuditLogEntry;
import com.pm.tracker.model.log.CommandLogEntry;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * LogController — Layer 1 Client.
 * Read-only endpoints for command log and audit log.
 */
@RestController
public class LogController {

    private final CommandLogRepository commandLogRepository;
    private final AuditLogRepository auditLogRepository;

    public LogController(CommandLogRepository commandLogRepository,
                         AuditLogRepository auditLogRepository) {
        this.commandLogRepository = commandLogRepository;
        this.auditLogRepository   = auditLogRepository;
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
}