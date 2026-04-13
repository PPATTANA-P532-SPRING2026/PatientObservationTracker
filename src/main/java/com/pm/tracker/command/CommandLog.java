package com.pm.tracker.command;

import com.pm.tracker.access.CommandLogRepository;
import com.pm.tracker.model.log.CommandLogEntry;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class CommandLog {

    private static final String CURRENT_USER = "staff";

    private final CommandLogRepository commandLogRepository;

    public CommandLog(CommandLogRepository commandLogRepository) {
        this.commandLogRepository = commandLogRepository;
    }


    public void execute(Command command) {
        // Step 1 — perform the action
        command.execute();

        // Step 2 — persist log entry with JSON payload
        CommandLogEntry entry = new CommandLogEntry(
                command.getCommandType(),
                command.toJson(),
                LocalDateTime.now(),
                CURRENT_USER
        );
        commandLogRepository.save(entry);
    }
}