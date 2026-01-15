package com.ikingsnipe.casino;
import com.ikingsnipe.casino.core.CasinoState;
import com.ikingsnipe.casino.games.*;
import com.ikingsnipe.casino.gui.CasinoGUI;
import com.ikingsnipe.casino.managers.*;
import com.ikingsnipe.casino.models.*;
import com.ikingsnipe.casino.utils.ProvablyFair;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;
import javax.swing.*;
import java.awt.*;

@ScriptManifest(name = "Elite Titan Casino v13.0", description = "Ultimate OSRS Casino Host Bot", author = "ikingsnipe", version = 13.0, category = Category.MISC)
public class EliteTitanCasino extends AbstractScript {
    private CasinoConfig config; private CasinoState state = CasinoState.IDLE; private CasinoGUI gui;
    private GameManager gameManager; private SessionManager sessionManager; private BankingManager bankingManager; private LocationManager locationManager;
    private ProvablyFair provablyFair; private String currentPlayer = null, currentGame = null; private long currentBet = 0; private PlayerSession currentSession = null;
    private long stateStartTime, scriptStartTime, lastAdTime = 0; private boolean welcomeMessageSent = false;
    private int totalWins = 0, totalLosses = 0; private long totalProfit = 0; private int gamesPlayed = 0;
    private volatile boolean guiComplete = false, startScript = false;

