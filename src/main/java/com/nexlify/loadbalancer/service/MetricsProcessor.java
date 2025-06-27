package com.nexlify.loadbalancer.service;

import com.nexlify.loadbalancer.model.ServiceNode;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MetricsProcessor {
    private final DependencyGraph dependencyGraph;

    public MetricsProcessor(DependencyGraph dependencyGraph) {
        this.dependencyGraph = dependencyGraph;
    }

    public void updateMetrics(String serviceId, double latency, double errorRate) {
        ServiceNode node = dependencyGraph.getService(serviceId);
        if (node != null) {
            node.getMetrics().put("latency", latency);
            node.getMetrics().put("errorRate", errorRate);
        }
    }

    public Map<String, Double> getMetrics(String serviceId) {
        ServiceNode node = dependencyGraph.getService(serviceId);
        return node != null ? node.getMetrics() : new ConcurrentHashMap<>();
    }
}