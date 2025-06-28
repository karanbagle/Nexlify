package com.nexlify.loadbalancer.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexlify.loadbalancer.model.ServiceNode;
import com.nexlify.loadbalancer.service.DependencyGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class DashboardWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(DashboardWebSocketHandler.class);
    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final DependencyGraph dependencyGraph;
    private final ObjectMapper objectMapper;

    @Autowired
    public DashboardWebSocketHandler(DependencyGraph dependencyGraph, ObjectMapper objectMapper) {
        this.dependencyGraph = dependencyGraph;
        this.objectMapper = objectMapper;
        logger.info("DashboardWebSocketHandler initialized, dependencyGraph: {}", dependencyGraph);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        logger.info("WebSocket connection established: {}", session.getId());
        sessions.add(session);
        broadcastInitialData(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.info("WebSocket connection closed: {}, status: {}", session.getId(), status);
        sessions.remove(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        logger.info("Received message from {}: {}", session.getId(), message.getPayload());
        if ("ping".equals(message.getPayload())) {
            session.sendMessage(new TextMessage("pong"));
            logger.info("Sent pong to session: {}", session.getId());
        }
    }

    @Scheduled(fixedRate = 5000) // Update every 5 seconds
    public void broadcastGraph() throws IOException {
        if (dependencyGraph == null) {
            logger.error("DependencyGraph is null, cannot broadcast");
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("services", dependencyGraph.getNodes());
        data.put("traffic", getTrafficData());
        String json = objectMapper.writeValueAsString(data);
        logger.debug("Broadcasting JSON: {}", json);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(json));
                logger.info("Sent data to session: {}", session.getId());
            } else {
                sessions.remove(session);
            }
        }
    }

    private void broadcastInitialData(WebSocketSession session) throws IOException {
        if (dependencyGraph == null) {
            logger.error("DependencyGraph is null, cannot send initial data");
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("services", dependencyGraph.getNodes());
        data.put("traffic", getTrafficData());
        String json = objectMapper.writeValueAsString(data);
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(json));
            logger.info("Sent initial data to session: {}", session.getId());
        } else {
            logger.warn("Session {} is closed, cannot send initial data", session.getId());
        }
    }

    private Map<String, Integer> getTrafficData() {
        Map<String, Integer> traffic = new HashMap<>();
        for (ServiceNode node : dependencyGraph.getNodes()) {
            traffic.put(node.getServiceId(), (int) (Math.random() * 100));
        }
        return traffic;
    }
}