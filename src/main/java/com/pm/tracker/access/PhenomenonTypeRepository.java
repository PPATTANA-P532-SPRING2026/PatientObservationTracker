package com.pm.tracker.access;

import com.pm.tracker.model.knowledge.PhenomenonType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PhenomenonTypeRepository extends JpaRepository<PhenomenonType, UUID> {}