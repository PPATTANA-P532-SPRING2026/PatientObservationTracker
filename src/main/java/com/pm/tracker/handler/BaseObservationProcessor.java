package com.pm.tracker.handler;

import org.springframework.stereotype.Component;

@Component
public class BaseObservationProcessor implements ObservationProcessor {

    @Override
    public ObservationRequest process(ObservationRequest request) {
        return request;
    }
}