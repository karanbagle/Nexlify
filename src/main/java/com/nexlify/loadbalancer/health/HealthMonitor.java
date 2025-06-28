package com.nexlify.loadbalancer.health;

import com.nexlify.loadbalancer.model.ServiceNode;
import com.nexlify.loadbalancer.service.DependencyGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class HealthMonitor {
    private final DependencyGraph dependencyGraph;
    private final RestTemplate restTemplate;

    @Autowired
    public HealthMonitor(DependencyGraph dependencyGraph, RestTemplate restTemplate) {
        this.dependencyGraph = dependencyGraph;
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedRate = 30000) // Check every 30 seconds
    public void checkHealth() {
        for (ServiceNode node : dependencyGraph.getNodes()) {
            if (node.getEndpoint() != null && !node.getEndpoint().isEmpty()) {
                try {
                    String healthUrl = node.getEndpoint() + "/health";
                    var response = restTemplate.getForEntity(healthUrl, String.class, 5000); // 5-second timeout
                    boolean isHealthy = response.getStatusCode().is2xxSuccessful();
                    node.setHealthy(isHealthy);
                    node.getMetrics().put("latency", isHealthy ? 50.0 : 500.0);
                    node.getMetrics().put("errorRate", isHealthy ? 0.01 : 0.5);
                } catch (Exception e) {
                    node.setHealthy(false);
                    node.getMetrics().put("latency", 1000.0);
                    node.getMetrics().put("errorRate", 1.0);
                }
            }
        }
    }
}