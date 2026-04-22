package com.pm.tracker.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.tracker.access.CommandLogRepository;
import com.pm.tracker.model.log.CommandLogEntry;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class CommandLog {

    private final CommandLogRepository commandLogRepository;
    private final ObjectMapper objectMapper;

    // in-memory map — survives for the lifetime of the container
    // keyed by CommandLogEntry id
    private final Map<UUID, Command> commandMap =
            new java.util.concurrent.ConcurrentHashMap<>();

    public CommandLog(CommandLogRepository commandLogRepository,
                      ObjectMapper objectMapper) {
        this.commandLogRepository = commandLogRepository;
        this.objectMapper         = objectMapper;
    }

    public void execute(Command command, String user) {
        command.execute();

        CommandLogEntry entry = new CommandLogEntry(
                command.getCommandType(),
                command.toJson(),
                java.time.LocalDateTime.now(),
                user,
                null   // ← don't store in entry, store in map below
        );
        commandLogRepository.save(entry);

        // store command reference keyed by the saved entry's id
        commandMap.put(entry.getId(), command);
    }

    public void undo(UUID logEntryId, String requestingUser) {
        CommandLogEntry entry = commandLogRepository.findById(logEntryId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Command log entry not found: " + logEntryId));

        if (entry.isUndone()) {
            throw new IllegalStateException(
                    "Command has already been undone.");
        }

        if (!entry.getUser().equals(requestingUser)) {
            throw new IllegalStateException(
                    "Only the user who executed this command may undo it.");
        }

        // look up command from in-memory map
        Command command = commandMap.get(logEntryId);
        if (command == null) {
            throw new IllegalStateException(
                    "Command reference lost — cannot undo after restart. " +
                            "Please record a new observation to correct this.");
        }

        command.undo();
        entry.setUndone(true);
        commandLogRepository.save(entry);

        // remove from map after undo — single level, no redo
        commandMap.remove(logEntryId);
    }
}