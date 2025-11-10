package com.eflo.user.exception;

/**
 * Exception thrown when a hierarchy relationship is not found in the system.
 */
public class HierarchyNotFoundException extends RuntimeException {

    private final String hierarchyId;

    public HierarchyNotFoundException(String hierarchyId) {
        super(String.format("Hierarchy not found with ID: %s", hierarchyId));
        this.hierarchyId = hierarchyId;
    }

    public HierarchyNotFoundException(String hierarchyId, String message) {
        super(message);
        this.hierarchyId = hierarchyId;
    }

    public String getHierarchyId() {
        return hierarchyId;
    }
}
