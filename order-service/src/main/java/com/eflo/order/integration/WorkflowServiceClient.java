package com.eflo.order.integration;

import com.eflo.order.config.FeignClientConfiguration;
import com.eflo.order.domain.model.dto.WorkflowInstanceDTO;
import com.eflo.order.domain.model.request.StartInstanceRequest;
import com.eflo.order.domain.model.request.TransitionRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "workflow-service", configuration = FeignClientConfiguration.class)
public interface WorkflowServiceClient {

    @GetMapping("/api/v1/workflow/processes/code/{code}")
    Map<String, Object> getProcessByCode(@PathVariable String code);

    @PostMapping("/api/v1/workflow/instances")
    WorkflowInstanceDTO createInstance(@RequestBody StartInstanceRequest request);

    @PostMapping("/api/v1/workflow/instances/{id}/start")
    WorkflowInstanceDTO startInstance(@PathVariable Long id);

    @GetMapping("/api/v1/workflow/instances/{id}")
    WorkflowInstanceDTO getInstance(@PathVariable Long id);

    @PostMapping("/api/v1/workflow/instances/{id}/transition")
    WorkflowInstanceDTO executeTransition(@PathVariable Long id, @RequestBody TransitionRequest request);
}
