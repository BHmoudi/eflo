package com.eflo.user.util;

import com.eflo.user.service.KeycloakSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job for periodic synchronization with Keycloak
 *
 * Runs every 60 minutes to ensure database is in sync with Keycloak.
 */
@Slf4j
@Component
public class KeycloakSyncScheduler {

    private final KeycloakSyncService keycloakSyncService;

    public KeycloakSyncScheduler(KeycloakSyncService keycloakSyncService) {
        this.keycloakSyncService = keycloakSyncService;
    }

    /**
     * Scheduled sync job - runs every 60 minutes
     */
    @Scheduled(fixedRate = 3600000) // 60 minutes in milliseconds
    public void scheduledSync() {
        log.info("Starting scheduled Keycloak sync...");

        try {
            int syncedCount = keycloakSyncService.syncAllUsersFromKeycloak();
            log.info("Scheduled Keycloak sync completed successfully. Synced {} users.", syncedCount);
        } catch (Exception e) {
            log.error("Error during scheduled Keycloak sync", e);
        }
    }

    /**
     * Initial sync on startup - runs 2 minutes after application starts
     */
    @Scheduled(initialDelay = 120000, fixedRate = Long.MAX_VALUE) // Run once after 2 minutes
    public void initialSync() {
        log.info("Starting initial Keycloak sync on startup...");

        try {
            int syncedCount = keycloakSyncService.syncAllUsersFromKeycloak();
            log.info("Initial Keycloak sync completed successfully. Synced {} users.", syncedCount);
        } catch (Exception e) {
            log.error("Error during initial Keycloak sync", e);
        }
    }
}
