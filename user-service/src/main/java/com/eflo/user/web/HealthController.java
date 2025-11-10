package com.eflo.user.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "user-service");
        health.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(health);
    }

    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> databaseHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            health.put("status", "UP");
            health.put("database", "PostgreSQL");
            health.put("message", "Database connection is healthy");
        } catch (Exception e) {
            log.error("Database health check failed", e);
            health.put("status", "DOWN");
            health.put("database", "PostgreSQL");
            health.put("message", "Database connection failed: " + e.getMessage());
            return ResponseEntity.status(503).body(health);
        }

        health.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(health);
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> ready = new HashMap<>();

        try {
            // Check database connectivity
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);

            ready.put("status", "READY");
            ready.put("message", "Service is ready to accept traffic");
            ready.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(ready);
        } catch (Exception e) {
            log.error("Readiness check failed", e);
            ready.put("status", "NOT_READY");
            ready.put("message", "Service is not ready: " + e.getMessage());
            ready.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(503).body(ready);
        }
    }

    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> liveness() {
        Map<String, Object> live = new HashMap<>();
        live.put("status", "ALIVE");
        live.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(live);
    }
}
