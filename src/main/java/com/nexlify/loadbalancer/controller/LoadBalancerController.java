package com.nexlify.loadbalancer.controller;


import com.nexlify.loadbalancer.dto.ServiceRegistrationRequest;
import com.nexlify.loadbalancer.service.DependencyGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nexlify")
public class LoadBalancerController {
    @Autowired
    private DependencyGraph dependencyGraph;

    @PostMapping("/register")
    public void registerService(@RequestBody ServiceRegistrationRequest request) {
        dependencyGraph.registerService(request.getServiceId(), request.getDependsOn());
    }

    @GetMapping("/route/{serviceId}")
    public String routeRequest(@PathVariable String serviceId) {
        return "Routed to: " + serviceId; // Placeholder
    }
}
