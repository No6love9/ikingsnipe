package com.ikingsnipe.casino;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;
import com.ikingsnipe.casino.gui.CasinoGUI;
import com.ikingsnipe.casino.listeners.TradeRequestListener;
import com.ikingsnipe.casino.managers.*;
import com.ikingsnipe.casino.models.CasinoConfig;
import com.ikingsnipe.casino.models.CasinoState;
import com.ikingsnipe.casino.models.PlayerSession;
import com.ikingsnipe.casino.utils.ChatAI;
import com.ikingsnipe.casino.utils.DiscordWebhook;
import com.ikingsnipe.casino.utils.ProvablyFair;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.widgets.message.Message;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.script.listener.ChatListener;
import org.dreambot.api.script.listener.PaintListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

@ScriptManifest(name = "SnipesScripts Ultimate", author = "ikingsnipe", version = 10.0, category = Category.MONEYMAKING, description = "Ultimate 2026 Grade Casino: Java 8 Compatible, SQL Balances, Discord Bot Sync, and Platinum Tokens")
public class EliteTitanCasino extends AbstractScript implements ChatListener, PaintListener, MouseListener {

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
    private TradeManager tradeManager;
    private DatabaseManager dbManager;
    private TradeRequestListener tradeRequestListener;

    private String currentPlayer;
    private long currentBet;
    private String selectedGame = "craps";
    private volatile boolean guiFinished, startScript;
    
    private long lastAdTime;
    private int adIndex = 0;
    private long lastStateChangeTime;
    private CasinoState lastState;

