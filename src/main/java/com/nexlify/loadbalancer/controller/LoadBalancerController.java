package com.nexlify.loadbalancer.controller;

import com.nexlify.loadbalancer.dto.ServiceRegistrationRequest;
import com.nexlify.loadbalancer.model.ServiceNode;
import com.nexlify.loadbalancer.service.DependencyGraph;
import com.nexlify.loadbalancer.service.MetricsProcessor;
import com.nexlify.loadbalancer.service.RoutingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/nexlify")
public class LoadBalancerController {
    private static final Logger logger = LoggerFactory.getLogger(LoadBalancerController.class);
    @Autowired
    private DependencyGraph dependencyGraph;
    @Autowired
    private MetricsProcessor metricsProcessor;
    @Autowired
    private RoutingService routingService;

    @PostMapping("/register")
    public void registerService(@RequestBody ServiceRegistrationRequest request) {
        logger.info("Registering service: {}, dependsOn: {}", request.getServiceId(), request.getDependsOn());
        dependencyGraph.registerService(request.getServiceId(), request.getDependsOn());
        logger.info("Graph after registration: {}", dependencyGraph.getGraph().keySet());
    }

    @PostMapping("/metrics")
    public void updateMetrics(@RequestBody Map<String, Double> metrics, @RequestParam(required = false) String serviceId) {
        if (serviceId == null || serviceId.isEmpty()) {
            logger.error("Missing serviceId parameter");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing serviceId parameter");
        }
        logger.info("Updating metrics for service {}: {}", serviceId, metrics);
        metricsProcessor.updateMetrics(serviceId, metrics.getOrDefault("latency", 0.0),
                metrics.getOrDefault("errorRate", 0.0));
        logger.info("Metrics updated for service {}: {}", serviceId, metricsProcessor.getMetrics(serviceId));
    }

    @GetMapping("/route/{serviceId}")
    public String routeRequest(@PathVariable String serviceId) {
        return routingService.routeRequest(serviceId);
    }

    @GetMapping("/debug/{serviceId}")
    public Map<String, Double> getMetrics(@PathVariable String serviceId) {
        ServiceNode node = dependencyGraph.getService(serviceId);
        return node != null ? node.getMetrics() : new HashMap<>();
    }
}