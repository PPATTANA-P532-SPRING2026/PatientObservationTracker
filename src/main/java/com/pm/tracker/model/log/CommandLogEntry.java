package com.pm.tracker.model.log;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "command_log_entries")
public class CommandLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String commandType;

    // Full JSON payload for future undo reconstruction
    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Column(nullable = false)
    private LocalDateTime executedAt;

    @Column(nullable = false)
    private String user;

    // ── constructors ──────────────────────────────────────────────────
    public CommandLogEntry() {}

    public CommandLogEntry(String commandType, String payload,
                           LocalDateTime executedAt, String user) {
        this.commandType = commandType;
        this.payload     = payload;
        this.executedAt  = executedAt;
        this.user        = user;
    }

    // ── getters ───────────────────────────────────────────────────────
    public UUID getId()              { return id; }
    public String getCommandType()   { return commandType; }
    public String getPayload()       { return payload; }
    public LocalDateTime getExecutedAt() { return executedAt; }
    public String getUser()          { return user; }
}