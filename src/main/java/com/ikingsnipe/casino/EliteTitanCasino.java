package com.ikingsnipe.casino;

import com.ikingsnipe.casino.core.CasinoState;
import com.ikingsnipe.casino.games.GameResult;
import com.ikingsnipe.casino.games.impl.CrapsGame;
import com.ikingsnipe.casino.gui.CasinoGUI;
import com.ikingsnipe.casino.managers.*;
import com.ikingsnipe.casino.models.*;
import com.ikingsnipe.casino.utils.*;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.ChatListener;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.message.Message;
import org.dreambot.api.utilities.Sleep;
import javax.swing.*;
import java.awt.*;

@ScriptManifest(
    name = "snipes♧scripts Enterprise",
    description = "Ultimate Enterprise-grade OSRS Casino Host Bot",
    author = "ikingsnipe",
    version = 16.0,
    category = Category.MISC
)
public class EliteTitanCasino extends AbstractScript implements ChatListener {
    private CasinoConfig config;
    private CasinoState state = CasinoState.INITIALIZING;
    private CasinoGUI gui;
    private GameManager gameManager;
    private SessionManager sessionManager;
    private BankingManager bankingManager;
    private LocationManager locationManager;
    private ProvablyFair provablyFair;
    private DiscordWebhook webhook;
    private ProfitTracker profitTracker;
    private ChatAI chatAI;

    private String currentPlayer;
    private long currentBet;
    private String selectedGame = "dice";
    private PlayerSession currentSession;
    private long lastAdTime;
    private long tradeStartTime;
    private boolean welcomeSent;
    private volatile boolean guiFinished, startScript;

    @Override
    public void onStart() {
        config = new CasinoConfig();
        SwingUtilities.invokeLater(() -> {
            gui = new CasinoGUI(config, b -> {
                guiFinished = true;
                startScript = b;
                if (b) initSystems();
            });
            gui.setVisible(true);
        });
    }

    private void initSystems() {
        gameManager = new GameManager(config);
        sessionManager = new SessionManager();
        bankingManager = new BankingManager(config);
        locationManager = new LocationManager(config);
        provablyFair = new ProvablyFair();
        profitTracker = new ProfitTracker();
        chatAI = new ChatAI();
        
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
            // Auto-Muling Check
            if (config.autoMule && getInventoryValue() >= config.muleThreshold) {
                log("Mule threshold reached! Please handle muling manually or implement auto-trade logic.");
                // For safety, we just log it for now to prevent accidental losses
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
        } else if (config.autoBank && getInventoryValue() < config.restockThreshold) {
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
            trader.interact("Trade with");
            Sleep.sleepUntil(Trade::isOpen, 3000);
        }

        // Humanization: Occasional random movement
        if (Calculations.random(1, 100) == 1) {
            locationManager.walkToLocation();
        }

        if (System.currentTimeMillis() - lastAdTime > config.adIntervalSeconds * 1000L) {
            Keyboard.type(config.adMessage, true);
            lastAdTime = System.currentTimeMillis();
        }
        return 1000;
    }

    private int handleTrade1() {
        if (!Trade.isOpen(1)) { if (!Trade.isOpen()) reset(); return 500; }
        if (System.currentTimeMillis() - tradeStartTime > 30000) {
            Trade.declineTrade(); reset(); return 1000;
        }

        if (!welcomeSent) {
            String greeting = String.format("Hello %s, you are now in a secure trade window. It is safe to proceed.", currentPlayer);
            Keyboard.type(greeting, true);
            Sleep.sleep(1000, 1500);
            Keyboard.type(String.format(config.tradeWelcome, provablyFair.getHash().substring(0, 12)), true);
            welcomeSent = true;
        }
        
        long tradeVal = getTradeValue();
        if (currentBet > 0 && tradeVal == 0 && currentSession.getBalance() >= currentBet) {
            Trade.acceptTrade();
            Sleep.sleepUntil(() -> Trade.isOpen(2), 5000);
        } else if (tradeVal > 0) {
            if (tradeVal < config.minBet || tradeVal > config.maxBet) {
                Keyboard.type("Bet must be between " + formatGP(config.minBet) + " and " + formatGP(config.maxBet), true);
                Trade.declineTrade(); reset(); return 1000;
            }
            currentBet = tradeVal;
            if (Trade.acceptTrade()) Sleep.sleepUntil(() -> Trade.isOpen(2), 5000);
        }
        return 1000;
    }

    private int handleTrade2() {
        if (!Trade.isOpen(2)) { if (!Trade.isOpen()) state = CasinoState.PROCESSING_GAME; return 500; }
        if (getTradeValue() > 0 && getTradeValue() != currentBet) {
            Trade.declineTrade(); reset(); return 1000;
        }
        if (Trade.acceptTrade()) Sleep.sleepUntil(() -> !Trade.isOpen(), 5000);
        return 1000;
    }

