package com.pm.tracker.engine;

import com.pm.tracker.model.knowledge.AssociativeFunction;
import com.pm.tracker.model.operational.Observation;

import java.util.List;


public interface DiagnosisStrategy {


    boolean evaluate(AssociativeFunction rule, List<Observation> patientObservations);
}