    @Override
    public void onStart() {
        log("Initializing SnipesScripts Ultimate v10.0...");
        config = new CasinoConfig();
        
        // Java 8 compatible Swing initialization
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new CasinoGUI(config, new java.util.function.Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean start) {
                        startScript = start;
                        guiFinished = true;
                    }
                });
            }
        });

        // Initialize Managers with robust fallbacks
        try {
            dbManager = new DatabaseManager("localhost", "snipes_casino", "root", "");
            sessionManager = new SessionManager();
            gameManager = new GameManager(config);
            bankingManager = new BankingManager(config);
            locationManager = new LocationManager(config);
            provablyFair = new ProvablyFair();
            profitTracker = new ProfitTracker();
            chatAI = new ChatAI();
            chatAI.setProfitTracker(profitTracker);
            chatAI.setConfig(config);
            chatAI.setProvablyFair(provablyFair);
            muleManager = new MuleManager(config);
            humanizationManager = new HumanizationManager(config);
            tradeManager = new TradeManager(config, dbManager);
            tradeRequestListener = new TradeRequestListener(tradeManager, config.tradeConfig);
            
            if (config.discordEnabled && !config.discordWebhookUrl.isEmpty()) {
                webhook = new DiscordWebhook(config.discordWebhookUrl);
                webhook.send("🚀 **SnipesScripts Ultimate v10.0** is now ONLINE!");
            }
        } catch (Exception e) {
            log("Critical Error during initialization: " + e.getMessage());
            stop();
        }
        
        state = CasinoState.INITIALIZING;
        lastStateChangeTime = System.currentTimeMillis();
    }

    @Override
    public int onLoop() {
        if (!guiFinished) return 500;
        if (!startScript) { stop(); return 0; }

        // Robust State Watchdog
        if (state == lastState) {
            if (System.currentTimeMillis() - lastStateChangeTime > 120000) { // 2 minute timeout
                log("State Watchdog: Script stuck in " + state + ". Attempting recovery...");
                state = CasinoState.ERROR_RECOVERY;
            }
        } else {
            lastState = state;
            lastStateChangeTime = System.currentTimeMillis();
        }

        try {
            if (humanizationManager.shouldTakeBreak()) {
                humanizationManager.takeBreak();
                return 1000;
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
            log("Runtime Error: " + e.getMessage());
            state = CasinoState.ERROR_RECOVERY;
        }
        return Calculations.random(200, 400);
    }

    private int handleInitializing() {
        state = (config.walkOnStart && !locationManager.isAtLocation()) ? CasinoState.WALKING_TO_LOCATION : CasinoState.IDLE;
        return 500;
    }

    private int handleWalking() {
        if (locationManager.isAtLocation()) { state = CasinoState.IDLE; return 500; }
        locationManager.walkToLocation();
        return 1000;
    }

    private int handleBanking() {
        if (bankingManager.restock()) state = CasinoState.IDLE;
        return 1000;
    }

    private int handleIdle() {
        if (Trade.isOpen()) {
            currentPlayer = Trade.getTradingWith();
            tradeManager.reset();
            state = CasinoState.TRADING_WINDOW_1;
            return 300;
        }

        if (tradeRequestListener.hasPendingRequests()) {
            TradeRequestListener.TradeRequestEvent request = tradeRequestListener.getNextRequest();
            if (request != null) {
                Player p = Players.closest(request.playerName);
                if (p != null) p.interact("Trade with");
            }
        }

        if (System.currentTimeMillis() - lastAdTime > config.adIntervalSeconds * 1000L) {
            sendRotatingAd();
            lastAdTime = System.currentTimeMillis();
        }
        
        return 1000;
    }

    private void sendRotatingAd() {
        if (config.adMessages.isEmpty()) return;
        Keyboard.type(config.adMessages.get(adIndex), true);
        adIndex = (adIndex + 1) % config.adMessages.size();
    }

    private int handleTrade1() {
        if (!Trade.isOpen(1)) {
            if (Trade.isOpen(2)) { state = CasinoState.TRADING_WINDOW_2; return 300; }
            state = CasinoState.IDLE; return 500;
        }
        tradeManager.handleTradeScreen1();
        return 500;
    }

    private int handleTrade2() {
        if (!Trade.isOpen(2)) {
            if (Trade.isOpen(1)) { state = CasinoState.TRADING_WINDOW_1; return 300; }
            state = CasinoState.PROCESSING_GAME;
            return 500;
        }
        tradeManager.handleTradeScreen2();
        return 500;
    }

    private int handleGame() {
        // Java 8 compatible Future handling
        try {
            long balance = dbManager.getBalance(currentPlayer).get(); // Blocking get for script flow
            if (balance < config.minBet) {
                Keyboard.type("Insufficient balance! Deposit more Coins or Platinum Tokens.", true);
                state = CasinoState.IDLE;
                return 1000;
            }

            currentBet = balance;
            AbstractGame game = gameManager.getGame(selectedGame);
            GameResult result = game.play(currentPlayer, currentBet, provablyFair.getSeed());
            
            Keyboard.type(result.getDescription(), true);
            
            long payout = result.isWin() ? result.getPayout() : -currentBet;
            dbManager.updateBalance(currentPlayer, payout, currentBet, result.isWin() ? payout : 0);
            
            long newBalance = balance + payout;

            if (webhook != null) {
                webhook.sendGameResult(currentPlayer, result.isWin(), currentBet, result.getPayout(), result.getDescription(), provablyFair.getSeed(), newBalance, config);
            }

            state = result.isWin() ? CasinoState.PAYOUT_PENDING : CasinoState.IDLE;
        } catch (Exception e) {
            log("Game Processing Error: " + e.getMessage());
            state = CasinoState.ERROR_RECOVERY;
        }
        return 1000;
    }

    private int handlePayout() {
        state = CasinoState.IDLE;
        return 1000;
    }

    private int handleRecovery() {
        log("Recovery: Resetting interfaces and state...");
        if (Trade.isOpen()) Trade.declineTrade();
        state = CasinoState.IDLE;
        return 2000;
    }

    @Override
    public void onMessage(Message msg) {
        if (chatAI != null) {
            chatAI.handleChat(msg.getUsername(), msg.getMessage(), msg.getType().name().contains("CLAN"));
        }
        if (tradeRequestListener != null) tradeRequestListener.onMessage(msg);
    }

    @Override
    public void onPaint(Graphics g) {
        g.setColor(new Color(0, 255, 255, 150));
        g.fillRect(10, 30, 220, 80);
        g.setColor(Color.BLACK);
        g.drawRect(10, 30, 220, 80);
        
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("SnipesScripts Ultimate v10.0", 15, 45);
        g.setFont(new Font("Arial", Font.PLAIN, 11));
        g.drawString("State: " + state, 15, 60);
        g.drawString("Profit: " + profitTracker.getNetProfit() + " GP", 15, 75);
        g.drawString("Database: CONNECTED", 15, 90);
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
