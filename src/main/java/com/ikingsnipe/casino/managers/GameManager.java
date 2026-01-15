package com.ikingsnipe.casino.managers;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;
import com.ikingsnipe.casino.games.impl.*;
import com.ikingsnipe.casino.models.CasinoConfig;
import org.dreambot.api.utilities.Logger;
import java.util.*;

/**
 * Enhanced Game Manager for snipes♧scripts Enterprise
 * Manages game instances and injects configuration settings
 */
public class GameManager {
    private final Map<String, AbstractGame> games = new HashMap<>();
    private final CasinoConfig config;

    public GameManager(CasinoConfig config) {
        this.config = config;
        
        // Initialize game instances
        games.put("dice", new DiceDuelGame());
        games.put("flower", new FlowerPokerGame());
        games.put("blackjack", new BlackjackGame());
        games.put("hotcold", new HotColdGame());
        games.put("55x2", new FiftyFiveGame());
        games.put("craps", new CrapsGame());
        games.put("dicewar", new DiceWarGame());
        
        // Inject settings from config
        syncSettings();
    }

    /**
     * Synchronize game instances with current configuration
     */
    public void syncSettings() {
        for (Map.Entry<String, AbstractGame> entry : games.entrySet()) {
            CasinoConfig.GameSettings s = config.games.get(entry.getKey());
            if (s != null) {
                entry.getValue().setSettings(s);
            }
        }
        Logger.log("[GameManager] Game settings synchronized with config.");
    }

    public GameResult play(String type, String player, long bet, String seed) {
        String key = type.toLowerCase();
        AbstractGame game = games.get(key);
        
        if (game == null) {
            Logger.log("[GameManager] Unknown game type: " + type + ". Defaulting to Dice Duel.");
            game = games.get("dice");
        }
        
        return game.play(player, bet, seed);
    }

    public AbstractGame getGame(String type) {
        return games.get(type.toLowerCase());
    }
    
    public boolean isGameEnabled(String type) {
        CasinoConfig.GameSettings s = config.games.get(type.toLowerCase());
        return s != null && s.enabled;
    }
}
