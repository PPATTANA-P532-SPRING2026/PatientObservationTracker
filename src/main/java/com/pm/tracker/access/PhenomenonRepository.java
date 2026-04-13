package com.pm.tracker.access;

import com.pm.tracker.model.knowledge.Phenomenon;
import com.pm.tracker.model.knowledge.PhenomenonType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PhenomenonRepository extends JpaRepository<Phenomenon, UUID> {
    List<Phenomenon> findByPhenomenonType(PhenomenonType phenomenonType);
}