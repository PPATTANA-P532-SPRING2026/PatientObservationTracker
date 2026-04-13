package com.pm.tracker.access;

import com.pm.tracker.model.log.CommandLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface CommandLogRepository extends JpaRepository<CommandLogEntry, UUID> {
    List<CommandLogEntry> findAllByOrderByExecutedAtDesc();
}