package com.nexlify.loadbalancer.service;

import com.nexlify.loadbalancer.model.ServiceNode;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DependencyGraph {
    private final Map<String, ServiceNode> graph = new ConcurrentHashMap<>();

    public void registerService(String serviceId, String[] dependsOn) {
        ServiceNode node = graph.computeIfAbsent(serviceId, ServiceNode::new);
        for (String depId : dependsOn) {
            ServiceNode depNode = graph.computeIfAbsent(depId, ServiceNode::new);
            node.getDependencies().put(depId, depNode);
        }
    }

    public ServiceNode getService(String serviceId) {
        return graph.getServiceId();
    }

    public Map<String, ServiceNode> getGraph() {
        return graph;
    }
}