    private int handleGame() {
        if (getTradeValue() == 0 && currentSession.getBalance() >= currentBet) {
            currentSession.subtractBalance(currentBet);
        }

        // Jackpot Logic
        if (config.jackpotEnabled) {
            long contribution = (long)(currentBet * (config.jackpotContributionPercent / 100.0));
            config.currentJackpot += contribution;
        }

        GameResult r = gameManager.play(selectedGame, currentBet);
        String seed = provablyFair.getSeed().substring(0, 8);
        
        String logMsg;
        if (r.isWin()) {
            profitTracker.addWin(r.getPayout(), currentPlayer);
            logMsg = String.format(config.winMessage, currentPlayer, formatGP(r.getPayout()), r.getDescription(), seed);
            Keyboard.type(logMsg, true);
            currentSession.addBalance(r.getPayout());
            currentSession.setOwedAmount(currentSession.getBalance());
            state = CasinoState.PAYOUT_PENDING;
        } else {
            profitTracker.addLoss(currentBet);
            logMsg = String.format(config.lossMessage, currentPlayer, r.getDescription());
            Keyboard.type(logMsg, true);
            reset();
        }
        
        if (webhook != null) webhook.send("🎮 **Game Result**: " + logMsg);
        return 1000;
    }

    private int handlePayout() {
        if (currentSession.getOwedAmount() <= 0) { reset(); return 500; }
        Player p = Players.closest(currentPlayer);
        if (p == null) return 5000;
        
        if (Trade.isOpen()) {
            if (Trade.isOpen(1)) {
                addPayout(currentSession.getOwedAmount());
                Trade.acceptTrade();
            } else if (Trade.isOpen(2)) {
                if (Trade.acceptTrade()) { 
                    currentSession.setOwedAmount(0); 
                    currentSession.setBalance(0);
                    reset(); 
                }
            }
        } else {
            p.interact("Trade with");
            Sleep.sleepUntil(Trade::isOpen, 5000);
        }
        return 1000;
    }

    private int handleRecovery() { Widgets.closeAll(); if (Trade.isOpen()) Trade.declineTrade(); reset(); return 1000; }

    private void reset() {
        state = CasinoState.IDLE; currentPlayer = null; currentBet = 0; welcomeSent = false;
        selectedGame = config.defaultGame;
    }

    @Override
    public void onMessage(Message msg) {
        String txt = msg.getMessage().toLowerCase();
        String sender = msg.getUsername();
        
        if (config.chatAIEnabled && !sender.equals(Players.getLocal().getName())) {
            chatAI.handleChat(txt);
        }

        if (state == CasinoState.TRADING_WINDOW_1 && sender.equals(currentPlayer)) {
            if (txt.startsWith("!c")) { selectedGame = "craps"; parseBet(txt); }
            else if (txt.startsWith("!dw")) { selectedGame = "dicewar"; parseBet(txt); }
            else if (txt.startsWith("!b2b")) {
                selectedGame = "craps";
                CrapsGame cg = (CrapsGame) gameManager.getGame("craps");
                cg.setB2B(true);
                if (txt.contains("7")) cg.setPredictedNumber(7);
                else if (txt.contains("9")) cg.setPredictedNumber(9);
                else if (txt.contains("12")) cg.setPredictedNumber(12);
                parseBet(txt);
            }
        }
    }

    private void parseBet(String txt) {
        String[] parts = txt.split(" ");
        if (parts.length > 1) {
            try {
                long val = Long.parseLong(parts[1].replaceAll("[^0-9]", ""));
                if (val > 0) currentBet = val;
            } catch (Exception ignored) {}
        }
    }

    private long getTradeValue() {
        long v = 0;
        Item[] items = Trade.getTheirItems();
        if (items != null) {
            for (Item i : items) {
                if (i == null) continue;
                if (i.getID() == CasinoConfig.COINS_ID) v += i.getAmount();
                else if (i.getID() == CasinoConfig.PLATINUM_TOKEN_ID) v += i.getAmount() * 1000L;
            }
        }
        return v;
    }

    private void addPayout(long amount) {
        long rem = amount;
        if (rem >= 1000) {
            int tokens = (int)(rem / 1000);
            Trade.addItem(CasinoConfig.PLATINUM_TOKEN_ID, tokens);
            rem %= 1000;
        }
        if (rem > 0) Trade.addItem(CasinoConfig.COINS_ID, (int)rem);
    }

    private long getInventoryValue() {
        return Inventory.count(CasinoConfig.COINS_ID) + Inventory.count(CasinoConfig.PLATINUM_TOKEN_ID) * 1000L;
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
        
        // Main Panel
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(5, 5, 240, 160, 15, 15);
        g2.setColor(new Color(0, 255, 127));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(5, 5, 240, 160, 15, 15);
        
        g2.setFont(new Font("Verdana", Font.BOLD, 16));
        g2.drawString("snipes♧scripts Enterprise", 15, 30);
        
        g2.setFont(new Font("Verdana", Font.PLAIN, 12));
        g2.setColor(Color.WHITE);
        g2.drawString("Runtime: " + profitTracker.getRuntime(), 15, 55);
        g2.drawString("Net Profit: " + formatGP(profitTracker.getNetProfit()), 15, 75);
        g2.drawString("Jackpot: " + formatGP(config.currentJackpot), 15, 95);
        g2.drawString("Status: " + state.getStatus(), 15, 115);
        
        // Recent Winners
        g2.setColor(new Color(0, 255, 127, 100));
        g2.drawString("Recent Winners:", 15, 135);
        int y = 155;
        for (String winner : profitTracker.getRecentWinners()) {
            g2.drawString("• " + winner, 20, y);
            y += 15;
        }
    }
}
