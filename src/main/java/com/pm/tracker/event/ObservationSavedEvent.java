package com.pm.tracker.event;

import com.pm.tracker.model.operational.Observation;


public class ObservationSavedEvent {

    private final Observation observation;

    public ObservationSavedEvent(Observation observation) {
        this.observation = observation;
    }

    public Observation getObservation() {
        return observation;
    }
}