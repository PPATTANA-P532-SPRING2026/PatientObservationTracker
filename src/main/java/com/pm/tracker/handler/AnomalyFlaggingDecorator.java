package com.pm.tracker.handler;


public class AnomalyFlaggingDecorator implements ObservationProcessor {

    private final ObservationProcessor delegate;

    public AnomalyFlaggingDecorator(ObservationProcessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public ObservationRequest process(ObservationRequest request) {
        if (request.getAmount() != null && request.getPhenomenonType() != null) {
            Double min = request.getPhenomenonType().getNormalMin();
            Double max = request.getPhenomenonType().getNormalMax();

            if (min != null && max != null) {
                double value = request.getAmount();
                if (value < min || value > max) {
                    request.setAnomalyFlagged(true);
                    request.setAnomalyDetail(
                            "Value " + value + " is outside normal range ["
                                    + min + ", " + max + "] for '"
                                    + request.getPhenomenonType().getName() + "'");
                }
            }
        }

        return delegate.process(request);
    }
}