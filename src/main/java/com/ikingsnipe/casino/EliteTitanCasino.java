package com.ikingsnipe.casino;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;
import com.ikingsnipe.casino.games.impl.CrapsGame;
import com.ikingsnipe.casino.gui.CasinoGUI;
import com.ikingsnipe.casino.managers.*;
import com.ikingsnipe.casino.models.CasinoConfig;
import com.ikingsnipe.casino.models.CasinoState;
import com.ikingsnipe.casino.models.PlayerSession;
import com.ikingsnipe.casino.utils.ChatAI;
import com.ikingsnipe.casino.utils.DiscordWebhook;
import com.ikingsnipe.casino.utils.ProvablyFair;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.widgets.message.Message;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.script.listener.ChatListener;

import javax.swing.*;
import java.awt.*;
import java.util.List;

@ScriptManifest(name = "snipes♧scripts Enterprise", author = "ikingsnipe", version = 3.0, category = Category.MONEYMAKING, description = "Enterprise Casino System")
public class EliteTitanCasino extends AbstractScript implements ChatListener {

    private CasinoConfig config;
    private CasinoState state = CasinoState.INITIALIZING;
    private GameManager gameManager;
    private SessionManager sessionManager;
    private BankingManager bankingManager;
    private LocationManager locationManager;
    private ProvablyFair provablyFair;
    private DiscordWebhook webhook;
    private ProfitTracker profitTracker;
    private ChatAI chatAI;
    private MuleManager muleManager;
    private HumanizationManager humanizationManager;

    private String currentPlayer;
    private long currentBet;
    private String selectedGame = "dice";
    private PlayerSession currentSession;
    private long lastAdTime;
    private long tradeStartTime;
    private boolean welcomeSent;
    private volatile boolean guiFinished, startScript;
    
    private CasinoState lastState;
    private long lastStateChangeTime;
    private int adIndex = 0;

    @Override
    public void onStart() {
        config = new CasinoConfig();
        SwingUtilities.invokeLater(() -> {
            new CasinoGUI(config, (start) -> {
                this.startScript = start;
                this.guiFinished = true;
            });
        });

        sessionManager = new SessionManager();
        gameManager = new GameManager(config);
        bankingManager = new BankingManager(config);
        locationManager = new LocationManager(config);
        provablyFair = new ProvablyFair();
        profitTracker = new ProfitTracker();
        chatAI = new ChatAI();
        chatAI.setProfitTracker(profitTracker);
        muleManager = new MuleManager(config);
        humanizationManager = new HumanizationManager(config);
        
        if (config.discordEnabled && !config.discordWebhookUrl.isEmpty()) {
            webhook = new DiscordWebhook(config.discordWebhookUrl);
            webhook.send("🚀 **snipes♧scripts Enterprise** is now ONLINE!");
        }
        state = CasinoState.INITIALIZING;
    }

    @Override
    public int onLoop() {
        if (!guiFinished) return 500;
        if (!startScript) { stop(); return 0; }

        try {
            if (humanizationManager.shouldTakeBreak()) {
                humanizationManager.takeBreak();
                return 1000;
            }

            if (muleManager.shouldMule() || muleManager.isMulingInProgress()) {
                muleManager.handleMuling();
                return 1000;
            }

            if (state == lastState) {
                if (System.currentTimeMillis() - lastStateChangeTime > 60000) {
                    log("Stuck detection triggered in state: " + state + ". Recovering...");
                    state = CasinoState.ERROR_RECOVERY;
                }
            } else {
                lastState = state;
                lastStateChangeTime = System.currentTimeMillis();
            }

            switch (state) {
                case INITIALIZING: return handleInitializing();
                case WALKING_TO_LOCATION: return handleWalking();
                case BANKING: return handleBanking();
                case IDLE: return handleIdle();
                case TRADING_WINDOW_1: return handleTrade1();
                case TRADING_WINDOW_2: return handleTrade2();
                case PROCESSING_GAME: return handleGame();
                case PAYOUT_PENDING: return handlePayout();
                case ERROR_RECOVERY: return handleRecovery();
            }
        } catch (Exception e) {
            log("Critical Error: " + e.getMessage());
            state = CasinoState.ERROR_RECOVERY;
        }
        return Calculations.random(200, 400);
    }

    private int handleInitializing() {
        if (config.walkOnStart && !locationManager.isAtLocation()) {
            state = CasinoState.WALKING_TO_LOCATION;
        } else if (config.autoBank && Inventory.count(CasinoConfig.COINS_ID) < config.restockThreshold) {
            state = CasinoState.BANKING;
        } else {
            state = CasinoState.IDLE;
        }
        return 500;
    }

    private int handleWalking() {
        if (locationManager.isAtLocation()) { state = CasinoState.INITIALIZING; return 500; }
        locationManager.walkToLocation();
        return 1000;
    }

