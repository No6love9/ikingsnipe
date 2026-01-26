package com.ikingsnipe.casino.games;

import java.util.HashMap;
import java.util.Map;

/**
 * GameContext - Elite Titan Casino
 * 
 * Holds contextual information for a game session, such as player choices
 * (e.g., "hot" or "cold", "over" or "under").
 */
public class GameContext {
    private final Map<String, Object> data = new HashMap<>();

    public void set(String key, Object value) {
        data.put(key, value);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public String getString(String key) {
        Object val = data.get(key);
        return val != null ? val.toString() : null;
    }

    public boolean getBoolean(String key) {
        Object val = data.get(key);
        return val instanceof Boolean && (Boolean) val;
    }

    public int getInt(String key) {
        Object val = data.get(key);
        return val instanceof Integer ? (Integer) val : 0;
    }
}
