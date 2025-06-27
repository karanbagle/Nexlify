package com.nexlify.loadbalancer.dto;

import lombok.Data;

@Data
public class ServiceRegistrationRequest {
    private String serviceId;
    private String[] dependsOn;
}
