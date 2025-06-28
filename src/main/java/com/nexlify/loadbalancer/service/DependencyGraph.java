package com.nexlify.loadbalancer.service;

import com.nexlify.loadbalancer.model.ServiceNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DependencyGraph {
    private static final Logger logger = LoggerFactory.getLogger(DependencyGraph.class);
    private final Map<String, ServiceNode> graph = new ConcurrentHashMap<>();

    public void registerService(String serviceId, String[] dependsOn, String endpoint) {
        logger.info("Registering service: {}, dependsOn: {}, endpoint: {}", serviceId, dependsOn, endpoint);
        ServiceNode node = graph.computeIfAbsent(serviceId, k -> new ServiceNode(serviceId));
        logger.info("Before setEndpoint, node.endpoint: {}", node.getEndpoint());
        node.setEndpoint(endpoint != null ? endpoint : "http://localhost:808" + (graph.size() + 1));
        logger.info("After setEndpoint, node.endpoint: {}", node.getEndpoint());
        for (String depId : dependsOn) {
            ServiceNode depNode = graph.computeIfAbsent(depId, k -> new ServiceNode(depId));
            depNode.setEndpoint(depNode.getEndpoint() != null ? depNode.getEndpoint() : "http://localhost:808" + (graph.size() + 1));
            node.getDependencies().put(depId, depNode);
        }
        logger.info("Graph after registration: {}", graph.keySet());
    }

    public ServiceNode getService(String serviceId) {
        return graph.get(serviceId);
    }

    public Map<String, ServiceNode> getGraph() {
        return graph;
    }

    public Set<ServiceNode> getNodes() {
        return Set.copyOf(graph.values()); // Thread-safe snapshot
    }
}