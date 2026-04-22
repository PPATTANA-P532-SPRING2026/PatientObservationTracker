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
    private java.time.LocalDateTime executedAt;

    @Column(nullable = false)
    private String user;

    @Column(nullable = false)
    private boolean undone = false;

    public CommandLogEntry() {}

    public CommandLogEntry(String commandType, String payload,
                           java.time.LocalDateTime executedAt,
                           String user, Object ignored) {
        this.commandType = commandType;
        this.payload     = payload;
        this.executedAt  = executedAt;
        this.user        = user;
        this.undone      = false;
    }

    public java.util.UUID getId()            { return id; }
    public String getCommandType()           { return commandType; }
    public String getPayload()               { return payload; }
    public java.time.LocalDateTime getExecutedAt() { return executedAt; }
    public String getUser()                  { return user; }
    public boolean isUndone()               { return undone; }
    public void setUndone(boolean u)        { this.undone = u; }
}