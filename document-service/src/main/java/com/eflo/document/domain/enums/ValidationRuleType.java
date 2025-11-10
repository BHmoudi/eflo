package com.eflo.document.domain.enums;

public enum ValidationRuleType {
    FILE_SIZE("File size validation"),
    FILE_FORMAT("File format validation"),
    FILE_NAME_PATTERN("File name pattern validation"),
    CONTENT_CHECK("Content validation"),
    METADATA_REQUIRED("Required metadata validation"),
    BUSINESS_RULE("Business rule validation"),
    CUSTOM_SCRIPT("Custom script validation");

    private final String description;

    ValidationRuleType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
