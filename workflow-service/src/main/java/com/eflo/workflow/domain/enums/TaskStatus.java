package com.eflo.workflow.domain.enums;

/**
 * Task execution status
 */
public enum TaskStatus {
    /**
     * En attente - Task is waiting to be started
     */
    EN_ATTENTE("En attente", "Waiting"),

    /**
     * Reçu - Task has been received/started
     */
    RECU("Reçu", "Received"),

    /**
     * Réalisé - Task has been completed
     */
    REALISE("Réalisé", "Completed"),

    /**
     * Skipped - Task was skipped (not applicable)
     */
    SKIPPED("Ignoré", "Skipped"),

    /**
     * Cancelled - Task was cancelled
     */
    CANCELLED("Annulé", "Cancelled"),

    // English aliases for backward compatibility
    PENDING("En attente", "Pending"),
    ASSIGNED("Assigné", "Assigned"),
    IN_PROGRESS("En cours", "In Progress"),
    COMPLETED("Terminé", "Completed"),
    FAILED("Échoué", "Failed");

    private final String displayNameFr;
    private final String displayNameEn;

    TaskStatus(String displayNameFr, String displayNameEn) {
        this.displayNameFr = displayNameFr;
        this.displayNameEn = displayNameEn;
    }

    public String getDisplayNameFr() {
        return displayNameFr;
    }

    public String getDisplayNameEn() {
        return displayNameEn;
    }

    public boolean isCompleted() {
        return this == REALISE;
    }

    public boolean isInProgress() {
        return this == RECU;
    }

    public boolean isPending() {
        return this == EN_ATTENTE;
    }

    public boolean isTerminal() {
        return this == REALISE || this == SKIPPED || this == CANCELLED;
    }

    public boolean canWork() {
        return this == RECU || this == IN_PROGRESS;
    }
}
