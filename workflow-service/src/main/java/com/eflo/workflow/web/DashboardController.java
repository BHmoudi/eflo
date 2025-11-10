package com.eflo.workflow.web;

import com.eflo.workflow.domain.model.response.DashboardResponse;
import com.eflo.workflow.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dashboard Controller
 *
 * REST API for workflow dashboard and statistics.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/workflow/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Get dashboard statistics
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<DashboardResponse> getDashboard() {
        log.info("REST request to get workflow dashboard");
        DashboardResponse response = dashboardService.getDashboardStatistics();
        return ResponseEntity.ok(response);
    }
}
