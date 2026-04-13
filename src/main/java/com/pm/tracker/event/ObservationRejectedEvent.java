package com.pm.tracker.event;

import com.pm.tracker.model.operational.Observation;


public class ObservationRejectedEvent {

    private final Observation observation;
    private final String rejectionReason;

    public ObservationRejectedEvent(Observation observation,
                                    String rejectionReason) {
        this.observation     = observation;
        this.rejectionReason = rejectionReason;
    }

    public Observation getObservation()  { return observation; }
    public String getRejectionReason()   { return rejectionReason; }
}