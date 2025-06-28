package com.nexlify.loadbalancer;


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

    @Scheduled(fixedRate = 60000) // Check every minute
    public void checkHealth() {
        for (ServiceNode node : dependencyGraph.getNodes()) {
            try {
                String healthUrl = node.getEndpoint() + "/health";
                var response = restTemplate.getForEntity(healthUrl, String.class);
                node.setHealthy(response.getStatusCode().is2xxSuccessful());
            } catch (Exception e) {
                node.setHealthy(false);
            }
        }
    }
}
