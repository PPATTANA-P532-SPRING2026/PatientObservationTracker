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

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Column(nullable = false)
    private LocalDateTime executedAt;

    @Column(nullable = false)
    private String user;

    // ── Change 3 additions ────────────────────────────────────────────
    @Column(nullable = false)
    private boolean undone = false;

    // store the command object for undo reconstruction
    @Transient
    private transient com.pm.tracker.command.Command command;

    // ── constructors ──────────────────────────────────────────────────
    public CommandLogEntry() {}

    public CommandLogEntry(String commandType, String payload,
                           LocalDateTime executedAt, String user,
                           com.pm.tracker.command.Command command) {
        this.commandType = commandType;
        this.payload     = payload;
        this.executedAt  = executedAt;
        this.user        = user;
        this.command     = command;
        this.undone      = false;
    }

    // ── getters / setters ─────────────────────────────────────────────
    public UUID getId()              { return id; }
    public String getCommandType()   { return commandType; }
    public String getPayload()       { return payload; }
    public LocalDateTime getExecutedAt() { return executedAt; }
    public String getUser()          { return user; }
    public boolean isUndone()        { return undone; }
    public void setUndone(boolean u) { this.undone = u; }
    public com.pm.tracker.command.Command getCommand() { return command; }
    public void setCommand(com.pm.tracker.command.Command c) { this.command = c; }
}