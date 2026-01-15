package com.ikingsnipe.casino.managers;
import com.ikingsnipe.casino.models.PlayerSession;
import java.util.*;
public class SessionManager {
    private final Map<String, PlayerSession> sessions = new HashMap<>();
    public PlayerSession getSession(String user) { return sessions.computeIfAbsent(user, PlayerSession::new); }
}
