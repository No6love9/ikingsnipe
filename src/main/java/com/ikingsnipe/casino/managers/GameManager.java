package com.ikingsnipe.casino.managers;

import com.ikingsnipe.casino.games.*;
import com.ikingsnipe.casino.models.CasinoConfig;
import java.util.HashMap;
import java.util.Map;

public class GameManager {
    private final Map<String, AbstractGame> games = new HashMap<>();
    private final CasinoConfig config;

    public GameManager(CasinoConfig config) {
        this.config = config;
        games.put("craps", new CrapsGame());
        games.put("dice", new DiceDuelGame());
        games.put("flower", new FlowerPokerGame());
    }

    public GameResult play(String gameType, int betAmount) {
        AbstractGame game = games.get(gameType.toLowerCase());
        if (game == null) return null;
        
        return game.play(betAmount, config.gameSettings);
    }

    public String getRules(String gameType) {
        AbstractGame game = games.get(gameType.toLowerCase());
        return game != null ? game.getRules() : "Game not found.";
    }
}
