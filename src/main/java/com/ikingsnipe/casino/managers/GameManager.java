package com.ikingsnipe.casino.managers;
import com.ikingsnipe.casino.games.*; import com.ikingsnipe.casino.models.CasinoConfig;
import java.util.*;
public class GameManager {
    private final Map<String, AbstractGame> games = new HashMap<>(); private final CasinoConfig config;
    public GameManager(CasinoConfig config) {
        this.config = config; games.put("dice", new DiceDuelGame()); games.put("flower", new FlowerPokerGame());
        games.put("craps", new CrapsGame()); games.put("blackjack", new BlackjackGame());
        games.put("hotcold", new HotColdGame()); games.put("55x2", new FiftyFiveGame());
    }
    public GameResult play(String type, long bet) {
        String k = type.toLowerCase(); AbstractGame g = games.getOrDefault(k, games.get("dice"));
        CasinoConfig.GameSettings s = config.games.getOrDefault(k, config.games.get("dice"));
        Map<String, Object> cfg = new HashMap<>(); cfg.put("payoutMultiplier", s.payoutMultiplier);
        cfg.put("winningNumbers", s.winningNumbers); return g.play(bet, cfg);
    }
}
