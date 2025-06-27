package com.nexlify.loadbalancer.service;

import com.nexlify.loadbalancer.model.ServiceNode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class MetricsProcessor {
    private final DependencyGraph dependencyGraph;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public MetricsProcessor(DependencyGraph dependencyGraph) {
        this.dependencyGraph = dependencyGraph;
    }

    @Async
    public CompletableFuture<Void> updateMetrics(String serviceId, double latency, double errorRate) {
        return CompletableFuture.runAsync(() -> {
            ServiceNode node = dependencyGraph.getService(serviceId);
            if (node != null) {
                node.getMetrics().put("latency", latency);
                node.getMetrics().put("errorRate", errorRate);
            }
        }, executor);
    }

    public void simulateMetrics() {
        dependencyGraph.getGraph().keySet().forEach(serviceId -> {
            updateMetrics(serviceId, Math.random() * 200, Math.random() * 0.1);
        });
    }
}
