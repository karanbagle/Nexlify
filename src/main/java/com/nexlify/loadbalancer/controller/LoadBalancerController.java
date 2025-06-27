package com.nexlify.loadbalancer.controller;


import com.nexlify.loadbalancer.dto.ServiceRegistrationRequest;
import com.nexlify.loadbalancer.service.DependencyGraph;
import com.nexlify.loadbalancer.service.MetricsProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/nexlify")
public class LoadBalancerController {
    @Autowired
    private DependencyGraph dependencyGraph;

    @Autowired
    private MetricsProcessor metricsProcessor;

    @PostMapping("/register")
    public void registerService(@RequestBody ServiceRegistrationRequest request) {
        dependencyGraph.registerService(request.getServiceId(), request.getDependsOn());
    }

    @GetMapping("/route/{serviceId}")
    public String routeRequest(@PathVariable String serviceId) {
        return "Routed to: " + serviceId; // Placeholder
    }

    @PostMapping("/metrics")
    public void updateMetrics(@RequestBody Map<String, Double> metrics, @RequestParam String serviceId) {
        metricsProcessor.updateMetrics(serviceId, metrics.getOrDefault("latency", 0.0),
                metrics.getOrDefault("errorRate", 0.0));
    }

}
