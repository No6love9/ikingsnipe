package com.ikingsnipe;

import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.methods.friend.Friends;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.wrappers.widgets.message.Message;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.Client;
import org.dreambot.api.data.GameState;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.ChatListener;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.walking.impl.Walking;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * ELITE TITAN CASINO PRO - ENHANCED FEATURE-RICH VERSION
 * All-in-one casino host with full GUI, trade management, and 3 game types
 * 
 * Enhanced Features:
 * - Added more chat commands: !odds, !hoststats, !top
 * - Improved !help with all commands listed
 * - Added game odds calculations (win probabilities and house edge notes)
 * - Top players leaderboard by profit
 * - Host overall statistics
 * - Ensured all game logic is fully implemented with fairness seeds
 * - Added more advertising messages for variety
 * - Chat cooldown to prevent spam (3-second global)
 * 
 * @author ikingsnipe
 * @version 2.1.0
 */
@ScriptManifest(
    name = "Elite Titan Casino Pro Enhanced",
    author = "ikingsnipe",
    version = 2.1,
    description = "Enhanced casino system with Craps, Dice Duel, and Flower Poker. Full GUI control and more chat features.",
    category = Category.MISC
)
public class EliteTitanCasino extends AbstractScript implements ChatListener {
    
    // ==================== CONSTANTS ====================
    private static final int COINS_ID = 995;
    private static final int DICE_ID = 10403;
    private static final int WHEEL_ID = 10404;
    private static final int ROULETTE_ID = 10405;
    private static final long CHAT_COOLDOWN_MS = 3000; // 3-second chat cooldown
    
    // ==================== CONFIGURATION ====================
    private static class CasinoConfig {
        // Betting
        int minBet = 1000;
        int maxBet = 1000000;
        int maxActiveSessions = 3;
        
        // Trading
        boolean autoAcceptTrades = true;
        int tradeTimeoutSeconds = 30;
        
        // Advertising
        boolean enableAdvertising = true;
        int adCooldownSeconds = 15;
        java.util.List<String> adMessages = new ArrayList<>();
        
        // Game Settings
        boolean crapsEnabled = true;
        int crapsPayoutMultiplier = 3;
        java.util.List<Integer> crapsWinningNumbers = Arrays.asList(7, 9, 12);
        
        boolean diceEnabled = true;
        int dicePayoutMultiplier = 2;
        boolean diceAllowTies = false;
        
        boolean flowerEnabled = true;
        int flowerTypes = 6;
        boolean flowerEscalatingPayouts = true;
        Map<Integer, Integer> flowerPayouts = new HashMap<>();
        
        // Messages
        Map<String, String> winMessages = new HashMap<>();
        Map<String, String> lossMessages = new HashMap<>();
        
        CasinoConfig() {
            // Enhanced ads with more variety
            adMessages.add("🎰 Elite Casino | Fast Payouts | High Limits! | !rules");
            adMessages.add("💰 Trusted Casino Host | Instant Trades | Fair Games!");
            adMessages.add("🎲 Professional Casino Service | Craps, Dice, Flower Poker!");
            adMessages.add("🌟 Join Now! Big Wins Await | !odds for probabilities");
            adMessages.add("🤑 High Roller Casino | Up to 10x Payouts | !help");
            
            // Default win messages
            winMessages.put("craps", "🎉 Congratulations! Craps win! You rolled {total} and won {payout} GP!");
            winMessages.put("dice", "⚄ You won the dice duel! {playerRoll} vs {hostRoll} = {payout} GP!");
            winMessages.put("flower", "🌸 Flower Poker victory! {handRank} beats the host! Won {payout} GP!");
            
            // Default loss messages
            lossMessages.put("craps", "❌ Better luck next time! You rolled {total}.");
            lossMessages.put("dice", "😔 House wins with {hostRoll} vs your {playerRoll}.");
            lossMessages.put("flower", "🌿 The host's {handRank} beats your hand.");
            
            // Flower poker payouts
            flowerPayouts.put(7, 10); // Five of a kind
            flowerPayouts.put(6, 6);  // Four of a kind
            flowerPayouts.put(5, 4);  // Full house
            flowerPayouts.put(4, 3);  // Three of a kind
            flowerPayouts.put(3, 2);  // Two pair
            flowerPayouts.put(2, 2);  // One pair
            flowerPayouts.put(1, 1);  // High flower
        }
        
        String getRandomAd() {
            if (adMessages.isEmpty()) return "🎰 Elite Casino hosting now!";
            return adMessages.get(new Random().nextInt(adMessages.size()));
        }
        
        String getWinMessage(String game, int payout, Map<String, String> placeholders) {
            String msg = winMessages.getOrDefault(game, "Congratulations! You won!");
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
            }
            return msg.replace("{payout}", formatCoins(payout));
        }
        
