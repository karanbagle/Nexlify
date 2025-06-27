package com.nexlify.loadbalancer.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexlify.loadbalancer.model.ServiceNode;
import com.nexlify.loadbalancer.service.DependencyGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
public class DashboardWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(DashboardWebSocketHandler.class);
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    @Autowired
    private DependencyGraph dependencyGraph;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        logger.info("WebSocket connection established: {}, dependencyGraph: {}", session.getId(), dependencyGraph);
        if (dependencyGraph == null) {
            logger.error("DependencyGraph is null, cannot broadcast initial data");
            return;
        }
        sessions.add(session);
        logger.info("Current open sessions: {}", sessions.stream()
                .filter(WebSocketSession::isOpen)
                .map(WebSocketSession::getId)
                .collect(Collectors.toList()));
        broadcastGraphToSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        logger.info("WebSocket connection closed: {}, status: {}", session.getId(), status);
        sessions.remove(session);
        logger.info("Current open sessions: {}", sessions.stream()
                .filter(WebSocketSession::isOpen)
                .map(WebSocketSession::getId)
                .collect(Collectors.toList()));
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        logger.info("Received message from {}: {}", session.getId(), message.getPayload());
        if ("ping".equals(message.getPayload())) {
            session.sendMessage(new TextMessage("pong"));
            logger.info("Sent pong to session: {}", session.getId());
        }
    }

    public void broadcastGraph() throws IOException {
        Map<String, ServiceNode> graph = dependencyGraph.getGraph();
        logger.info("Broadcasting graph, services: {}, dependencyGraph: {}", graph.keySet(), dependencyGraph);
        if (graph.isEmpty() || dependencyGraph == null) {
            logger.warn("Graph is empty or dependencyGraph is null, no data to broadcast");
            return;
        }
        Map<String, Map<String, Double>> simplifiedGraph = new HashMap<>();
        for (Map.Entry<String, ServiceNode> entry : graph.entrySet()) {
            simplifiedGraph.put(entry.getKey(), entry.getValue().getMetrics());
        }
        String json = objectMapper.writeValueAsString(simplifiedGraph);
        logger.debug("Sending JSON: {}", json);
        List<String> openSessions = sessions.stream()
                .filter(WebSocketSession::isOpen)
                .map(WebSocketSession::getId)
                .collect(Collectors.toList());
        logger.info("Current open sessions: {}", openSessions);
        if (openSessions.isEmpty()) {
            logger.warn("No open sessions to broadcast to");
            return;
        }
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(json));
                logger.info("Sent data to session: {}", session.getId());
            } else {
                logger.warn("Session {} is closed, removing", session.getId());
                sessions.remove(session);
            }
        }
    }

    private void broadcastGraphToSession(WebSocketSession session) throws IOException {
        Map<String, ServiceNode> graph = dependencyGraph.getGraph();
        logger.info("Sending initial graph to session {}, services: {}, dependencyGraph: {}", session.getId(), graph.keySet(), dependencyGraph);
        if (graph.isEmpty() || dependencyGraph == null) {
            logger.warn("Graph is empty or dependencyGraph is null for initial broadcast to {}", session.getId());
            return;
        }
        Map<String, Map<String, Double>> simplifiedGraph = new HashMap<>();
        for (Map.Entry<String, ServiceNode> entry : graph.entrySet()) {
            simplifiedGraph.put(entry.getKey(), entry.getValue().getMetrics());
        }
        String json = objectMapper.writeValueAsString(simplifiedGraph);
        logger.debug("Sending initial JSON to {}: {}", session.getId(), json);
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(json));
            logger.info("Sent initial data to session: {}", session.getId());
        } else {
            logger.warn("Session {} is closed, cannot send initial data", session.getId());
        }
    }
}