    private int handleBanking() {
        if (bankingManager.restock()) {
            state = CasinoState.INITIALIZING;
        }
        return 1000;
    }

    private int handleIdle() {
        if (Trade.isOpen()) {
            currentPlayer = Trade.getTradingWith();
            if (currentPlayer != null) {
                currentSession = sessionManager.getSession(currentPlayer);
                provablyFair.generateNewSeed();
                state = CasinoState.TRADING_WINDOW_1;
                tradeStartTime = System.currentTimeMillis();
                welcomeSent = false;
            }
            return 500;
        }
        
        Player trader = Players.closest(p -> p != null && p.isInteracting(Players.getLocal()));
        if (trader != null && !Trade.isOpen()) {
            log("Accepting trade request from: " + trader.getName());
            trader.interact("Trade with");
            if (Sleep.sleepUntil(Trade::isOpen, 5000)) {
                Sleep.sleep(600, 1000);
            }
        }

        humanizationManager.applyIdleHumanization();
        
        if (Calculations.random(1, 100) == 1) {
            locationManager.walkToLocation();
        }

        if (System.currentTimeMillis() - lastAdTime > config.adIntervalSeconds * 1000L) {
            sendRotatingAd();
            lastAdTime = System.currentTimeMillis();
        }
        return 1000;
    }

    private void sendRotatingAd() {
        if (config.adMessages.isEmpty()) return;
        String msg = config.adMessages.get(adIndex);
        if (config.enableAntiMute) {
            msg += " " + generateRandomString(3);
        }
        Keyboard.type(msg, true);
        adIndex = (adIndex + 1) % config.adMessages.size();
    }

