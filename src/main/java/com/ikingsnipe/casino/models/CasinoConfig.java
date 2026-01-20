package com.ikingsnipe.casino.models;

import org.dreambot.api.methods.map.Tile;
import java.util.*;

public class CasinoConfig {
    // Branding
    public String scriptName = "iKingSnipe GoatGang Casino";
    public String version = "12.0";

    // Constants
    public static final int COINS_ID = 995;
    public static final int PLATINUM_TOKEN_ID = 13204;
    public static final long TOKEN_VALUE = 1000L;

    // Betting Limits
    public long minBet = 100_000L;
    public long maxBet = 100_000_000L;

    // Location Settings
    public enum LocationPreset {
        GRAND_EXCHANGE_SW("GE Southwest", new Tile(3164, 3484, 0)),
        CLAN_HALL("Clan Hall", new Tile(3388, 3161, 0)),
        CUSTOM("Custom", null);

        private final String name;
        private final Tile tile;
        LocationPreset(String name, Tile tile) { this.name = name; this.tile = tile; }
        public String getName() { return name; }
        public Tile getTile() { return tile; }
    }

    public LocationPreset locationPreset = LocationPreset.GRAND_EXCHANGE_SW;
    public int customX = 3164, customY = 3484, customZ = 0;
    public boolean walkOnStart = true;

    // Banking & Muling Settings
    public boolean autoBank = true;
    public boolean skipBanking = false;
    public long restockThreshold = 10_000_000L;
    public long restockAmount = 100_000_000L;
    public boolean autoMule = false;
    public String muleName = "";
    public long muleThreshold = 500_000_000L;
    public long muleKeepAmount = 50_000_000L;

    // Humanization & Anti-Ban Settings
    public boolean enableMicroBreaks = true;
    public int breakFrequencyMinutes = 60;
    public int breakDurationMinutes = 5;
    public boolean enableCameraJitter = true;
    public boolean enableMouseFatigue = true;
    public boolean enableRandomWalking = true;
    public boolean enableSmartAdaptiveLogic = true;
    public boolean enableAntiMute = true;
    public int reactionTimeMin = 600;
    public int reactionTimeMax = 1200;

    // Game Settings & Command Triggers
    public Map<String, GameSettings> games = new HashMap<>();
    public boolean useProvablyFair = true;
    
    // Command Triggers (Configurable)
    public String cmdBalance = "!bal";
    public String cmdStats = "!stats";
    public String cmdCraps = "!c";
    public String cmdDiceDuel = "!dd";
    public String cmdFlowerPoker = "!fp";
    public String cmdHotCold = "!hc";
    public String cmdBlackjack = "!bj";
    public String cmdHelp = "!help";

    // Messaging & Game Messages
    public List<String> adMessages = new ArrayList<>(Arrays.asList(
        "🎰 GoatGang Casino | Craps 3x Payout | !c to play!",
        "💰 Trusted Host | Instant Trades | !stats",
        "🎲 Verifiable RNG | Join Clan: GoatGang"
    ));
    public int adIntervalSeconds = 30;
    public String tradeWelcome = "Welcome to GoatGang! Type !help for games. Hash: %s";
    public String winMessage = "🎉 WINNER! %s won %s GP! (Result: %s) [Seed: %s]";
    public String lossMessage = "❌ LOSS! %s lost. (Result: %s) Better luck next time!";
    public String payoutMessage = "💰 Payout of %s GP sent to %s. Enjoy!";
    public String timeoutMessage = "⏳ Payout delayed for %s. Admin will collect manually.";
    
    public boolean chatAIEnabled = true;
    public boolean autoReplyToScamAccusations = true;
    public boolean autoAnnounceBigWins = true;
    public long bigWinThreshold = 50_000_000L;

    // Clan Chat Settings
    public boolean clanChatEnabled = true;
    public String clanChatName = "GoatGang";
    public boolean clanChatAnnounceWins = true;
    public boolean clanChatRespondToCommands = true;
    public boolean notifyClanOnTrade = true;

    // Discord Configuration
    public String discordToken = "";
    public String discordWebhookUrl = "";
    public boolean discordEnabled = false;
    public boolean discordNotifyWins = true;
    public boolean discordNotifyLosses = true;
    public boolean discordShowSeeds = true;
    public String discordNotificationChannelId = "";

    // Trade Configuration
    public boolean autoAcceptTrades = true;
    public boolean verifyTradeAmount = true;
    public int tradeTimeoutSeconds = 60;
    
    // Legacy fields for compatibility
    public String defaultGame = "craps";
    public boolean enableB2B = true;
    public TradeConfig tradeConfig = new TradeConfig();
    public TradeConfig.TradePreset tradePreset = TradeConfig.TradePreset.BALANCED;

    public CasinoConfig() {
        initializeDefaultGames();
    }

    private void initializeDefaultGames() {
        games.put("dice", new GameSettings("Dice Duel", 2.0, true, "!dd"));
        games.put("flower", new GameSettings("Flower Poker", 2.0, true, "!fp"));
        games.put("craps", new GameSettings("Chasing Craps", 3.0, true, "!c", 7, 9, 12));
        games.put("blackjack", new GameSettings("Blackjack", 2.5, true, "!bj"));
        games.put("hotcold", new GameSettings("Hot/Cold", 2.0, true, "!hc"));
    }

    public Tile getTargetTile() {
        return locationPreset == LocationPreset.CUSTOM ? new Tile(customX, customY, customZ) : locationPreset.getTile();
    }

    public static class GameSettings {
        public String name;
        public double multiplier;
        public boolean enabled;
        public String trigger;
        public List<Integer> winningNumbers = new ArrayList<>();
        
        public GameSettings(String name, double multiplier, boolean enabled, String trigger) {
            this.name = name; this.multiplier = multiplier; this.enabled = enabled; this.trigger = trigger;
        }
        
        public GameSettings(String name, double multiplier, boolean enabled, String trigger, Integer... nums) {
            this(name, multiplier, enabled, trigger);
            this.winningNumbers.addAll(Arrays.asList(nums));
        }
    }
}
