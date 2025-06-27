package com.nexlify.loadbalancer.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexlify.loadbalancer.model.ServiceNode;
import com.nexlify.loadbalancer.service.DependencyGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class DashboardWebSocketHandler extends TextWebSocketHandler {
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    @Autowired
    private DependencyGraph dependencyGraph;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    public void broadcastGraph() throws IOException {
        String json = objectMapper.writeValueAsString(dependencyGraph.getGraph());
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(json));
            }
        }
    }
}