package com.nexlify.loadbalancer.model;

import lombok.Data;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class ServiceNode {
    private final String serviceId;
    private final Map<String, Double> metrics;
    private final Map<String, ServiceNode> dependencies;

    public ServiceNode(String serviceId) {
        this.serviceId = serviceId;
        this.metrics = new ConcurrentHashMap<>();
        this.dependencies = new ConcurrentHashMap<>();
    }

    public boolean isHealthy() {
        double latency = metrics.getOrDefault("latency", 0.0);
        double errorRate = metrics.getOrDefault("errorRate", 0.0);
        return latency < 100 && errorRate < 0.05 &&
                dependencies.values().stream().allMatch(ServiceNode::isHealthy);
    }
}
