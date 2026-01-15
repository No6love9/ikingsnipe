package com.ikingsnipe.casino.models;
import org.dreambot.api.methods.map.Tile;
import java.util.*;
public class CasinoConfig {
    public static final int COINS_ID = 995;
    public static final int PLATINUM_TOKEN_ID = 13204;
    public static final int TOKEN_VALUE = 1000;
    public long minBet = 1000000;
    public long maxBet = 2147000000L;
    public int adIntervalMs = 20000;
    public int tradeTimeoutMs = 60000;
    public int loopDelayMinMs = 200;
    public int loopDelayMaxMs = 400;
    public int messageDelayMinMs = 600;
    public int messageDelayMaxMs = 1200;
    public enum StartLocation {
        GRAND_EXCHANGE("Grand Exchange", new Tile(3164, 3487, 0)),
        CLAN_HALL_PORTAL("Clan Hall Portal", new Tile(3388, 3161, 0)),
        CUSTOM("Custom", null);
        private final String name; private final Tile tile;
        StartLocation(String name, Tile tile) { this.name = name; this.tile = tile; }
        public String getName() { return name; } public Tile getTile() { return tile; }
    }
    public StartLocation startLocation = StartLocation.GRAND_EXCHANGE;
    public int customX = 3164, customY = 3487, customZ = 0;
    public boolean walkToLocationOnStart = true;
    public boolean autoRestock = true;
    public long restockThreshold = 10000000;
    public long restockAmount = 100000000;
    public String defaultGame = "dice";
    public boolean useProvablyFair = true;
    public Map<String, GameSettings> games = new HashMap<>();
    public List<String> blacklist = new ArrayList<>();
    public String adMessage = "[Elite Casino] Dice | Flower Poker | Craps | Blackjack | Hot/Cold | Provably Fair! Trade me!";
    public String tradeWelcome = "Welcome! Provably Fair Hash: %s";
    public String gameSelectionMsg = "Games: !dice !flower !craps !blackjack !hotcold";
    public String tradeSafety = "Place bet and accept. Good luck!";
    public String winAnnouncement = "WIN! %s %s and won %s GP! Seed: %s";
    public String lossAnnouncement = "LOSS! %s %s.";
    public String blacklistMsg = "Sorry %s, you are blacklisted.";
    public CasinoConfig() {
        games.put("dice", new GameSettings("Dice Duel", 2.0, true, "Roll 1-100. Higher wins."));
        games.put("flower", new GameSettings("Flower Poker", 2.0, true, "Best hand wins."));
        games.put("craps", new GameSettings("Craps", 2.0, true, "7/11 wins."));
        games.get("craps").winningNumbers = Arrays.asList(7, 11);
        games.put("blackjack", new GameSettings("Blackjack", 2.5, true, "21 wins."));
        games.put("hotcold", new GameSettings("Hot/Cold", 2.0, true, "50/50 chance."));
        games.put("55x2", new GameSettings("55x2", 2.0, true, "55+ wins."));
    }
    public Tile getStartLocationTile() { return startLocation == StartLocation.CUSTOM ? new Tile(customX, customY, customZ) : startLocation.getTile(); }
    public String getStartLocationName() { return startLocation == StartLocation.CUSTOM ? "Custom" : startLocation.getName(); }
    public static class GameSettings {
        public String displayName, rules; public double payoutMultiplier; public boolean enabled;
        public List<Integer> winningNumbers = new ArrayList<>(), losingNumbers = new ArrayList<>();
        public int minRoll = 1, maxRoll = 100;
        public GameSettings(String d, double p, boolean e, String r) { displayName = d; payoutMultiplier = p; enabled = e; rules = r; }
    }
}
