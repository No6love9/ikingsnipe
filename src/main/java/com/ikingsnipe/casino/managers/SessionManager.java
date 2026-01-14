package com.ikingsnipe.casino.managers;

import com.ikingsnipe.casino.models.PlayerSession;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private final ConcurrentHashMap<String, PlayerSession> activeSessions = new ConcurrentHashMap<>();

    public PlayerSession getSession(String username) {
        return activeSessions.computeIfAbsent(username, PlayerSession::new);
    }

    public void removeSession(String username) {
        activeSessions.remove(username);
    }

    public boolean hasActiveSession(String username) {
        return activeSessions.containsKey(username);
    }
}
