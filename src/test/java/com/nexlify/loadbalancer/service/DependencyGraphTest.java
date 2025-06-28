package com.nexlify.loadbalancer.service;


import com.nexlify.loadbalancer.model.ServiceNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DependencyGraphTest {

    @Autowired
    private DependencyGraph dependencyGraph;

    @Test
    void testRegisterServiceWithEndpoint() {
        dependencyGraph.registerService("A", new String[]{"B"}, "http://localhost:8081");
        dependencyGraph.registerService("B", new String[]{}, "http://localhost:8082");
        ServiceNode nodeA = dependencyGraph.getService("A");
        assertNotNull(nodeA.getEndpoint());
        assertEquals("http://localhost:8081", nodeA.getEndpoint());
    }
    @Test
    void testRegisterService() {
        DependencyGraph graph = new DependencyGraph();
        graph.registerService("A", new String[]{"B", "C"}, "http://localhost:8081");
        assertNotNull(graph.getService("A"));
        assertEquals(2, graph.getService("A").getDependencies().size());
    }

    @Test
    void testGetServiceNonExistent() {
        DependencyGraph graph = new DependencyGraph();
        assertNull(graph.getService("X"));
    }

    @Test
    void testHealthyService() {
        DependencyGraph graph = new DependencyGraph();
        graph.registerService("A", new String[]{"B"}, "http://localhost:8081");
        ServiceNode nodeA = graph.getService("A");
        ServiceNode nodeB = graph.getService("B");
        nodeA.getMetrics().put("latency", 50.0);
        nodeA.getMetrics().put("errorRate", 0.01);
        nodeB.getMetrics().put("latency", 30.0);
        nodeB.getMetrics().put("errorRate", 0.02);
        assertTrue(nodeA.isHealthy());
    }
}