    private String generateRandomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(Calculations.random(0, chars.length() - 1)));
        }
        return sb.toString();
    }

    private int handleTrade1() {
        if (!Trade.isOpen(1)) { if (!Trade.isOpen()) reset(); return 500; }
        if (System.currentTimeMillis() - tradeStartTime > 30000) {
            Trade.declineTrade(); reset(); return 1000;
        }

        if (!welcomeSent) {
            String greeting = "Hello " + currentPlayer + ", safe trade window active.";
            Keyboard.type(greeting, true);
            Sleep.sleep(600, 1000);
            Keyboard.type(String.format(config.tradeWelcome, provablyFair.getHash().substring(0, 12)), true);
            welcomeSent = true;
        }
        
        long tradeVal = getTradeValue();
        if (currentBet > 0 && tradeVal == 0 && currentSession.getBalance() >= currentBet) {
            confirmToClanChat();
            Trade.acceptTrade();
            Sleep.sleepUntil(() -> Trade.isOpen(2), 5000);
        } else if (tradeVal > 0) {
            if (tradeVal < config.minBet || tradeVal > config.maxBet) {
                Keyboard.type("Bet must be between " + formatGP(config.minBet) + " and " + formatGP(config.maxBet), true);
                Trade.declineTrade(); reset(); return 1000;
            }
            currentBet = tradeVal;
            confirmToClanChat();
            if (Trade.acceptTrade()) Sleep.sleepUntil(() -> Trade.isOpen(2), 5000);
        }
        return 1000;
    }

    private int handleTrade2() {
        if (!Trade.isOpen(2)) { if (!Trade.isOpen()) reset(); return 500; }
        if (Trade.acceptTrade()) {
            state = CasinoState.PROCESSING_GAME;
        }
        return 1000;
    }

    private int handleGame() {
        AbstractGame game = gameManager.getGame(selectedGame);
        double mult = config.games.get(selectedGame).multiplier;
        GameResult result = game.play(currentBet, mult);
        
        String logMsg = result.isWin() ? 
            String.format(config.winMessage, currentPlayer, formatGP(result.getPayout()), result.getDescription(), provablyFair.getSeed()) :
            String.format(config.lossMessage, currentPlayer, result.getDescription());
        
        Keyboard.type(logMsg, true);
        profitTracker.addGame(currentPlayer, result.isWin(), result.isWin() ? result.getPayout() - currentBet : -currentBet);
        
        if (result.isWin()) {
            currentSession.addBalance(result.getPayout());
            state = CasinoState.PAYOUT_PENDING;
        } else {
            reset();
        }
        return 1000;
    }

    private int handlePayout() {
        Player p = Players.closest(currentPlayer);
        if (p != null && !Trade.isOpen()) {
            p.interact("Trade with");
            if (Sleep.sleepUntil(Trade::isOpen, 5000)) {
                long toPay = currentSession.getBalance();
                if (Trade.isOpen(1)) {
                    addPayoutItems(toPay);
                    Trade.acceptTrade();
                } else if (Trade.isOpen(2)) {
                    if (Trade.acceptTrade()) {
                        currentSession.setBalance(0);
                        reset();
                    }
                }
            }
        }
        return 1000;
    }

    private void addPayoutItems(long amount) {
        long rem = amount;
        if (rem >= 1000) {
            int tokens = (int)(rem / 1000);
            Trade.addItem(CasinoConfig.PLATINUM_TOKEN_ID, tokens);
            rem %= 1000;
        }
        if (rem > 0) Trade.addItem(CasinoConfig.COINS_ID, (int)rem);
    }

    private int handleRecovery() { 
        Widgets.closeAll(); 
        if (Trade.isOpen()) Trade.declineTrade(); 
        reset(); 
        return 1000; 
    }

    private void confirmToClanChat() {
        String msg = String.format("/Confirming %s's bet of %s. Safe to accept!", currentPlayer, formatGP(currentBet));
        Keyboard.type(msg, true);
    }

    @Override
    public void onMessage(Message msg) {
        String txt = msg.getMessage().toLowerCase();
        String sender = msg.getUsername();
        
        if (config.chatAIEnabled && !sender.equals(Players.getLocal().getName())) {
            chatAI.handleChat(txt);
        }

        if (state == CasinoState.TRADING_WINDOW_1 && sender.equals(currentPlayer)) {
            if (txt.startsWith("!c") || txt.startsWith("!craps")) { 
                selectedGame = "craps"; 
                parseBet(txt); 
                Keyboard.type("Game set to Chasing Craps!", true);
            }
            else if (txt.startsWith("!dw") || txt.startsWith("!dicewar")) { 
                selectedGame = "dicewar"; 
                parseBet(txt); 
                Keyboard.type("Game set to Dice War!", true);
            }
            else if (txt.startsWith("!b2b")) {
                selectedGame = "craps";
                CrapsGame cg = (CrapsGame) gameManager.getGame("craps");
                cg.setConfig(config);
                cg.setB2B(true);
                if (txt.contains("7")) cg.setPredictedNumber(7);
                else if (txt.contains("9")) cg.setPredictedNumber(9);
                else if (txt.contains("12")) cg.setPredictedNumber(12);
                parseBet(txt);
                Keyboard.type("B2B Chasing Craps activated!", true);
            }
            else if (txt.startsWith("!fp") || txt.startsWith("!flower")) {
                selectedGame = "flower";
                parseBet(txt);
                Keyboard.type("Game set to Flower Poker!", true);
            }
        }
    }

    private void parseBet(String txt) {
        String[] parts = txt.split(" ");
        if (parts.length > 1) {
            try {
                String val = parts[1].toLowerCase();
                if (val.endsWith("k")) currentBet = Long.parseLong(val.replace("k", "")) * 1000L;
                else if (val.endsWith("m")) currentBet = Long.parseLong(val.replace("m", "")) * 1000000L;
                else currentBet = Long.parseLong(val);
            } catch (Exception ignored) {}
        }
    }

    private void reset() {
        currentPlayer = null;
        currentBet = 0;
        state = CasinoState.IDLE;
        welcomeSent = false;
    }

    private long getTradeValue() {
        long total = 0;
        for (Item item : Trade.getTheirItems()) {
            if (item != null) {
                if (item.getID() == CasinoConfig.PLATINUM_TOKEN_ID) {
                    total += item.getAmount() * 1000L;
                } else if (item.getID() == CasinoConfig.COINS_ID) {
                    total += item.getAmount();
                }
            }
        }
        return total;
    }

    private String formatGP(long a) {
        if (a >= 1_000_000) return (a / 1_000_000) + "M";
        if (a >= 1_000) return (a / 1_000) + "K";
        return String.valueOf(a);
    }

    @Override
    public void onPaint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(10, 10, 220, 180, 15, 15);
        g2.setColor(new Color(0, 255, 127));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(10, 10, 220, 180, 15, 15);
        
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString("snipes♧scripts Enterprise", 15, 35);
        
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.setColor(Color.WHITE);
        g2.drawString("Runtime: " + profitTracker.getRuntime(), 15, 55);
        g2.drawString("Net Profit: " + formatGP(profitTracker.getNetProfit()), 15, 75);
        g2.drawString("Jackpot: " + formatGP(config.currentJackpot), 15, 95);
        
        String status = state.getStatus();
        if (state == CasinoState.BANKING) {
            status = bankingManager.getStatus();
        }
        g2.drawString("Status: " + status, 15, 115);
        
        g2.setColor(new Color(0, 255, 127, 100));
        g2.drawString("Recent Winners:", 15, 135);
        g2.setFont(new Font("Arial", Font.ITALIC, 11));
        List<String> winners = profitTracker.getRecentWinners();
        for (int i = 0; i < Math.min(winners.size(), 3); i++) {
            g2.drawString("- " + winners.get(i), 20, 155 + (i * 15));
        }
    }
}