    @Override
    public void onStart() {
        config = new CasinoConfig();
        SwingUtilities.invokeLater(() -> { gui = new CasinoGUI(config, b -> { guiComplete = true; startScript = b; if(b) initializeManagers(); }); gui.setVisible(true); });
    }
    private void initializeManagers() {
        provablyFair = new ProvablyFair(); gameManager = new GameManager(config); sessionManager = new SessionManager();
        bankingManager = new BankingManager(config); locationManager = new LocationManager(config);
        state = CasinoState.INITIALIZING; scriptStartTime = System.currentTimeMillis();
    }
    @Override
    public int onLoop() {
        if (!guiComplete) return 500; if (!startScript) { stop(); return 0; }
        if (state != CasinoState.IDLE && state != CasinoState.INITIALIZING && System.currentTimeMillis() - stateStartTime > config.tradeTimeoutMs) { resetState("Timeout"); return 1000; }
        try {
            switch (state) {
                case INITIALIZING: return handleInitializing();
                case WALKING_TO_LOCATION: return handleWalkingToLocation();
                case BANKING: return handleBanking();
                case IDLE: return handleIdle();
                case TRADING_WINDOW_1: return handleTradeWindow1();
                case TRADING_WINDOW_2: return handleTradeWindow2();
                case PROCESSING_GAME: return handleGameProcessing();
                case PAYOUT_PENDING: return handlePayout();
                case ERROR_RECOVERY: return handleErrorRecovery();
            }
        } catch (Exception e) { state = CasinoState.ERROR_RECOVERY; }
        return Calculations.random(config.loopDelayMinMs, config.loopDelayMaxMs);
    }
    private int handleInitializing() {
        Tile t = config.getStartLocationTile();
        if (config.walkToLocationOnStart && t != null && Players.getLocal().getTile().distance(t) > 10) { state = CasinoState.WALKING_TO_LOCATION; return 500; }
        if (config.autoRestock && getInventoryValue() < config.restockThreshold) { state = CasinoState.BANKING; return 500; }
        state = CasinoState.IDLE; return 500;
    }
    private int handleWalkingToLocation() {
        Tile t = config.getStartLocationTile(); if (t == null || Players.getLocal().getTile().distance(t) <= 5) { state = CasinoState.IDLE; return 500; }
        if (Walking.shouldWalk(5)) Walking.walk(t); return 1000;
    }
    private int handleBanking() {
        if (!Bank.isOpen()) { if (Bank.open()) Sleep.sleepUntil(Bank::isOpen, 5000); return 1000; }
        if (Bank.contains(CasinoConfig.PLATINUM_TOKEN_ID)) Bank.withdrawAll(CasinoConfig.PLATINUM_TOKEN_ID);
        if (Bank.contains(CasinoConfig.COINS_ID)) Bank.withdrawAll(CasinoConfig.COINS_ID);
        Bank.close(); state = CasinoState.IDLE; return 1000;
    }
    private int handleIdle() {
        if (Trade.isOpen()) {
            currentPlayer = Trade.getTradingWith(); if (currentPlayer == null) currentPlayer = "Player";
            state = CasinoState.TRADING_WINDOW_1; stateStartTime = System.currentTimeMillis(); welcomeMessageSent = false;
            currentSession = sessionManager.getSession(currentPlayer); provablyFair.newRound(currentPlayer); return 500;
        }
        if (System.currentTimeMillis() - lastAdTime > config.adIntervalMs) { Keyboard.type(config.adMessage, true); lastAdTime = System.currentTimeMillis(); }
        return 1000;
    }
    private int handleTradeWindow1() {
        if (!Trade.isOpen(1)) { if (!Trade.isOpen()) resetState("Closed"); return 500; }
        if (!welcomeMessageSent) { Keyboard.type(String.format(config.tradeWelcome, provablyFair.getServerSeedHash().substring(0,12)), true); welcomeMessageSent = true; }
        long val = getTheirOfferedValue();
        if (val >= config.minBet && val <= config.maxBet) { currentBet = val; currentGame = config.defaultGame; if (Trade.acceptTrade()) Sleep.sleepUntil(() -> Trade.isOpen(2), 5000); }
        return 1000;
    }
    private int handleTradeWindow2() {
        if (!Trade.isOpen(2)) { if (!Trade.isOpen()) state = CasinoState.PROCESSING_GAME; return 500; }
        if (Trade.acceptTrade()) Sleep.sleepUntil(() -> !Trade.isOpen(), 5000); return 1000;
    }
    private int handleGameProcessing() {
        GameResult r = gameManager.play(currentGame, currentBet); gamesPlayed++;
        String reveal = provablyFair.revealServerSeed().substring(0,10);
        if (r.isWin()) {
            totalWins++; totalProfit -= (r.getPayout() - currentBet);
            Keyboard.type(String.format(config.winAnnouncement, currentPlayer, r.getResultDescription(), formatGP(r.getPayout()), reveal), true);
            currentSession.setOwedAmount(r.getPayout()); state = CasinoState.PAYOUT_PENDING;
        } else {
            totalLosses++; totalProfit += currentBet;
            Keyboard.type(String.format(config.lossAnnouncement, currentPlayer, r.getResultDescription()), true); resetState("Loss");
        }
        return 1000;
    }
    private int handlePayout() {
        if (currentSession.getOwedAmount() <= 0) { resetState("Done"); return 500; }
        Player p = Players.closest(currentPlayer); if (p == null) return 2000;
        if (Trade.isOpen()) {
            if (Trade.isOpen(1)) { addPayout(currentSession.getOwedAmount()); Trade.acceptTrade(); }
            else if (Trade.isOpen(2)) { if (Trade.acceptTrade()) { currentSession.setOwedAmount(0); resetState("Paid"); } }
        } else { p.interact("Trade with"); Sleep.sleepUntil(Trade::isOpen, 5000); }
        return 1000;
    }
    private int handleErrorRecovery() { Widgets.closeAll(); resetState("Error"); return 1000; }
    private void resetState(String r) { state = CasinoState.IDLE; currentPlayer = null; currentBet = 0; welcomeMessageSent = false; stateStartTime = System.currentTimeMillis(); }
    private long getTheirOfferedValue() {
        long v = 0; Item[] items = Trade.getTheirItems();
        if (items != null) for (Item i : items) if (i != null) { if (i.getID() == CasinoConfig.COINS_ID) v += i.getAmount(); else if (i.getID() == CasinoConfig.PLATINUM_TOKEN_ID) v += (long)i.getAmount() * 1000; }
        return v;
    }
    private long getInventoryValue() { return Inventory.count(995) + (long)Inventory.count(13204) * 1000; }
    private void addPayout(long a) {
        long rem = a; if (rem >= 1000) { int t = (int)(rem/1000); Trade.addItem(13204, t); rem -= (long)t*1000; }
        if (rem > 0) Trade.addItem(995, (int)rem);
    }
    private String formatGP(long a) { if (a >= 1000000) return (a/1000000) + "M"; if (a >= 1000) return (a/1000) + "K"; return String.valueOf(a); }
    @Override public void onPaint(Graphics g) { g.setColor(Color.YELLOW); g.drawString("Elite Casino v13.0 - State: " + state, 10, 50); g.drawString("Profit: " + formatGP(totalProfit), 10, 70); }
}
