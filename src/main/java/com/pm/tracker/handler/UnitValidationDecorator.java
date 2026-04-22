package com.pm.tracker.handler;


public class UnitValidationDecorator implements ObservationProcessor {

    private final ObservationProcessor delegate;

    public UnitValidationDecorator(ObservationProcessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public ObservationRequest process(ObservationRequest request) {
        // only validate if this is a measurement (has a unit)
        if (request.getUnit() != null && request.getPhenomenonType() != null) {
            String unit = request.getUnit();
            boolean valid = request.getPhenomenonType()
                    .getAllowedUnits()
                    .contains(unit);

            if (!valid) {
                throw new ValidationException(
                        "Unit '" + unit + "' is not allowed for '"
                                + request.getPhenomenonType().getName()
                                + "'. Allowed: "
                                + request.getPhenomenonType().getAllowedUnits());
            }
        }

        return delegate.process(request);
    }
}