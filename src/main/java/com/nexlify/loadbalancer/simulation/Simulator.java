package com.nexlify.loadbalancer.simulation;

import com.nexlify.loadbalancer.service.DependencyGraph;
import com.nexlify.loadbalancer.service.MetricsProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Simulator implements CommandLineRunner {
    @Autowired
    private DependencyGraph dependencyGraph;
    @Autowired
    private MetricsProcessor metricsProcessor;

    @Override
    public void run(String... args) {
        dependencyGraph.registerService("A", new String[]{"B"});
        dependencyGraph.registerService("B", new String[]{"C"});
        dependencyGraph.registerService("C", new String[]{});
        metricsProcessor.updateMetrics("A", 100.0, 0.01);
        metricsProcessor.updateMetrics("B", 200.0, 0.01);
        metricsProcessor.updateMetrics("C", 30.0, 0.02);
    }
}