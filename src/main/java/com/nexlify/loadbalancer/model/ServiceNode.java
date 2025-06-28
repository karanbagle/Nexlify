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

    public ServiceNode(String serviceId) {
        this.serviceId = serviceId;
        this.metrics = new ConcurrentHashMap<>();
        this.dependencies = new ConcurrentHashMap<>();
    }

    public boolean isHealthy() {
        double latency = metrics.getOrDefault("latency", 0.0);
        double errorRate = metrics.getOrDefault("errorRate", 0.0);
        boolean isHealthy = latency < 100 && errorRate < 0.05 &&
                dependencies.values().stream().allMatch(ServiceNode::isHealthy);
        logger.info("Service {}: latency={}, errorRate={}, isHealthy={}", serviceId, latency, errorRate, isHealthy);
        return isHealthy;
    }
}