        String getLossMessage(String game, Map<String, String> placeholders) {
            String msg = lossMessages.getOrDefault(game, "Better luck next time!");
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
            }
            return msg;
        }
        
        private String formatCoins(int amount) {
            if (amount >= 1000000) return String.format("%.1fM", amount / 1000000.0);
            if (amount >= 1000) return String.format("%.1fK", amount / 1000.0);
            return String.valueOf(amount);
        }
    }
    
    // ==================== GAME RESULT CLASS ====================
    private static class GameResult {
        boolean win;
        String message;
        int payout;
        String gameType;
        Map<String, Object> details;
        
        GameResult(boolean win, String message, int payout, String gameType, Map<String, Object> details) {
            this.win = win;
            this.message = message;
            this.payout = payout;
            this.gameType = gameType;
            this.details = details;
        }
        
        String getSummary() {
            return String.format("[%s] %s | %s", gameType, win ? "WIN" : "LOSS", message);
        }
    }
    
    // ==================== PLAYER SESSION ====================
    private static class PlayerSession {
        String playerName;
        String gameType;
        int betAmount;
        long createdAt;
        SessionState state;
        boolean win;
        int payout;
        GameResult gameResult;
        Timer timer;
        
        enum SessionState {
            CREATED, AWAITING_TRADE, TRADE_OPEN, TRADE_ACCEPTED,
            GAME_LOCKED, GAME_COMPLETE, PAYOUT_PENDING, COMPLETED, FAILED
        }
        
        PlayerSession(String playerName, String gameType, int betAmount) {
            this.playerName = playerName;
            this.gameType = gameType;
            this.betAmount = betAmount;
            this.createdAt = System.currentTimeMillis();
            this.state = SessionState.CREATED;
            this.timer = new Timer();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - createdAt > 300000; // 5 minutes
        }
        
        void setState(SessionState state) {
            this.state = state;
            this.timer.reset();
        }
    }
    
    // ==================== PLAYER STATS ====================
    private static class PlayerStats {
        int gamesPlayed = 0;
        int gamesWon = 0;
        int totalWagered = 0;
        int totalPayout = 0;
        
        void recordGame(boolean win, int wager, int payout) {
            gamesPlayed++;
            if (win) gamesWon++;
            totalWagered += wager;
            totalPayout += payout;
        }
        
        int getProfit() {
            return totalPayout - totalWagered;
        }
        
        double getWinRate() {
            return gamesPlayed > 0 ? (gamesWon * 100.0 / gamesPlayed) : 0;
        }
    }
    
    // ==================== SYSTEM STATE ====================
    private enum SystemState {
        STOPPED, INITIALIZING, RUNNING, ADVERTISING, TRADING,
        GAMING, PAYOUT, ERROR_RECOVERY, SHUTTING_DOWN
    }
    
    // ==================== GAME ENGINE ====================
    private interface GameEngine {
        GameResult play(int betAmount, String playerName);
        String getRules();
        String getOdds();
        boolean isEnabled();
        void setEnabled(boolean enabled);
    }
    
    private class CrapsEngine implements GameEngine {
        private boolean enabled = true;
        
        @Override
        public GameResult play(int betAmount, String playerName) {
            Random rng = new Random(generateSeed(playerName));
            
            int die1 = rng.nextInt(6) + 1;
            int die2 = rng.nextInt(6) + 1;
            int total = die1 + die2;
            
            boolean win = config.crapsWinningNumbers.contains(total);
            int payout = win ? betAmount * config.crapsPayoutMultiplier : 0;
            
            Map<String, Object> details = new HashMap<>();
            details.put("dice1", die1);
            details.put("dice2", die2);
            details.put("total", total);
            details.put("winningNumbers", config.crapsWinningNumbers);
            
            String message = String.format("🎲 Craps: %d + %d = %d | %s",
                die1, die2, total, win ? "WIN!" : "LOSE");
            
            return new GameResult(win, message, payout, "craps", details);
        }
        
        @Override
        public String getRules() {
            return String.format(
                "🎲 Craps Rules:\n" +
                "• Roll 2 six-sided dice\n" +
                "• Win %dx bet on total: %s\n" +
                "• Any other total loses\n" +
                "• Min bet: %,d GP | Max bet: %,d GP",
                config.crapsPayoutMultiplier,
                config.crapsWinningNumbers.toString(),
                config.minBet,
                config.maxBet
            );
        }
        
        @Override
        public String getOdds() {
            int ways = 0;
            Map<Integer, Integer> diceWays = new HashMap<>();
            diceWays.put(2, 1); diceWays.put(3, 2); diceWays.put(4, 3); diceWays.put(5, 4);
            diceWays.put(6, 5); diceWays.put(7, 6); diceWays.put(8, 5); diceWays.put(9, 4);
            diceWays.put(10, 3); diceWays.put(11, 2); diceWays.put(12, 1);
            
            for (int n : config.crapsWinningNumbers) {
                ways += diceWays.getOrDefault(n, 0);
            }
            double winProb = (ways / 36.0) * 100;
            return String.format("Craps Odds: %.2f%% win chance | %dx payout | House edge ~%.2f%%",
                winProb, config.crapsPayoutMultiplier, 100 - (winProb * config.crapsPayoutMultiplier));
        }
        
        @Override public boolean isEnabled() { return enabled && config.crapsEnabled; }
        @Override public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
    
    private class DiceDuelEngine implements GameEngine {
        private boolean enabled = true;
        
        @Override
        public GameResult play(int betAmount, String playerName) {
            Random rng = new Random(generateSeed(playerName));
            
            int playerRoll, hostRoll;
            do {
                playerRoll = rng.nextInt(6) + 1;
                hostRoll = rng.nextInt(6) + 1;
            } while (playerRoll == hostRoll && !config.diceAllowTies);
            
            boolean win = playerRoll > hostRoll;
            int payout = win ? betAmount * config.dicePayoutMultiplier : 0;
            
            Map<String, Object> details = new HashMap<>();
            details.put("playerRoll", playerRoll);
            details.put("hostRoll", hostRoll);
            details.put("difference", Math.abs(playerRoll - hostRoll));
            
            String message;
            if (playerRoll == hostRoll) {
                message = String.format("⚄ Dice Duel: %d vs %d | TIE!", playerRoll, hostRoll);
            } else {
                message = String.format("⚄ Dice Duel: %d vs %d | %s WINS!",
                    playerRoll, hostRoll, win ? "PLAYER" : "HOST");
            }
            
            return new GameResult(win, message, payout, "dice", details);
        }
        
        @Override
        public String getRules() {
            return String.format(
                "⚄ Dice Duel Rules:\n" +
                "• Player and host each roll 1 die\n" +
                "• Higher roll wins %dx bet\n" +
                "• Ties: %s\n" +
                "• Min bet: %,d GP | Max bet: %,d GP",
                config.dicePayoutMultiplier,
                config.diceAllowTies ? "Push (no winner)" : "Reroll until winner",
                config.minBet,
                config.maxBet
            );
        }
        
        @Override
        public String getOdds() {
            double winProb;
            String tieNote;
            if (config.diceAllowTies) {
                winProb = (15.0 / 36) * 100; // 15 win, 15 lose, 6 tie (house keeps on tie)
                tieNote = " (ties favor house)";
            } else {
                winProb = 50.0; // Reroll ties, symmetric
                tieNote = " (reroll ties)";
            }
            return String.format("Dice Duel Odds: %.2f%% win chance%s | %dx payout | House edge ~%.2f%%",
                winProb, tieNote, config.dicePayoutMultiplier, 100 - (winProb * config.dicePayoutMultiplier / 100));
        }
        
        @Override public boolean isEnabled() { return enabled && config.diceEnabled; }
        @Override public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
    
    private class FlowerPokerEngine implements GameEngine {
        private boolean enabled = true;
        
        @Override
        public GameResult play(int betAmount, String playerName) {
            Random rng = new Random(generateSeed(playerName));
            
            // Generate hands
            int[] playerHand = generateHand(rng);
            int[] hostHand = generateHand(rng);
            
            // Evaluate hands
            int playerRank = evaluateHand(playerHand);
            int hostRank = evaluateHand(hostHand);
            
            boolean win = playerRank > hostRank;
            int payoutMultiplier = getPayoutMultiplier(playerRank);
            int payout = win ? betAmount * payoutMultiplier : 0;
            
            Map<String, Object> details = new HashMap<>();
            details.put("playerRank", playerRank);
            details.put("hostRank", hostRank);
            details.put("playerRankName", getRankName(playerRank));
            details.put("hostRankName", getRankName(hostRank));
            details.put("payoutMultiplier", payoutMultiplier);
            
            String message = String.format("🌸 Flower Poker: %s vs %s | %s",
                getRankName(playerRank), getRankName(hostRank),
                win ? "PLAYER WINS!" : (playerRank == hostRank ? "TIE (house wins)" : "HOST WINS"));
            
            return new GameResult(win, message, payout, "flower", details);
        }
        
        private int[] generateHand(Random rng) {
            int[] hand = new int[5];
            for (int i = 0; i < 5; i++) {
                hand[i] = rng.nextInt(config.flowerTypes) + 1;
            }
            return hand;
        }
        
        private int evaluateHand(int[] hand) {
            Map<Integer, Integer> freq = new HashMap<>();
            for (int flower : hand) freq.put(flower, freq.getOrDefault(flower, 0) + 1);
            
            java.util.List<Integer> counts = new ArrayList<>(freq.values());
            counts.sort(Collections.reverseOrder());
            
            if (counts.get(0) == 5) return 7;
            if (counts.get(0) == 4) return 6;
            if (counts.get(0) == 3 && counts.size() >= 2 && counts.get(1) == 2) return 5;
            if (counts.get(0) == 3) return 4;
            if (counts.get(0) == 2 && counts.size() >= 2 && counts.get(1) == 2) return 3;
            if (counts.get(0) == 2) return 2;
            return 1;
        }
        
        private String getRankName(int rank) {
            switch (rank) {
                case 7: return "Five of a kind";
                case 6: return "Four of a kind";
                case 5: return "Full house";
                case 4: return "Three of a kind";
                case 3: return "Two pair";
                case 2: return "One pair";
                case 1: return "High flower";
                default: return "Unknown";
            }
        }
        
        private int getPayoutMultiplier(int rank) {
            if (config.flowerEscalatingPayouts) {
                return config.flowerPayouts.getOrDefault(rank, 1);
            }
            return config.dicePayoutMultiplier;
        }
        
        @Override
        public String getRules() {
            StringBuilder sb = new StringBuilder();
            sb.append("🌸 Flower Poker Rules:\n");
            sb.append("• Both get 5 random flowers (1-").append(config.flowerTypes).append(")\n");
            sb.append("• Hand rankings:\n");
            
            if (config.flowerEscalatingPayouts) {
                for (int i = 7; i >= 1; i--) {
                    sb.append("  ").append(getRankName(i)).append(": ").append(getPayoutMultiplier(i)).append("x\n");
                }
            } else {
                sb.append("  Higher hand wins ").append(config.dicePayoutMultiplier).append("x bet\n");
            }
            sb.append("• Ties in rank: House wins\n");
            sb.append("• Min bet: ").append(formatCoins(config.minBet)).append("\n");
            sb.append("• Max bet: ").append(formatCoins(config.maxBet));
            return sb.toString();
        }
        
        @Override
        public String getOdds() {
            // Approximate: symmetric but ties favor house, variable payout makes edge complex
            return "Flower Poker Odds: ~47-50% win chance (ties favor house) | Variable 1-10x payout | House edge varies by hand strength";
        }
        
        @Override public boolean isEnabled() { return enabled && config.flowerEnabled; }
        @Override public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        private String formatCoins(int amount) {
            if (amount >= 1000000) return String.format("%.1fM", amount / 1000000.0);
            if (amount >= 1000) return String.format("%.1fK", amount / 1000.0);
            return String.format("%,d", amount);
        }
    }
    
    // ==================== CHAT PARSER ====================
    private class ChatParser {
        private final Pattern BET_PATTERN = Pattern.compile(
            "!bet\\s+(\\d+[kKmM]?)\\s+(craps|dice|flower)", Pattern.CASE_INSENSITIVE);
        private final Pattern RULES_PATTERN = Pattern.compile(
            "!rules\\s*(craps|dice|flower)?", Pattern.CASE_INSENSITIVE);
        private final Pattern ODDS_PATTERN = Pattern.compile(
            "!odds\\s*(craps|dice|flower)?", Pattern.CASE_INSENSITIVE);
        private final Pattern STATS_PATTERN = Pattern.compile("!stats", Pattern.CASE_INSENSITIVE);
        private final Pattern HOSTSTATS_PATTERN = Pattern.compile("!hoststats", Pattern.CASE_INSENSITIVE);
        private final Pattern TOP_PATTERN = Pattern.compile("!top", Pattern.CASE_INSENSITIVE);
        private final Pattern HELP_PATTERN = Pattern.compile("!help|!commands", Pattern.CASE_INSENSITIVE);
        
        private long lastChatTime = 0;
        
        void processMessage(String sender, String message) {
            if (sender == null || sender.equals(Players.getLocal().getName())) return;
            
            long now = System.currentTimeMillis();
            if (now - lastChatTime < CHAT_COOLDOWN_MS) {
                return; // Spam protection
            }
            lastChatTime = now;
            
            try {
                Matcher betMatcher = BET_PATTERN.matcher(message);
                if (betMatcher.matches()) {
                    handleBet(sender, betMatcher.group(1), betMatcher.group(2));
                    return;
                }
                
                Matcher rulesMatcher = RULES_PATTERN.matcher(message);
                if (rulesMatcher.matches()) {
                    handleRules(sender, rulesMatcher.group(1));
                    return;
                }
                
                Matcher oddsMatcher = ODDS_PATTERN.matcher(message);
                if (oddsMatcher.matches()) {
                    handleOdds(sender, oddsMatcher.group(1));
                    return;
                }
                
                Matcher statsMatcher = STATS_PATTERN.matcher(message);
                if (statsMatcher.matches()) {
                    handleStats(sender);
                    return;
                }
                
                Matcher hostStatsMatcher = HOSTSTATS_PATTERN.matcher(message);
                if (hostStatsMatcher.matches()) {
                    handleHostStats(sender);
                    return;
                }
                
                Matcher topMatcher = TOP_PATTERN.matcher(message);
                if (topMatcher.matches()) {
                    handleTop(sender);
                    return;
                }
                
                Matcher helpMatcher = HELP_PATTERN.matcher(message);
                if (helpMatcher.matches()) {
                    handleHelp(sender);
                    return;
                }
            } catch (Exception e) {
                log("Chat parse error: " + e.getMessage());
            }
        }
        
        private void handleBet(String player, String betStr, String gameType) {
            // Check if game is enabled
            GameEngine game = getGameEngine(gameType);
            if (game == null || !game.isEnabled()) {
                sendMessage(player, gameType + " is currently disabled.");
                return;
            }
            
            // Parse bet amount
            int betAmount = parseBetAmount(betStr);
            if (betAmount <= 0) {
                sendMessage(player, "Invalid bet amount: " + betStr);
                return;
            }
            
            // Validate bet limits
            if (betAmount < config.minBet || betAmount > config.maxBet) {
                sendMessage(player, "Bet must be between " + 
                    formatCoins(config.minBet) + " and " + formatCoins(config.maxBet));
                return;
            }
            
            // Check for existing session
            if (activeSessions.containsKey(player)) {
                sendMessage(player, "You already have an active bet. Please complete it first.");
                return;
            }
            
            // Check session limit
            if (activeSessions.size() >= config.maxActiveSessions) {
                sendMessage(player, "Casino is at capacity. Please try again later.");
                return;
            }
            
            // Create session
            PlayerSession session = new PlayerSession(player, gameType, betAmount);
            activeSessions.put(player, session);
            
            // Send confirmation
            sendMessage(player, "✅ Accepted " + formatCoins(betAmount) + 
                " GP " + gameType + " bet. Opening trade...");
            
            // Open trade
            if (Trade.tradeWithPlayer(player)) {
                log("Trade opened with " + player);
            } else {
                sendMessage(player, "Failed to open trade. Please try again.");
                activeSessions.remove(player);
            }
        }
        
        private void handleRules(String player, String gameType) {
            if (gameType == null) {
                StringBuilder rules = new StringBuilder();
                rules.append("🎰 Elite Titan Casino - Available Games:\n");
                rules.append("Use !rules <game> for detailed rules\n\n");
                
                for (String game : Arrays.asList("craps", "dice", "flower")) {
                    GameEngine engine = getGameEngine(game);
                    if (engine != null && engine.isEnabled()) {
                        String rule = engine.getRules();
                        rules.append(game.toUpperCase()).append(": ")
                             .append(rule.substring(0, Math.min(50, rule.length())))
                             .append("...\n");
                    }
                }
                
                rules.append("\nBetting: !bet <amount> <game>\n");
                rules.append("Example: !bet 100k craps\n");
                rules.append("Use !odds <game> for probabilities");
                
                sendMessage(player, rules.toString());
            } else {
                GameEngine engine = getGameEngine(gameType);
                if (engine != null) {
                    sendMessage(player, engine.getRules());
                } else {
                    sendMessage(player, "Unknown game. Available: craps, dice, flower");
                }
            }
        }
        
        private void handleOdds(String player, String gameType) {
            if (gameType == null) {
                sendMessage(player, "Use !odds <game> for specific game odds. Games: craps, dice, flower");
                return;
            }
            
            GameEngine engine = getGameEngine(gameType);
            if (engine != null) {
                sendMessage(player, engine.getOdds());
            } else {
                sendMessage(player, "Unknown game. Available: craps, dice, flower");
            }
        }
        
        private void handleStats(String player) {
            PlayerStats stats = playerStats.get(player);
            if (stats != null) {
                String message = String.format(
                    "📊 Your Stats: %d games, %d wins (%.1f%%), Profit: %s GP, Wagered: %s GP",
                    stats.gamesPlayed,
                    stats.gamesWon,
                    stats.getWinRate(),
                    formatCoins(stats.getProfit()),
                    formatCoins(stats.totalWagered)
                );
                sendMessage(player, message);
            } else {
                sendMessage(player, "No stats found. Place your first bet with !bet");
            }
        }
        
        private void handleHostStats(String player) {
            String message = String.format(
                "🏠 Host Stats: Profit %s GP | Total Wagered %s GP | Host Wins/Losses %d/%d | Total Trades %d",
                formatCoins(totalProfit),
                formatCoins(totalWagered),
                totalLosses,  // Host wins = player losses
                totalWins,    // Host losses = player wins
                totalTrades
            );
            sendMessage(player, message);
        }
        
        private void handleTop(String player) {
            if (playerStats.isEmpty()) {
                sendMessage(player, "No player stats available yet.");
                return;
            }
            
            java.util.List<Map.Entry<String, PlayerStats>> sorted = new ArrayList<>(playerStats.entrySet());
            sorted.sort(Comparator.comparingInt(e -> -e.getValue().getProfit())); // Descending profit
            
            StringBuilder sb = new StringBuilder("🏆 Top 5 Players by Profit:\n");
            for (int i = 0; i < Math.min(5, sorted.size()); i++) {
                PlayerStats stats = sorted.get(i).getValue();
                sb.append(i + 1).append(". ").append(sorted.get(i).getKey())
                  .append(": ").append(formatCoins(stats.getProfit())).append(" GP")
                  .append(" (").append(stats.gamesWon).append(" wins)\n");
            }
            sendMessage(player, sb.toString());
        }
        
        private void handleHelp(String player) {
            String help = "🎰 Elite Titan Casino Commands:\n" +
                          "!bet <amount> <game> - Place a bet (e.g., !bet 100k craps)\n" +
                          "!rules [game] - Show game rules\n" +
                          "!odds [game] - Show game odds and probabilities\n" +
                          "!stats - View your personal statistics\n" +
                          "!hoststats - View host overall statistics\n" +
                          "!top - View top players leaderboard\n" +
                          "!help or !commands - Show this message\n" +
                          "Games: craps, dice, flower";
            sendMessage(player, help);
        }
        
        private int parseBetAmount(String betStr) {
            try {
                betStr = betStr.toUpperCase();
                if (betStr.endsWith("K")) {
                    return Integer.parseInt(betStr.substring(0, betStr.length() - 1)) * 1000;
                } else if (betStr.endsWith("M")) {
                    return Integer.parseInt(betStr.substring(0, betStr.length() - 1)) * 1000000;
                } else {
                    return Integer.parseInt(betStr.replace(",", ""));
                }
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        
        private void sendMessage(String player, String message) {
            Friends.sendMessage(player, message);
        }
        
        private String formatCoins(int amount) {
            if (amount >= 1000000) return String.format("%.1fM", amount / 1000000.0);
            if (amount >= 1000) return String.format("%.1fK", amount / 1000.0);
            return String.format("%,d", amount);
        }
    }
    
    // ==================== TRADE MANAGER ====================
    private class TradeManager {
        void process() {
            if (!Trade.isOpen()) return;
            
            String partner = Trade.getTradingWith();
            if (partner == null) {
                Trade.declineTrade();
                return;
            }
            
            PlayerSession session = activeSessions.get(partner);
            
            try {
                if (Trade.isOpen(1)) {
                    handleFirstScreen(partner, session);
                } else if (Trade.isOpen(2)) {
                    handleSecondScreen(partner, session);
                }
            } catch (Exception e) {
                log("Trade error with " + partner + ": " + e.getMessage());
                Trade.declineTrade();
                if (session != null) {
                    activeSessions.remove(partner);
                }
            }
        }
        
        private void handleFirstScreen(String partner, PlayerSession session) {
            if (session == null) {
                log("No session for " + partner + ", declining trade");
                Trade.declineTrade();
                return;
            }
            
            int offeredCoins = getOfferedCoins();
            if (offeredCoins != session.betAmount) {
                sendMessage(partner, "Bet mismatch. Expected " + 
                    formatCoins(session.betAmount) + ", got " + formatCoins(offeredCoins));
                Trade.declineTrade();
                activeSessions.remove(partner);
                return;
            }
            
            if (config.autoAcceptTrades) {
                Trade.acceptTrade();
                log("Accepted trade from " + partner);
            }
        }
        
        private void handleSecondScreen(String partner, PlayerSession session) {
            if (session != null) {
                Trade.acceptTrade();
                session.setState(PlayerSession.SessionState.TRADE_ACCEPTED);
                
                // Record wager
                totalWagered += session.betAmount;
                
                // Play game
                playGame(session);
            }
        }
        
        private int getOfferedCoins() {
            if (Trade.getTheirItems() == null) return 0;
            int total = 0;
            for (Item item : Trade.getTheirItems()) {
                if (item != null && item.getID() == COINS_ID) {
                    total += item.getAmount();
                }
            }
            return total;
        }
        
        private void sendMessage(String player, String message) {
            Friends.sendMessage(player, message);
        }
        
        private String formatCoins(int amount) {
            if (amount >= 1000000) return String.format("%.1fM", amount / 1000000.0);
            if (amount >= 1000) return String.format("%.1fK", amount / 1000.0);
            return String.valueOf(amount);
        }
    }
    
    // ==================== MAIN FIELDS ====================
    private CasinoConfig config = new CasinoConfig();
    private SystemState currentState = SystemState.STOPPED;
    private boolean systemRunning = false;
    private boolean emergencyStop = false;
    
    // Game engines
    private Map<String, GameEngine> gameEngines = new HashMap<>();
    
    // Player data
    private Map<String, PlayerSession> activeSessions = new ConcurrentHashMap<>();
    private Map<String, PlayerStats> playerStats = new ConcurrentHashMap<>();
    
    // Components
    private ChatParser chatParser = new ChatParser();
    private TradeManager tradeManager = new TradeManager();
    
    // Statistics
    private int totalProfit = 0;
    private int totalWagered = 0;
    private int totalWins = 0;
    private int totalLosses = 0;
    private int totalTrades = 0;
    
    // GUI
    private JFrame controlFrame;
    private JTabbedPane tabbedPane;
    private JButton startButton, stopButton, emergencyButton;
    private JCheckBox autoAcceptBox, enableAdsBox;
    private JCheckBox crapsToggle, diceToggle, flowerToggle;
    private JSpinner minBetSpinner, maxBetSpinner;
    private JTextArea logArea;
    private JTable sessionsTable;
    private DefaultTableModel sessionsModel;
    private JLabel profitLabel, activeLabel, statusLabel;
    
    // Timing
    private long lastAdTime = 0;
    private Timer stateTimer = new Timer();
    
    // ==================== MAIN METHODS ====================
    
    @Override
    public void onStart() {
        log("================================================");
        log("     ELITE TITAN CASINO PRO ENHANCED v2.1");
        log("================================================");
        
        // Initialize game engines
        gameEngines.put("craps", new CrapsEngine());
        gameEngines.put("dice", new DiceDuelEngine());
        gameEngines.put("flower", new FlowerPokerEngine());
        
        // Setup chat listener
        
        
        // Create GUI
        SwingUtilities.invokeLater(this::createGUI);
        
        log("System initialized. Open GUI to start casino.");
    }
    
    @Override
    public int onLoop() {
        if (!systemRunning || emergencyStop || !isLoggedIn()) {
            return 600;
        }
        
        try {
            // Update state based on conditions
            updateState();
            
            // Process based on state
            switch (currentState) {
                case RUNNING:
                    processRunning();
                    break;
                case ADVERTISING:
                    processAdvertising();
                    break;
                case TRADING:
                    processTrading();
                    break;
                case GAMING:
                    processGaming();
                    break;
                case PAYOUT:
                    processPayout();
                    break;
                case ERROR_RECOVERY:
                    processErrorRecovery();
                    break;
            }
            
            // Always process trades and cleanup
            tradeManager.process();
            cleanupSessions();
            
            // Update GUI periodically
            updateGUI();
            
        } catch (Exception e) {
            log("Error in main loop: " + e.getMessage());
            currentState = SystemState.ERROR_RECOVERY;
        }
        
        return 300;
    }
    
    private void updateState() {
        if (Trade.isOpen()) {
            currentState = SystemState.TRADING;
        } else if (currentState == SystemState.RUNNING && shouldAdvertise()) {
            currentState = SystemState.ADVERTISING;
        } else if (currentState != SystemState.TRADING && 
                   currentState != SystemState.GAMING && 
                   currentState != SystemState.PAYOUT) {
            currentState = SystemState.RUNNING;
        }
    }
    
    private void processRunning() {
        // Check for timeouts
        if (stateTimer.elapsed() > 60000) {
            log("State timeout, resetting...");
            currentState = SystemState.RUNNING;
            stateTimer.reset();
        }
    }
    
    private void processAdvertising() {
        if (!config.enableAdvertising) {
            currentState = SystemState.RUNNING;
            return;
        }
        
        if (System.currentTimeMillis() - lastAdTime > config.adCooldownSeconds * 1000L) {
            String ad = config.getRandomAd();
            Keyboard.type(ad, true); if (true) {
                lastAdTime = System.currentTimeMillis();
                log("Advertisement: " + ad);
            }
        }
        
        currentState = SystemState.RUNNING;
    }
    
    private void processTrading() {
        // Trade processing handled by TradeManager
        // Check if we should move to gaming state
        for (PlayerSession session : activeSessions.values()) {
            if (session.state == PlayerSession.SessionState.TRADE_ACCEPTED) {
                currentState = SystemState.GAMING;
                break;
            }
        }
    }
    
    private void processGaming() {
        // Games are played immediately after trade acceptance
        // This state is mainly for tracking
        currentState = SystemState.RUNNING;
    }
    
    private void processPayout() {
        // Find sessions needing payout
        for (PlayerSession session : activeSessions.values()) {
            if (session.win && session.state == PlayerSession.SessionState.GAME_COMPLETE) {
                executePayout(session);
            }
        }
        currentState = SystemState.RUNNING;
    }
    
    private void processErrorRecovery() {
        log("Attempting error recovery...");
        
        // Close any open trades
        if (Trade.isOpen()) {
            Trade.declineTrade();
        }
        
        // Clear widgets
        Widgets.closeAll();
        
        // Reset state
        currentState = SystemState.RUNNING;
        log("Error recovery complete");
    }
    
    private void playGame(PlayerSession session) {
        GameEngine engine = getGameEngine(session.gameType);
        if (engine == null) {
            log("Unknown game type: " + session.gameType);
            completeSession(session, false);
            return;
        }
        
        try {
            GameResult result = engine.play(session.betAmount, session.playerName);
            session.gameResult = result;
            session.win = result.win;
            session.payout = result.payout;
            session.setState(PlayerSession.SessionState.GAME_COMPLETE);
            
            // Send result messages
            Map<String, String> placeholders = new HashMap<>();
            if (result.details != null) {
                for (Map.Entry<String, Object> entry : result.details.entrySet()) {
                    placeholders.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
            
            if (result.win) {
                String winMsg = config.getWinMessage(session.gameType, result.payout, placeholders);
                sendMessage(session.playerName, winMsg);
                currentState = SystemState.PAYOUT;
            } else {
                String lossMsg = config.getLossMessage(session.gameType, placeholders);
                sendMessage(session.playerName, lossMsg);
                completeSession(session, false);
            }
            
            // Send game result
            sendMessage(session.playerName, result.message);
            
            log("Game played for " + session.playerName + ": " + 
                (result.win ? "WIN" : "LOSS") + " (" + result.payout + " GP)");
            
        } catch (Exception e) {
            log("Game error for " + session.playerName + ": " + e.getMessage());
            // Refund on error
            session.win = true;
            session.payout = session.betAmount;
            session.setState(PlayerSession.SessionState.GAME_COMPLETE);
            currentState = SystemState.PAYOUT;
        }
    }
    
    private void executePayout(PlayerSession session) {
        if (!session.win || session.payout <= 0) return;
        
        // Check if we have enough coins
        Item coins = Inventory.get(COINS_ID);
        if (coins == null || coins.getAmount() < session.payout) {
            sendMessage(session.playerName, "⚠️ Insufficient funds for payout. Please wait.");
            return;
        }
        
        // Open trade for payout
        if (Trade.tradeWithPlayer(session.playerName)) {
            Sleep.sleepUntil(() -> Trade.isOpen(), 5000);
            
            if (Trade.isOpen()) {
                // Add payout
                if (Trade.addItem(COINS_ID, session.payout)) {
                    if (Trade.isOpen(2)) {
                        Trade.acceptTrade();
                        log("Payout sent to " + session.playerName + ": " + 
                            formatCoins(session.payout) + " GP");
                        completeSession(session, true);
                    }
                }
            }
        }
    }
    
    private void completeSession(PlayerSession session, boolean payoutSuccess) {
        // Update statistics
        totalTrades++;
        
        PlayerStats stats = playerStats.computeIfAbsent(
            session.playerName, k -> new PlayerStats());
        stats.recordGame(session.win, session.betAmount, session.payout);
        
        if (session.win) {
            totalWins++;
            totalProfit -= session.payout;
        } else {
            totalLosses++;
            totalProfit += session.betAmount;
        }
        
        // Remove session
        activeSessions.remove(session.playerName);
        
        log("Session completed: " + session.playerName + 
            " - " + (session.win ? "WIN" : "LOSS") + 
            " $" + (session.win ? session.payout : session.betAmount));
    }
    
    private void cleanupSessions() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, PlayerSession>> it = activeSessions.entrySet().iterator();
        
        while (it.hasNext()) {
            Map.Entry<String, PlayerSession> entry = it.next();
            PlayerSession session = entry.getValue();
            
            if (session.isExpired() || (now - session.createdAt > 300000)) {
                sendMessage(session.playerName, "Session timed out.");
                it.remove();
                log("Cleaned up expired session for " + session.playerName);
            }
        }
    }
    
    private boolean shouldAdvertise() {
        return config.enableAdvertising && 
               (System.currentTimeMillis() - lastAdTime > config.adCooldownSeconds * 1000L);
    }
    
    private boolean isLoggedIn() {
        return Client.getGameState() == GameState.LOGGED_IN && Players.getLocal() != null;
    }
    
    private GameEngine getGameEngine(String gameType) {
        return gameEngines.get(gameType.toLowerCase());
    }
    
    private long generateSeed(String playerName) {
        try {
            String input = playerName + System.currentTimeMillis() + Math.random();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            
            long seed = 0;
            for (int i = 0; i < 8; i++) {
                seed = (seed << 8) | (hash[i] & 0xFF);
            }
            return seed;
        } catch (Exception e) {
            return System.currentTimeMillis() ^ playerName.hashCode();
        }
    }
    
    private void sendMessage(String player, String message) {
        Friends.sendMessage(player, message);
    }
    
    private String formatCoins(int amount) {
        if (amount >= 1000000) return String.format("%.1fM", amount / 1000000.0);
        if (amount >= 1000) return String.format("%.1fK", amount / 1000.0);
        return String.format("%,d", amount);
    }
    
    // ==================== CHAT LISTENER ====================
    @Override
    public void onMessage(Message message) {
        if (!systemRunning || emergencyStop) return;
        
        String sender = message.getUsername();
        String msg = message.getMessage();
        
        if (sender == null || sender.equals(Players.getLocal().getName())) {
            return;
        }
        
        chatParser.processMessage(sender, msg);
    }
    
    // ==================== GUI METHODS ====================
    private void createGUI() {
        try {
            controlFrame = new JFrame("🎰 Elite Titan Casino Pro Enhanced v2.1");
            controlFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            controlFrame.setSize(900, 700);
            controlFrame.setLayout(new BorderLayout());
            
            // Create tabbed pane
            tabbedPane = new JTabbedPane();
            
            // Create panels
            tabbedPane.addTab("🎮 Control", createControlPanel());
            tabbedPane.addTab("🎲 Games", createGamesPanel());
            tabbedPane.addTab("📊 Stats", createStatsPanel());
            tabbedPane.addTab("📝 Log", createLogPanel());
            
            controlFrame.add(tabbedPane, BorderLayout.CENTER);
            
            // Status bar
            controlFrame.add(createStatusBar(), BorderLayout.SOUTH);
            
            controlFrame.setLocationRelativeTo(null);
            controlFrame.setVisible(true);
            
            // Start update timer
            javax.swing.Timer updateTimer = new javax.swing.Timer(1000, e -> updateGUI());
            updateTimer.start();
            
        } catch (Exception e) {
            log("GUI creation error: " + e.getMessage());
        }
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Title
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("🎰 CASINO CONTROL CENTER");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, gbc);
        
        row++;
        
        // Control Buttons
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        startButton = new JButton("▶ START CASINO");
        startButton.setBackground(new Color(46, 204, 113));
        startButton.setForeground(Color.WHITE);
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        startButton.addActionListener(e -> startCasino());
        panel.add(startButton, gbc);
        
        gbc.gridx = 1;
        stopButton = new JButton("⏹ STOP");
        stopButton.setBackground(new Color(231, 76, 60));
        stopButton.setForeground(Color.WHITE);
        stopButton.setFont(new Font("Arial", Font.BOLD, 14));
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> stopCasino());
        panel.add(stopButton, gbc);
        
        row++;
        
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        emergencyButton = new JButton("🚨 EMERGENCY STOP");
        emergencyButton.setBackground(Color.RED);
        emergencyButton.setForeground(Color.WHITE);
        emergencyButton.setFont(new Font("Arial", Font.BOLD, 14));
        emergencyButton.addActionListener(e -> emergencyStop());
        panel.add(emergencyButton, gbc);
        
        row++;
        
        // Status Display
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel statusPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        statusPanel.setBorder(BorderFactory.createTitledBorder("Status"));
        
        statusLabel = new JLabel("STOPPED");
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statusPanel.add(new JLabel("State:"));
        statusPanel.add(statusLabel);
        
        profitLabel = new JLabel("0 GP");
        profitLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statusPanel.add(new JLabel("Profit:"));
        statusPanel.add(profitLabel);
        
        activeLabel = new JLabel("0");
        activeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statusPanel.add(new JLabel("Active:"));
        statusPanel.add(activeLabel);
        
        panel.add(statusPanel, gbc);
        
        row++;
        
        // Betting Limits
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel betPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        betPanel.setBorder(BorderFactory.createTitledBorder("Betting Limits"));
        
        betPanel.add(new JLabel("Min Bet:"));
        minBetSpinner = new JSpinner(new SpinnerNumberModel(config.minBet, 1, 1000000000, 1000));
        betPanel.add(minBetSpinner);
        
        betPanel.add(new JLabel("Max Bet:"));
        maxBetSpinner = new JSpinner(new SpinnerNumberModel(config.maxBet, 1, 1000000000, 10000));
        betPanel.add(maxBetSpinner);
        
        panel.add(betPanel, gbc);
        
        row++;
        
        // Settings
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel settingsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        settingsPanel.setBorder(BorderFactory.createTitledBorder("Settings"));
        
        autoAcceptBox = new JCheckBox("Auto Accept Trades", config.autoAcceptTrades);
        settingsPanel.add(autoAcceptBox);
        
        enableAdsBox = new JCheckBox("Enable Ads", config.enableAdvertising);
        settingsPanel.add(enableAdsBox);
        
        settingsPanel.add(new JLabel("Max Sessions:"));
        JSpinner maxSessionsSpinner = new JSpinner(new SpinnerNumberModel(config.maxActiveSessions, 1, 10, 1));
        settingsPanel.add(maxSessionsSpinner);
        
        panel.add(settingsPanel, gbc);
        
        return panel;
    }
    
    private JPanel createGamesPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Craps
        JPanel crapsPanel = new JPanel(new BorderLayout());
        crapsPanel.setBorder(BorderFactory.createTitledBorder("🎲 Craps"));
        crapsToggle = new JCheckBox("Enable Craps", config.crapsEnabled);
        crapsPanel.add(crapsToggle, BorderLayout.NORTH);
        
        JTextArea crapsRules = new JTextArea(gameEngines.get("craps").getRules(), 3, 40);
        crapsRules.setEditable(false);
        crapsRules.setLineWrap(true);
        crapsPanel.add(new JScrollPane(crapsRules), BorderLayout.CENTER);
        panel.add(crapsPanel);
        
        // Dice Duel
        JPanel dicePanel = new JPanel(new BorderLayout());
        dicePanel.setBorder(BorderFactory.createTitledBorder("⚄ Dice Duel"));
        diceToggle = new JCheckBox("Enable Dice Duel", config.diceEnabled);
        dicePanel.add(diceToggle, BorderLayout.NORTH);
        
        JTextArea diceRules = new JTextArea(gameEngines.get("dice").getRules(), 3, 40);
        diceRules.setEditable(false);
        diceRules.setLineWrap(true);
        dicePanel.add(new JScrollPane(diceRules), BorderLayout.CENTER);
        panel.add(dicePanel);
        
        // Flower Poker
        JPanel flowerPanel = new JPanel(new BorderLayout());
        flowerPanel.setBorder(BorderFactory.createTitledBorder("🌸 Flower Poker"));
        flowerToggle = new JCheckBox("Enable Flower Poker", config.flowerEnabled);
        flowerPanel.add(flowerToggle, BorderLayout.NORTH);
        
        JTextArea flowerRules = new JTextArea(gameEngines.get("flower").getRules(), 4, 40);
        flowerRules.setEditable(false);
        flowerRules.setLineWrap(true);
        flowerPanel.add(new JScrollPane(flowerRules), BorderLayout.CENTER);
        panel.add(flowerPanel);
        
        // Apply Button
        JButton applyButton = new JButton("✅ APPLY GAME SETTINGS");
        applyButton.addActionListener(e -> applyGameSettings());
        panel.add(applyButton);
        
        return panel;
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Overall Stats
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel statsPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Overall Statistics"));
        
        statsPanel.add(new JLabel("Total Profit:"));
        JLabel totalProfitLabel = new JLabel("0 GP");
        statsPanel.add(totalProfitLabel);
        
        statsPanel.add(new JLabel("Total Wagered:"));
        JLabel wageredLabel = new JLabel("0 GP");
        statsPanel.add(wageredLabel);
        
        statsPanel.add(new JLabel("Wins/Losses:"));
        JLabel winLossLabel = new JLabel("0/0");
        statsPanel.add(winLossLabel);
        
        statsPanel.add(new JLabel("Total Trades:"));
        JLabel tradesLabel = new JLabel("0");
        statsPanel.add(tradesLabel);
        
        panel.add(statsPanel, gbc);
        
        row++;
        
        // Session Table
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        
        sessionsModel = new DefaultTableModel(new Object[]{"Player", "Game", "Bet", "Status", "Time"}, 0);
        sessionsTable = new JTable(sessionsModel);
        JScrollPane tableScroll = new JScrollPane(sessionsTable);
        tableScroll.setPreferredSize(new Dimension(400, 200));
        panel.add(tableScroll, gbc);
        
        return panel;
    }
    
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        logArea = new JTextArea(15, 60);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("System Log"));
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton clearButton = new JButton("Clear Log");
        clearButton.addActionListener(e -> logArea.setText(""));
        buttonPanel.add(clearButton);
        
        JButton exportButton = new JButton("Export Log");
        exportButton.addActionListener(e -> exportLog());
        buttonPanel.add(exportButton);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        
        JLabel versionLabel = new JLabel(" Elite Titan Casino Pro Enhanced v2.1 | DreamBot 4.0+ ");
        statusBar.add(versionLabel, BorderLayout.WEST);
        
        JLabel authorLabel = new JLabel("Created by ikingsnipe ");
        statusBar.add(authorLabel, BorderLayout.EAST);
        
        return statusBar;
    }
    
    private void updateGUI() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Update status
                statusLabel.setText(currentState.toString());
                statusLabel.setForeground(
                    currentState == SystemState.RUNNING ? new Color(46, 204, 113) :
                    currentState == SystemState.ERROR_RECOVERY ? Color.ORANGE :
                    Color.RED
                );
                
                // Update profit
                profitLabel.setText(formatCoins(totalProfit) + " GP");
                profitLabel.setForeground(totalProfit >= 0 ? new Color(46, 204, 113) : Color.RED);
                
                // Update active sessions
                activeLabel.setText(String.valueOf(activeSessions.size()));
                
                // Update sessions table
                sessionsModel.setRowCount(0);
                for (PlayerSession session : activeSessions.values()) {
                    long minutes = (System.currentTimeMillis() - session.createdAt) / 60000;
                    sessionsModel.addRow(new Object[]{
                        session.playerName,
                        session.gameType,
                        formatCoins(session.betAmount) + " GP",
                        session.state.toString(),
                        minutes + " min"
                    });
                }
                
                // Update button states
                startButton.setEnabled(!systemRunning);
                stopButton.setEnabled(systemRunning);
                
            } catch (Exception e) {
                // Silent fail for GUI updates
            }
        });
    }
    
    private void startCasino() {
        log("Starting Elite Titan Casino Enhanced...");
        
        // Update config from UI
        updateConfigFromUI();
        
        // Validate
        if (!validateConfig()) {
            log("Configuration invalid. Please check settings.");
            return;
        }
        
        systemRunning = true;
        emergencyStop = false;
        currentState = SystemState.RUNNING;
        stateTimer.reset();
        
        log("Casino started successfully!");
        log("Min Bet: " + config.minBet + " | Max Bet: " + config.maxBet);
        log("Enabled Games: " + getEnabledGames());
    }
    
    private void stopCasino() {
        log("Stopping casino...");
        
        systemRunning = false;
        currentState = SystemState.STOPPED;
        
        // Close trades
        if (Trade.isOpen()) {
            Trade.declineTrade();
        }
        
        // Clear sessions
        activeSessions.clear();
        
        log("Casino stopped");
    }
    
    private void emergencyStop() {
        int confirm = JOptionPane.showConfirmDialog(controlFrame,
            "EMERGENCY STOP - This will cancel all trades and clear sessions.\nAre you sure?",
            "Emergency Stop Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            log("EMERGENCY STOP ACTIVATED!");
            
            emergencyStop = true;
            systemRunning = false;
            currentState = SystemState.STOPPED;
            
            // Force close everything
            if (Trade.isOpen()) {
                Trade.declineTrade();
            }
            Widgets.closeAll();
            activeSessions.clear();
            
            // Notify players
            for (String player : new ArrayList<>(activeSessions.keySet())) {
                sendMessage(player, "⚠️ Casino emergency stop. All bets refunded.");
            }
            
            log("All systems halted");
        }
    }
    
    private void applyGameSettings() {
        updateConfigFromUI();
        log("Game settings updated");
    }
    
    private void exportLog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("casino_log_" + System.currentTimeMillis() + ".txt"));
        
        if (fileChooser.showSaveDialog(controlFrame) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter writer = new PrintWriter(fileChooser.getSelectedFile())) {
                writer.write(logArea.getText());
                log("Log exported to: " + fileChooser.getSelectedFile().getName());
            } catch (Exception e) {
                log("Export error: " + e.getMessage());
            }
        }
    }
    
    private void updateConfigFromUI() {
        // General
        config.minBet = (int) minBetSpinner.getValue();
        config.maxBet = (int) maxBetSpinner.getValue();
        config.autoAcceptTrades = autoAcceptBox.isSelected();
        config.enableAdvertising = enableAdsBox.isSelected();
        
        // Games
        config.crapsEnabled = crapsToggle.isSelected();
        config.diceEnabled = diceToggle.isSelected();
        config.flowerEnabled = flowerToggle.isSelected();
        
        // Update game engines
        gameEngines.get("craps").setEnabled(config.crapsEnabled);
        gameEngines.get("dice").setEnabled(config.diceEnabled);
        gameEngines.get("flower").setEnabled(config.flowerEnabled);
    }
    
    private boolean validateConfig() {
        if (config.minBet <= 0 || config.maxBet <= 0 || config.maxBet < config.minBet) {
            log("Invalid bet limits");
            return false;
        }
        return true;
    }
    
    private String getEnabledGames() {
        java.util.List<String> enabled = new ArrayList<>();
        if (config.crapsEnabled) enabled.add("Craps");
        if (config.diceEnabled) enabled.add("Dice");
        if (config.flowerEnabled) enabled.add("Flower Poker");
        return String.join(", ", enabled);
    }
    
    // ==================== LOGGING ====================
    private void log(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String logMessage = "[" + timestamp + "] " + message;
        
        Logger.log(logMessage);
        
        if (logArea != null) {
            SwingUtilities.invokeLater(() -> {
                logArea.append(logMessage + "\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
            });
        }
    }
    
    // ==================== PAINT ====================
    @Override
    public void onPaint(Graphics g) {
        if (!systemRunning && !emergencyStop) {
            // Show stopped state
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(5, 5, 250, 100);
            g.setColor(Color.WHITE);
            g.drawString("Elite Titan Casino Pro Enhanced", 15, 25);
            g.drawString("Status: STOPPED", 15, 45);
            g.drawString("Open GUI to start", 15, 65);
            return;
        }
        
        // Running state overlay
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(5, 5, 280, 150);
        
        // Header
        g.setColor(new Color(255, 215, 0));
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("🎰 Elite Titan Casino Pro Enhanced", 15, 25);
        
        // Status
        g.setColor(currentState == SystemState.ERROR_RECOVERY ? Color.RED : Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Status: " + currentState, 15, 45);
        
        // Statistics
        g.setColor(Color.CYAN);
        g.drawString("Profit: " + formatCoins(totalProfit), 15, 65);
        g.drawString("Active: " + activeSessions.size(), 15, 85);
        g.drawString("W/L: " + totalWins + "/" + totalLosses, 15, 105);
        
        // Current session info
        if (!activeSessions.isEmpty()) {
            g.setColor(Color.LIGHT_GRAY);
            g.drawString("Current: " + activeSessions.size() + " players", 15, 125);
        }
        
        // Emergency stop indicator
        if (emergencyStop) {
            g.setColor(Color.RED);
            g.fillRect(265, 5, 20, 20);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("!", 272, 20);
        }
    }
    
    // ==================== SHUTDOWN ====================
    @Override
    public void onExit() {
        log("Shutting down Elite Titan Casino Enhanced...");
        
        // Stop system
        systemRunning = false;
        emergencyStop = true;
        
        // Close trades
        if (Trade.isOpen()) {
            Trade.declineTrade();
        }
        
        // Close GUI
        if (controlFrame != null) {
            controlFrame.dispose();
        }
        
        // Remove chat listener
        
        
        log("System shutdown complete");
        log("================================================");
    }
}
