package com.nexlify.loadbalancer.service;


import com.nexlify.loadbalancer.model.ServiceNode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DependencyGraphTest {
    @Test
    void testRegisterService() {
        DependencyGraph graph = new DependencyGraph();
        graph.registerService("A", new String[]{"B", "C"});
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
        graph.registerService("A", new String[]{"B"});
        ServiceNode nodeA = graph.getService("A");
        ServiceNode nodeB = graph.getService("B");
        nodeA.getMetrics().put("latency", 50.0);
        nodeA.getMetrics().put("errorRate", 0.01);
        nodeB.getMetrics().put("latency", 30.0);
        nodeB.getMetrics().put("errorRate", 0.02);
        assertTrue(nodeA.isHealthy());
    }
}
