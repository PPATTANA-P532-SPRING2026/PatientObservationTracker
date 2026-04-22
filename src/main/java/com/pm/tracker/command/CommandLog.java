package com.pm.tracker.command;

import com.pm.tracker.access.CommandLogRepository;
import com.pm.tracker.model.log.CommandLogEntry;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CommandLog {

    private final CommandLogRepository commandLogRepository;

    public CommandLog(CommandLogRepository commandLogRepository) {
        this.commandLogRepository = commandLogRepository;
    }

    public void execute(Command command, String user) {
        command.execute();
        CommandLogEntry entry = new CommandLogEntry(
                command.getCommandType(),
                command.toJson(),
                LocalDateTime.now(),
                user,
                command
        );
        commandLogRepository.save(entry);
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

        if (entry.getCommand() == null) {
            throw new IllegalStateException(
                    "Command reference lost — cannot undo after restart.");
        }

        entry.getCommand().undo();
        entry.setUndone(true);
        commandLogRepository.save(entry);
    }
}