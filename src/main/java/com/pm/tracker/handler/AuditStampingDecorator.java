package com.pm.tracker.handler;

import java.time.LocalDateTime;


public class AuditStampingDecorator implements ObservationProcessor {

    private final ObservationProcessor delegate;
    private final String actingUser;

    public AuditStampingDecorator(ObservationProcessor delegate,
                                  String actingUser) {
        this.delegate    = delegate;
        this.actingUser  = actingUser;
    }

    @Override
    public ObservationRequest process(ObservationRequest request) {
        request.setRecordingTime(LocalDateTime.now());
        request.setActingUser(actingUser);
        return delegate.process(request);
    }
}