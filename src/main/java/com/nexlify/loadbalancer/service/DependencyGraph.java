package com.nexlify.loadbalancer.service;

import com.nexlify.loadbalancer.model.ServiceNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DependencyGraph {
    private static final Logger logger = LoggerFactory.getLogger(DependencyGraph.class);
    private final Map<String, ServiceNode> graph = new ConcurrentHashMap<>();

    public void registerService(String serviceId, String[] dependsOn) {
        logger.info("Registering service: {}, dependsOn: {}", serviceId, dependsOn);
        ServiceNode node = graph.computeIfAbsent(serviceId, ServiceNode::new);
        for (String depId : dependsOn) {
            ServiceNode depNode = graph.computeIfAbsent(depId, ServiceNode::new);
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
}