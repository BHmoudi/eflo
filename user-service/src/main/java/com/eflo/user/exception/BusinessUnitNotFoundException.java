package com.eflo.user.exception;

/**
 * Exception thrown when a business unit is not found in the system.
 */
public class BusinessUnitNotFoundException extends RuntimeException {

    private final String businessUnitId;

    public BusinessUnitNotFoundException(String businessUnitId) {
        super(String.format("Business unit not found with ID: %s", businessUnitId));
        this.businessUnitId = businessUnitId;
    }

    public BusinessUnitNotFoundException(String businessUnitId, String message) {
        super(message);
        this.businessUnitId = businessUnitId;
    }

    public String getBusinessUnitId() {
        return businessUnitId;
    }
}
