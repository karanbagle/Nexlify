package com.nexlify.loadbalancer.service;

import com.nexlify.loadbalancer.websocket.DashboardWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);
    private final DashboardWebSocketHandler webSocketHandler;

    public DashboardService(DashboardWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Scheduled(initialDelay = 10000, fixedRate = 10000)
    public void updateDashboard() throws Exception {
        logger.info("Updating dashboard");
        webSocketHandler.broadcastGraph();
    }
}