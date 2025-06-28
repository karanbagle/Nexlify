package com.nexlify.loadbalancer.model;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class ServiceNode implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(ServiceNode.class);
    private final String serviceId;
    private final Map<String, Double> metrics;
    private final Map<String, ServiceNode> dependencies;
    private String endpoint;
    private boolean healthy; // New field for direct health status

    public ServiceNode(String serviceId) {
        this.serviceId = serviceId;
        this.metrics = new ConcurrentHashMap<>();
        this.dependencies = new ConcurrentHashMap<>();
        this.endpoint = null; // Default to null, set later
        this.healthy = true;  // Default to healthy
    }

    public boolean isHealthy() {
        double latency = metrics.getOrDefault("latency", 0.0);
        double errorRate = metrics.getOrDefault("errorRate", 0.0);
        boolean calculatedHealthy = latency < 250 && errorRate < 0.1 &&
                dependencies.values().stream().allMatch(ServiceNode::isHealthy);
        logger.info("Service {}: latency={}, errorRate={}, calculatedHealthy={}, actualHealthy={}",
                serviceId, latency, errorRate, calculatedHealthy, healthy);
        return healthy; // Use the settable healthy status, validated by calculated value
    }
}