package com.eflo.user.exception;

/**
 * Exception thrown when an invalid hierarchy operation is attempted,
 * such as creating circular references or invalid parent-child relationships.
 */
public class InvalidHierarchyException extends RuntimeException {

    private final String childId;
    private final String parentId;

    public InvalidHierarchyException(String message) {
        super(message);
        this.childId = null;
        this.parentId = null;
    }

    public InvalidHierarchyException(String childId, String parentId, String message) {
        super(message);
        this.childId = childId;
        this.parentId = parentId;
    }

    public static InvalidHierarchyException circularReference(String childId, String parentId) {
        return new InvalidHierarchyException(
            childId,
            parentId,
            String.format("Circular reference detected: Cannot set parent %s for child %s", parentId, childId)
        );
    }

    public static InvalidHierarchyException selfReference(String businessUnitId) {
        return new InvalidHierarchyException(
            String.format("Business unit cannot be its own parent: %s", businessUnitId)
        );
    }

    public String getChildId() {
        return childId;
    }

    public String getParentId() {
        return parentId;
    }
}
