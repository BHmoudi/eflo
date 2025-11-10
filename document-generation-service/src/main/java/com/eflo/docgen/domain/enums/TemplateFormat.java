package com.eflo.docgen.domain.enums;

/**
 * Template Format
 * Defines the file format of templates
 */
public enum TemplateFormat {
    DOCX("Microsoft Word Document", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx"),
    HTML("HTML Document", "text/html", ".html"),
    PDF("PDF Document", "application/pdf", ".pdf"),
    ODT("OpenDocument Text", "application/vnd.oasis.opendocument.text", ".odt");

    private final String displayName;
    private final String mimeType;
    private final String extension;

    TemplateFormat(String displayName, String mimeType, String extension) {
        this.displayName = displayName;
        this.mimeType = mimeType;
        this.extension = extension;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getExtension() {
        return extension;
    }
}
