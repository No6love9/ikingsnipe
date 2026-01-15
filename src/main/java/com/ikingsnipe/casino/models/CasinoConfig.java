package com.ikingsnipe.casino.models;

import org.dreambot.api.methods.map.Tile;
import java.util.*;

public class CasinoConfig {
    // Constants
    public static final int COINS_ID = 995;
    public static final int PLATINUM_TOKEN_ID = 13204;
    public static final long TOKEN_VALUE = 1000L;

    // Betting Limits
    public long minBet = 1_000_000L;
    public long maxBet = 2_147_483_647L;

    // Location Settings
    public enum LocationPreset {
        GRAND_EXCHANGE("Grand Exchange", new Tile(3164, 3487, 0)),
        CLAN_HALL("Clan Hall Portal", new Tile(3388, 3161, 0)),
        CUSTOM("Custom Coordinates", null);

        private final String name;
        private final Tile tile;
        LocationPreset(String name, Tile tile) { this.name = name; this.tile = tile; }
        public String getName() { return name; }
        public Tile getTile() { return tile; }
    }

    public LocationPreset locationPreset = LocationPreset.GRAND_EXCHANGE;
    public int customX = 3164, customY = 3487, customZ = 0;
    public boolean walkOnStart = true;

    // Banking & Muling Settings
    public boolean autoBank = true;
    public long restockThreshold = 10_000_000L;
    public long restockAmount = 100_000_000L;
    public boolean autoMule = false;
    public String muleName = "";
    public long muleThreshold = 500_000_000L;
    public long muleKeepAmount = 50_000_000L;

    // Humanization Settings
    public boolean enableMicroBreaks = true;
    public int breakFrequencyMinutes = 60;
    public int breakDurationMinutes = 5;
    public boolean enableCameraJitter = true;
    public boolean enableMouseFatigue = true;

    // Game Settings
    public String defaultGame = "dice";
    public Map<String, GameSettings> games = new HashMap<>();
    public boolean useProvablyFair = true;
    
    // Jackpot Settings
    public boolean jackpotEnabled = true;
    public long currentJackpot = 0;
    public double jackpotContributionPercent = 1.0; // 1% of bets go to jackpot

    // Messaging & AI
    public List<String> adMessages = new ArrayList<>(Arrays.asList(
        "[snipes♧scripts] Chasing Craps | Dice War | Jackpot | !c !dw !b2b | Trade me!",
        "Elite Casino is OPEN! !c !dw !b2b for big wins!",
        "Safe & Fast Payouts | Chasing Craps | Dice War | Trade to start!"
    ));
    public boolean enableAntiMute = true;
    public int adIntervalSeconds = 30;
    public String tradeWelcome = "Welcome! Type !c for Craps or !dw for Dice War. Hash: %s";
    public String winMessage = "WINNER! %s won %s GP! (Result: %s) [Seed: %s]";
    public String lossMessage = "LOSS! %s lost. (Result: %s)";
    public boolean chatAIEnabled = true;

    // Discord Webhook
    public String discordWebhookUrl = "";
    public boolean discordEnabled = false;
    public boolean discordNotifyWins = true;
    public boolean discordNotifyLosses = true;
    public boolean discordShowSeeds = true;

    // ==================== TRADE CONFIGURATION ====================
    /** Trade-specific configuration */
    public TradeConfig tradeConfig = new TradeConfig();
    
    /** Selected trade preset */
    public TradeConfig.TradePreset tradePreset = TradeConfig.TradePreset.BALANCED;

    public CasinoConfig() {
        initializeDefaultGames();
    }

    private void initializeDefaultGames() {
        games.put("dice", new GameSettings("Dice Duel", 2.0, true));
        games.put("flower", new GameSettings("Flower Poker", 2.0, true));
        games.put("craps", new GameSettings("Chasing Craps", 3.0, true, 7, 9, 12));
        games.put("blackjack", new GameSettings("Blackjack", 2.5, true));
        games.put("hotcold", new GameSettings("Hot/Cold", 2.0, true));
        games.put("55x2", new GameSettings("55x2", 2.0, true));
        games.put("dicewar", new GameSettings("Dice War", 1.95, true));
    }

    public Tile getTargetTile() {
        return locationPreset == LocationPreset.CUSTOM ? new Tile(customX, customY, customZ) : locationPreset.getTile();
    }
    
    /**
     * Apply a trade preset to the trade configuration
     */
    public void applyTradePreset(TradeConfig.TradePreset preset) {
        this.tradePreset = preset;
        this.tradeConfig = TradeConfig.fromPreset(preset);
    }

    public static class GameSettings {
        public String name;
        public double multiplier;
        public boolean enabled;
        public List<Integer> winningNumbers = new ArrayList<>();
        
        public GameSettings(String name, double multiplier, boolean enabled) {
            this.name = name; this.multiplier = multiplier; this.enabled = enabled;
        }
        
        public GameSettings(String name, double multiplier, boolean enabled, Integer... nums) {
            this(name, multiplier, enabled);
            this.winningNumbers.addAll(Arrays.asList(nums));
        }
    }
}
