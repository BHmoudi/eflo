package com.eflo.user.web;

import com.eflo.user.domain.dto.SyncStatusResponse;
import com.eflo.user.service.KeycloakSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/keycloak-sync")
public class KeycloakSyncController {

    private final KeycloakSyncService keycloakSyncService;

    public KeycloakSyncController(KeycloakSyncService keycloakSyncService) {
        this.keycloakSyncService = keycloakSyncService;
    }

    @PostMapping("/sync-all")
    
    public ResponseEntity<SyncStatusResponse> syncAllUsers() {
        log.info("Starting manual sync of all users from Keycloak");

        try {
            int syncedCount = keycloakSyncService.syncAllUsersFromKeycloak();

            SyncStatusResponse response = SyncStatusResponse.builder()
                    .status("success")
                    .message("Successfully synced " + syncedCount + " users from Keycloak")
                    .lastCheck(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error syncing users from Keycloak", e);

            SyncStatusResponse response = SyncStatusResponse.builder()
                    .status("error")
                    .message("Error syncing users: " + e.getMessage())
                    .lastCheck(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/sync-user/{keycloakId}")
    
    public ResponseEntity<SyncStatusResponse> syncUser(@PathVariable String keycloakId) {
        log.info("Starting manual sync of user from Keycloak: {}", keycloakId);

        try {
            keycloakSyncService.syncSingleUserFromKeycloak(java.util.UUID.fromString(keycloakId));

            SyncStatusResponse response = SyncStatusResponse.builder()
                    .status("success")
                    .message("Successfully synced user from Keycloak")
                    .lastCheck(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error syncing user from Keycloak: {}", keycloakId, e);

            SyncStatusResponse response = SyncStatusResponse.builder()
                    .status("error")
                    .message("Error syncing user: " + e.getMessage())
                    .lastCheck(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/status")
    
    public ResponseEntity<SyncStatusResponse> getSyncStatus() {
        log.info("Fetching Keycloak sync status");

        Map<String, Object> statusMap = keycloakSyncService.getSyncStatus();

        SyncStatusResponse response = SyncStatusResponse.builder()
                .status((String) statusMap.get("status"))
                .keycloakUserCount(statusMap.containsKey("keycloakUserCount") ?
                        (int) statusMap.get("keycloakUserCount") : 0)
                .databaseUserCount(statusMap.containsKey("databaseUserCount") ?
                        (long) statusMap.get("databaseUserCount") : 0L)
                .lastCheck((LocalDateTime) statusMap.get("lastCheck"))
                .details(statusMap)
                .build();

        return ResponseEntity.ok(response);
    }
}
