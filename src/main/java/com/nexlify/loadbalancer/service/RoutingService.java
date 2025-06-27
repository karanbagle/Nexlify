package com.nexlify.loadbalancer.service;

import com.nexlify.loadbalancer.model.ServiceNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RoutingService {
    private static final Logger logger = LoggerFactory.getLogger(RoutingService.class);
    private final DependencyGraph dependencyGraph;

    public RoutingService(DependencyGraph dependencyGraph) {
        this.dependencyGraph = dependencyGraph;
    }

    public String routeRequest(String targetServiceId) {
        ServiceNode node = dependencyGraph.getService(targetServiceId);
        logger.info("Routing request for serviceId: {}, node: {}", targetServiceId, node);
        if (node == null || !node.isHealthy()) {
            logger.info("Service {} is unhealthy or null, finding alternative", targetServiceId);
            return findAlternativeService();
        }
        logger.info("Service {} is healthy, routing to it", targetServiceId);
        return targetServiceId;
    }

    private String findAlternativeService() {
        String alternative = dependencyGraph.getGraph().keySet().stream()
                .filter(id -> {
                    ServiceNode service = dependencyGraph.getService(id);
                    boolean isHealthy = service.isHealthy();
                    logger.info("Checking service {}: isHealthy={}", id, isHealthy);
                    return isHealthy;
                })
                .findFirst()
                .orElse("fallback-service");
        logger.info("Selected alternative service: {}", alternative);
        return alternative;
    }
}
