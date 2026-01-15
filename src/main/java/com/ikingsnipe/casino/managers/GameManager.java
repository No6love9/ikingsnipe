package com.ikingsnipe.casino.managers;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;
import com.ikingsnipe.casino.games.impl.*;
import com.ikingsnipe.casino.models.CasinoConfig;
import java.util.*;

public class GameManager {
    private final Map<String, AbstractGame> games = new HashMap<>();
    private final CasinoConfig config;

    public GameManager(CasinoConfig config) {
        this.config = config;
        games.put("dice", new DiceDuelGame());
        games.put("flower", new FlowerPokerGame());
        games.put("blackjack", new BlackjackGame());
        games.put("hotcold", new HotColdGame());
        games.put("55x2", new FiftyFiveGame());
        games.put("craps", new CrapsGame());
        games.put("dicewar", new DiceWarGame());
    }

    public GameResult play(String type, long bet) {
        String key = type.toLowerCase();
        AbstractGame game = games.getOrDefault(key, games.get("dice"));
        CasinoConfig.GameSettings settings = config.games.getOrDefault(key, config.games.get("dice"));
        return game.play(bet, settings.multiplier);
    }

    public AbstractGame getGame(String type) {
        return games.get(type.toLowerCase());
    }
}
