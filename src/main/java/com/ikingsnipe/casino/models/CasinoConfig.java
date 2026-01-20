package com.ikingsnipe.casino.models;

import org.dreambot.api.methods.input.Keyboard;


import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.utilities.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.*;

public class CasinoConfig {
    // Branding
    public String scriptName = "iKingSnipe GoatGang Casino";
    public String version = "14.0.0-GOATGANG";

    // Constants
    public static final int COINS_ID = 995;
    public static final int PLATINUM_TOKEN_ID = 13204;
    public static final long TOKEN_VALUE = 1000L;

    // Discord Configuration
    public String discordBotToken = "";
    public String discordWebhookUrl = "https://discord.com/api/webhooks/1462544141874499638/PsIKk3oEr_4xYA1DbPG-mTzDx1RecIgGNm6Ck5-5dwzXMgSgEc80w16kbFU5fhSKWMMz";
    public String discordAdminId = "1458983982522699799";
    public boolean discordEnabled = true;
    public boolean discordNotifyWins = true;
    public boolean discordNotifyLosses = true;
    public boolean discordShowSeeds = true;
    public String discordNotificationChannelId = "";

    // Database Configuration
    public String dbHost = "localhost";
    public String dbPort = "3306";
    public String dbName = "goatgang";
    public String dbUser = "root";
    public String dbPass = "";

    // Game Triggers & Commands
    public String cmdDiceDuel = "!dd";
    public String cmdCraps = "!c";
    public String cmdMid = "!mid";
    public String cmdOver = "!over";
    public String cmdUnder = "!under";
    public String cmdBalance = "!bal";
    public String cmdRakeback = "!rb";
    public String cmdTip = "!tip";
    public String cmdStats = "!stats";
    public String cmdGoatGang = "!gg";
    public String cmdHelp = "!help";
    public String cmdFlowerPoker = "!fp";
    public String cmdHotCold = "!hc";
    public String cmdBlackjack = "!bj";

    // Betting Limits
    public long minBet = 100_000L;
    public long maxBet = 100_000_000L;
    public double payoutMultiplier = 2.0;

    // Messaging
    public String msgWelcome = "Welcome to GoatGang! Type !help for games.";
    public String msgWin = "🎉 WINNER! %s won %s GP! (Result: %s)";
    public String msgLoss = "❌ LOSS! %s lost. (Result: %s)";
    public String msgBalance = "💰 %s, your balance is: %s GP.";
    public String msgStreaks = "🐐 GoatGang Recent Streaks: %s";
    public String tradeWelcome = "Welcome to GoatGang! Type !help for games. Hash: %s";
    public String winMessage = "🎉 WINNER! %s won %s GP! (Result: %s) [Seed: %s]";
    public String lossMessage = "❌ LOSS! %s lost. (Result: %s) Better luck next time!";
    public String payoutMessage = "💰 Payout of %s GP sent to %s. Enjoy!";
    public String timeoutMessage = "⏳ Payout delayed for %s. Admin will collect manually.";

    // Location & Behavior
    public Tile targetTile = new Tile(3164, 3484, 0);
    public boolean autoAcceptTrades = true;
    public int adIntervalSeconds = 30;
    public boolean walkOnStart = true;
    public LocationPreset locationPreset = LocationPreset.GRAND_EXCHANGE_SW;
    public int customX = 3164, customY = 3484, customZ = 0;

    // Banking & Muling
    public boolean autoBank = true;
    public boolean skipBanking = false;
    public long restockThreshold = 10_000_000L;
    public long restockAmount = 100_000_000L;
    public boolean autoMule = false;
    public String muleName = "";
    public long muleThreshold = 500_000_000L;
    public long muleKeepAmount = 50_000_000L;

    // Humanization
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

    // Clan Chat
    public boolean clanChatEnabled = true;
    public String clanChatName = "GoatGang";
    public boolean clanChatAnnounceWins = true;
    public boolean clanChatRespondToCommands = true;
    public boolean notifyClanOnTrade = true;

    // Games Map
    public Map<String, GameSettings> games = new HashMap<>();
    public boolean useProvablyFair = true;
    public boolean chatAIEnabled = true;
    public boolean autoReplyToScamAccusations = true;
    public boolean autoAnnounceBigWins = true;
    public long bigWinThreshold = 50_000_000L;
    public boolean verifyTradeAmount = true;
    public int tradeTimeoutSeconds = 60;
    public String defaultGame = "craps";
    public boolean enableB2B = true;
    public TradeConfig tradeConfig = new TradeConfig();
    public TradeConfig.TradePreset tradePreset = TradeConfig.TradePreset.BALANCED;

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

    private static final String CONFIG_PATH = System.getProperty("user.home") + "/DreamBot/scripts/GoatGang/config.json";

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

    public void save() {
        try {
            File file = new File(CONFIG_PATH);
            file.getParentFile().mkdirs();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(this, writer);
                Logger.log("[Config] Settings saved successfully.");
            }
        } catch (IOException e) {
            Logger.error("[Config] Save failed: " + e.getMessage());
        }
    }

    public static CasinoConfig load() {
        try {
            File file = new File(CONFIG_PATH);
            if (!file.exists()) return new CasinoConfig();
            Gson gson = new Gson();
            try (FileReader reader = new FileReader(file)) {
                return gson.fromJson(reader, CasinoConfig.class);
            }
        } catch (IOException e) {
            return new CasinoConfig();
        }
    }
}
